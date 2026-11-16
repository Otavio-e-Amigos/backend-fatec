package br.com.fatec.backend.service.relatorio;

import br.com.fatec.backend.dto.relatorio.PeriodoRelatorioDTO;
import br.com.fatec.backend.dto.relatorio.RelatorioFolhaPontoDTO;
import br.com.fatec.backend.dto.relatorio.RelatorioFolhaPontoDTO.*;
import br.com.fatec.backend.entity.DisciplinaDaGrade;
import br.com.fatec.backend.entity.Grade;
import br.com.fatec.backend.entity.PeriodoLetivo;
import br.com.fatec.backend.entity.Professor;
import br.com.fatec.backend.entity.RegistroFolhaPonto;
import br.com.fatec.backend.enums.StatusDisciplinaGrade;
import br.com.fatec.backend.enums.StatusProfessor;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.repository.DisciplinaDaGradeRepository;
import br.com.fatec.backend.repository.GradeRepository;
import br.com.fatec.backend.repository.ProfessorRepository;
import br.com.fatec.backend.repository.RegistroFolhaPontoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Monta o relatório de geração de folhas de ponto a partir do histórico {@code registro_folha_ponto},
 * individual (um professor, com grades e disciplinas) ou geral (todos os professores, comparável).
 */
@Service
public class RelatorioFolhaPontoService {

    private static final int LIMITE_PRINCIPAIS_DISCIPLINAS = 10;

    private final RegistroFolhaPontoRepository registroRepository;
    private final ProfessorRepository professorRepository;
    private final GradeRepository gradeRepository;
    private final DisciplinaDaGradeRepository disciplinaDaGradeRepository;
    private final Clock clock;

    public RelatorioFolhaPontoService(RegistroFolhaPontoRepository registroRepository,
                                      ProfessorRepository professorRepository,
                                      GradeRepository gradeRepository,
                                      DisciplinaDaGradeRepository disciplinaDaGradeRepository,
                                      Clock clock) {
        this.registroRepository = registroRepository;
        this.professorRepository = professorRepository;
        this.gradeRepository = gradeRepository;
        this.disciplinaDaGradeRepository = disciplinaDaGradeRepository;
        this.clock = clock;
    }

    /**
     * @param professorId quando informado, gera o relatório individual; quando nulo, o geral
     */
    @Transactional(readOnly = true)
    public RelatorioFolhaPontoDTO gerar(String tipoPeriodo, LocalDate dataReferencia, Long professorId) {
        LocalDate hoje = LocalDate.now(clock);
        PeriodoRelatorio periodo = PeriodoRelatorio.de(tipoPeriodo, dataReferencia, hoje);

        Professor professor = null;
        List<RegistroFolhaPonto> registros;
        if (professorId != null) {
            professor = professorRepository.findById(professorId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Professor não encontrado com ID: " + professorId));
            registros = registroRepository.buscarNoIntervaloPorProfessor(professorId, periodo.inicio(), periodo.fimExclusivo());
        } else {
            registros = registroRepository.buscarNoIntervalo(periodo.inicio(), periodo.fimExclusivo());
        }

        boolean individual = professor != null;
        List<PorProfessor> porProfessor = montarPorProfessor(registros, individual ? List.of(professor) : List.of());

        return new RelatorioFolhaPontoDTO(
                PeriodoRelatorioDTO.de(periodo, hoje),
                LocalDateTime.now(clock),
                individual ? RelatorioFolhaPontoDTO.ESCOPO_INDIVIDUAL : RelatorioFolhaPontoDTO.ESCOPO_TODOS,
                montarResumo(registros, individual),
                porProfessor,
                periodo.dias().stream().map(dia -> new SerieDiaria(dia,
                        registros.stream().filter(r -> r.getCreatedAt().toLocalDate().equals(dia)).count())).toList(),
                montarPorCompetencia(registros),
                montarPorUsuario(registros),
                registros.stream().map(RelatorioFolhaPontoService::paraHistorico).toList(),
                individual ? montarDetalheProfessor(professor) : null
        );
    }

    // -------------------------------------------------------------------------
    // Visão consolidada
    // -------------------------------------------------------------------------

    private Resumo montarResumo(List<RegistroFolhaPonto> registros, boolean individual) {
        long total = registros.size();
        Set<Long> professores = registros.stream().map(r -> r.getProfessor().getId()).collect(Collectors.toSet());
        long competencias = registros.stream().map(RelatorioFolhaPontoService::competencia).distinct().count();
        long combinacoesUnicas = registros.stream()
                .map(r -> r.getProfessor().getId() + "|" + competencia(r)).distinct().count();
        long outroPeriodo = registros.stream().filter(RegistroFolhaPonto::isGradeOutroPeriodo).count();

        Long ativos = null;
        Long ativosSemFolha = null;
        if (!individual) {
            ativos = professorRepository.countByStatus(StatusProfessor.ATIVO);
            long ativosAtendidos = registros.stream()
                    .map(RegistroFolhaPonto::getProfessor)
                    .filter(p -> p.getStatus() == StatusProfessor.ATIVO)
                    .map(Professor::getId).distinct().count();
            ativosSemFolha = Math.max(0, ativos - ativosAtendidos);
        }

        return new Resumo(total, professores.size(),
                RelatorioExtracaoGradeService.media(total, professores.size()),
                ativos, ativosSemFolha, competencias, total - combinacoesUnicas, outroPeriodo);
    }

    private List<PorProfessor> montarPorProfessor(List<RegistroFolhaPonto> registros, List<Professor> obrigatorios) {
        Map<Long, List<RegistroFolhaPonto>> agrupado = registros.stream()
                .collect(Collectors.groupingBy(r -> r.getProfessor().getId(), LinkedHashMap::new, Collectors.toList()));

        Map<Long, Professor> professores = new LinkedHashMap<>();
        registros.forEach(r -> professores.putIfAbsent(r.getProfessor().getId(), r.getProfessor()));
        obrigatorios.forEach(p -> professores.putIfAbsent(p.getId(), p));
        if (professores.isEmpty()) {
            return List.of();
        }

        Map<Long, Object[]> historico = new HashMap<>();
        for (Object[] linha : registroRepository.totaisHistoricosPorProfessor(professores.keySet())) {
            historico.put(((Number) linha[0]).longValue(), linha);
        }

        long total = registros.size();
        return professores.values().stream().map(p -> {
                    List<RegistroFolhaPonto> doProfessor = agrupado.getOrDefault(p.getId(), List.of());
                    Object[] hist = historico.get(p.getId());
                    List<String> competencias = doProfessor.stream()
                            .sorted(Comparator.comparing(RegistroFolhaPonto::getAno).thenComparing(RegistroFolhaPonto::getMes))
                            .map(RelatorioFolhaPontoService::competencia).distinct().toList();
                    return new PorProfessor(
                            p.getId(), p.getNome(), p.getMatricula(),
                            p.getStatus() != null ? p.getStatus().name() : null,
                            doProfessor.size(),
                            RelatorioExtracaoGradeService.percentual(doProfessor.size(), total),
                            hist != null ? ((Number) hist[1]).longValue() : 0L,
                            hist != null ? (LocalDateTime) hist[2] : null,
                            competencias);
                })
                .sorted(Comparator.comparingLong(PorProfessor::folhasNoPeriodo).reversed()
                        .thenComparing(PorProfessor::nome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<PorCompetencia> montarPorCompetencia(List<RegistroFolhaPonto> registros) {
        Map<String, List<RegistroFolhaPonto>> agrupado = registros.stream()
                .sorted(Comparator.comparing(RegistroFolhaPonto::getAno).thenComparing(RegistroFolhaPonto::getMes).reversed())
                .collect(Collectors.groupingBy(RelatorioFolhaPontoService::competencia, LinkedHashMap::new, Collectors.toList()));
        return agrupado.entrySet().stream()
                .map(e -> new PorCompetencia(e.getKey(), e.getValue().size(),
                        e.getValue().stream().map(r -> r.getProfessor().getId()).distinct().count()))
                .toList();
    }

    private List<PorUsuario> montarPorUsuario(List<RegistroFolhaPonto> registros) {
        Map<Long, List<RegistroFolhaPonto>> agrupado = registros.stream()
                .collect(Collectors.groupingBy(r -> r.getUsuario() != null ? r.getUsuario().getId() : -1L,
                        LinkedHashMap::new, Collectors.toList()));
        return agrupado.entrySet().stream()
                .map(e -> new PorUsuario(
                        e.getKey() >= 0 ? e.getKey() : null,
                        e.getValue().get(0).getUsuario() != null ? e.getValue().get(0).getUsuario().getNome() : "Não identificado",
                        e.getValue().size()))
                .sorted(Comparator.comparingLong(PorUsuario::quantidade).reversed())
                .toList();
    }

    private static ItemHistorico paraHistorico(RegistroFolhaPonto r) {
        String periodoGrade = r.getPeriodoLetivoAno() != null && r.getPeriodoLetivoSemestre() != null
                ? r.getPeriodoLetivoSemestre() + "º/" + r.getPeriodoLetivoAno() : null;
        return new ItemHistorico(
                r.getId(), r.getCreatedAt(),
                r.getProfessor().getId(), r.getProfessor().getNome(), r.getProfessor().getMatricula(),
                competencia(r), periodoGrade, r.isGradeOutroPeriodo(),
                r.getDiasSemAula() != null ? r.getDiasSemAula() : 0,
                r.getQuantidadeDisciplinas() != null ? r.getQuantidadeDisciplinas() : 0,
                r.getUsuario() != null ? r.getUsuario().getNome() : null);
    }

    static String competencia(RegistroFolhaPonto r) {
        return String.format("%02d/%d", r.getMes(), r.getAno());
    }

    // -------------------------------------------------------------------------
    // Informações complementares do relatório individual
    // -------------------------------------------------------------------------

    private DetalheProfessor montarDetalheProfessor(Professor professor) {
        Long id = professor.getId();

        long totalHistorico = 0;
        LocalDateTime primeira = null;
        LocalDateTime ultima = null;
        List<Object[]> resumo = registroRepository.resumoHistoricoDoProfessor(id);
        if (!resumo.isEmpty() && resumo.get(0) != null && resumo.get(0)[0] != null) {
            Object[] linha = resumo.get(0);
            totalHistorico = ((Number) linha[0]).longValue();
            primeira = (LocalDateTime) linha[1];
            ultima = (LocalDateTime) linha[2];
        }

        Map<Long, Long> folhasPorGrade = new HashMap<>();
        for (Object[] linha : registroRepository.totaisPorGradeDoProfessor(id)) {
            folhasPorGrade.put(((Number) linha[0]).longValue(), ((Number) linha[1]).longValue());
        }

        List<Grade> grades = gradeRepository.findByProfessorIdComPeriodo(id);
        List<DisciplinaDaGrade> vinculos = disciplinaDaGradeRepository.findByProfessorIdComDetalhes(id);
        Map<Long, List<DisciplinaDaGrade>> vinculosPorGrade = vinculos.stream()
                .collect(Collectors.groupingBy(dg -> dg.getGrade().getId()));

        List<GradeDoProfessor> gradesDTO = grades.stream().map(g -> {
            List<DisciplinaDaGrade> daGrade = vinculosPorGrade.getOrDefault(g.getId(), List.of());
            List<DisciplinaDaGrade> ministrando = daGrade.stream()
                    .filter(dg -> dg.getStatus() == StatusDisciplinaGrade.MINISTRANDO).toList();
            int aulasSemanais = ministrando.stream()
                    .mapToInt(dg -> dg.getQuantidadeAulas() != null ? dg.getQuantidadeAulas() : 0).sum();
            PeriodoLetivo pl = g.getPeriodoLetivo();
            return new GradeDoProfessor(
                    g.getId(), rotuloPeriodo(pl),
                    pl != null ? pl.getDataInicio() : null, pl != null ? pl.getDataFim() : null,
                    g.getSemanal(), g.getMensal(), g.getTotal(),
                    daGrade.size(), ministrando.size(), aulasSemanais,
                    folhasPorGrade.getOrDefault(g.getId(), 0L));
        }).toList();

        List<String> cursos = vinculos.stream()
                .map(dg -> rotuloCurso(dg))
                .filter(Objects::nonNull).distinct().sorted().toList();

        return new DetalheProfessor(
                id, professor.getNome(), professor.getCodigo(), professor.getMatricula(),
                nomeOuNulo(professor.getTitulacao()), nomeOuNulo(professor.getRegimeJuridico()),
                nomeOuNulo(professor.getRegimeContrato()), nomeOuNulo(professor.getStatus()),
                totalHistorico, primeira, ultima, cursos, gradesDTO,
                montarPrincipaisDisciplinas(vinculos));
    }

    /**
     * Agrupa os vínculos por disciplina. Disciplinas canceladas não entram na contagem de aulas,
     * mas o período em que aparecem é considerado. Ordena por recorrência e carga semanal.
     */
    private List<DisciplinaDoProfessor> montarPrincipaisDisciplinas(List<DisciplinaDaGrade> vinculos) {
        Map<Long, List<DisciplinaDaGrade>> porDisciplina = vinculos.stream()
                .collect(Collectors.groupingBy(dg -> dg.getDisciplina().getId(), LinkedHashMap::new, Collectors.toList()));

        return porDisciplina.values().stream().map(lista -> {
                    var disciplina = lista.get(0).getDisciplina();
                    List<PeriodoLetivo> periodos = lista.stream()
                            .map(dg -> dg.getGrade().getPeriodoLetivo()).filter(Objects::nonNull)
                            .distinct().toList();
                    PeriodoLetivo maisRecente = periodos.stream()
                            .max(Comparator.comparing(PeriodoLetivo::getAno).thenComparing(PeriodoLetivo::getSemestre))
                            .orElse(null);
                    int aulas = lista.stream()
                            .filter(dg -> dg.getStatus() == StatusDisciplinaGrade.MINISTRANDO)
                            .mapToInt(dg -> dg.getQuantidadeAulas() != null ? dg.getQuantidadeAulas() : 0).sum();
                    List<String> cursos = lista.stream().map(RelatorioFolhaPontoService::rotuloCurso)
                            .filter(Objects::nonNull).distinct().sorted().toList();
                    return new DisciplinaDoProfessor(disciplina.getId(), disciplina.getSigla(), disciplina.getNome(),
                            disciplina.getCodigo(), periodos.size(), aulas, cursos, rotuloPeriodo(maisRecente));
                })
                .sorted(Comparator.comparingInt(DisciplinaDoProfessor::periodosLetivos).reversed()
                        .thenComparing(Comparator.comparingInt(DisciplinaDoProfessor::aulasSemanaisTotal).reversed())
                        .thenComparing(DisciplinaDoProfessor::nome, String.CASE_INSENSITIVE_ORDER))
                .limit(LIMITE_PRINCIPAIS_DISCIPLINAS)
                .toList();
    }

    private static String rotuloCurso(DisciplinaDaGrade dg) {
        if (dg.getCurso() == null) {
            return null;
        }
        String turno = dg.getCurso().getTurno() != null ? " (" + dg.getCurso().getTurno().name() + ")" : "";
        return dg.getCurso().getNome() + turno;
    }

    private static String rotuloPeriodo(PeriodoLetivo pl) {
        return pl != null ? pl.getSemestre() + "º/" + pl.getAno() : null;
    }

    private static String nomeOuNulo(Enum<?> valor) {
        return valor != null ? valor.name() : null;
    }
}
