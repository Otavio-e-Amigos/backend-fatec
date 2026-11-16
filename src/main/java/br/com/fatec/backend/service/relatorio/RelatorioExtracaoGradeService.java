package br.com.fatec.backend.service.relatorio;

import br.com.fatec.backend.dto.relatorio.PeriodoRelatorioDTO;
import br.com.fatec.backend.dto.relatorio.RelatorioExtracaoGradeDTO;
import br.com.fatec.backend.dto.relatorio.RelatorioExtracaoGradeDTO.*;
import br.com.fatec.backend.entity.RegistroExtracaoGrade;
import br.com.fatec.backend.enums.StatusExtracaoGrade;
import br.com.fatec.backend.repository.RegistroExtracaoGradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Monta o relatório de extração de grades a partir do histórico {@code registro_extracao_grade}.
 * Uma única consulta por intervalo (indexada por created_at) e agregação em memória.
 */
@Service
public class RelatorioExtracaoGradeService {

    private final RegistroExtracaoGradeRepository repository;
    private final Clock clock;

    public RelatorioExtracaoGradeService(RegistroExtracaoGradeRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public RelatorioExtracaoGradeDTO gerar(String tipoPeriodo, LocalDate dataReferencia) {
        LocalDate hoje = LocalDate.now(clock);
        PeriodoRelatorio periodo = PeriodoRelatorio.de(tipoPeriodo, dataReferencia, hoje);
        List<RegistroExtracaoGrade> registros = repository.buscarNoIntervalo(periodo.inicio(), periodo.fimExclusivo());

        return new RelatorioExtracaoGradeDTO(
                PeriodoRelatorioDTO.de(periodo, hoje),
                LocalDateTime.now(clock),
                montarResumo(registros),
                montarEvolucaoDiaria(periodo, registros),
                montarPorUsuario(registros),
                montarPorPeriodoLetivo(registros),
                montarPrincipaisFalhas(registros),
                registros.stream().map(this::paraItem).toList()
        );
    }

    private Resumo montarResumo(List<RegistroExtracaoGrade> registros) {
        long total = registros.size();
        long semAvisos = contar(registros, StatusExtracaoGrade.SUCESSO);
        long comAvisos = contar(registros, StatusExtracaoGrade.SUCESSO_COM_AVISOS);
        long falhas = contar(registros, StatusExtracaoGrade.FALHA);
        long extraidas = semAvisos + comAvisos;

        List<RegistroExtracaoGrade> sucessos = registros.stream().filter(r -> r.getStatus() != StatusExtracaoGrade.FALHA).toList();
        long disciplinas = sucessos.stream().mapToLong(RegistroExtracaoGrade::getQuantidadeDisciplinas).sum();
        long aulas = sucessos.stream().mapToLong(RegistroExtracaoGrade::getQuantidadeAulas).sum();
        long avisos = sucessos.stream().mapToLong(RegistroExtracaoGrade::getQuantidadeAvisos).sum();

        LongSummaryStatistics duracoes = registros.stream()
                .map(RegistroExtracaoGrade::getDuracaoMs).filter(Objects::nonNull)
                .mapToLong(Long::longValue).summaryStatistics();

        Set<String> matriculas = sucessos.stream()
                .map(RegistroExtracaoGrade::getProfessorMatricula).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> matriculasCadastradas = sucessos.stream()
                .filter(r -> r.getProfessor() != null && r.getProfessorMatricula() != null)
                .map(RegistroExtracaoGrade::getProfessorMatricula)
                .collect(Collectors.toSet());

        long volumeBytes = registros.stream().map(RegistroExtracaoGrade::getTamanhoBytes)
                .filter(Objects::nonNull).mapToLong(Long::longValue).sum();

        return new Resumo(
                total, extraidas, semAvisos, comAvisos, falhas,
                percentual(extraidas, total),
                disciplinas, aulas, avisos,
                media(disciplinas, extraidas),
                duracoes.getCount() > 0 ? Math.round(duracoes.getAverage()) : null,
                duracoes.getCount() > 0 ? duracoes.getMax() : null,
                matriculas.size(), matriculasCadastradas.size(), matriculas.size() - matriculasCadastradas.size(),
                Math.round(volumeBytes / 1024.0)
        );
    }

    private List<SerieDiaria> montarEvolucaoDiaria(PeriodoRelatorio periodo, List<RegistroExtracaoGrade> registros) {
        Map<LocalDate, List<RegistroExtracaoGrade>> porDia = registros.stream()
                .collect(Collectors.groupingBy(r -> r.getCreatedAt().toLocalDate()));
        return periodo.dias().stream().map(dia -> {
            List<RegistroExtracaoGrade> doDia = porDia.getOrDefault(dia, List.of());
            long falhas = contar(doDia, StatusExtracaoGrade.FALHA);
            return new SerieDiaria(dia, doDia.size(), doDia.size() - falhas, falhas);
        }).toList();
    }

    private List<PorUsuario> montarPorUsuario(List<RegistroExtracaoGrade> registros) {
        Map<Long, List<RegistroExtracaoGrade>> porUsuario = registros.stream()
                .collect(Collectors.groupingBy(r -> r.getUsuario() != null ? r.getUsuario().getId() : -1L,
                        LinkedHashMap::new, Collectors.toList()));
        return porUsuario.entrySet().stream().map(e -> {
                    List<RegistroExtracaoGrade> lista = e.getValue();
                    long falhas = contar(lista, StatusExtracaoGrade.FALHA);
                    RegistroExtracaoGrade primeiro = lista.get(0);
                    return new PorUsuario(
                            e.getKey() >= 0 ? e.getKey() : null,
                            primeiro.getUsuario() != null ? primeiro.getUsuario().getNome() : "Não identificado",
                            lista.size(), lista.size() - falhas, falhas);
                })
                .sorted(Comparator.comparingLong(PorUsuario::total).reversed().thenComparing(PorUsuario::usuario))
                .toList();
    }

    private List<PorPeriodoLetivo> montarPorPeriodoLetivo(List<RegistroExtracaoGrade> registros) {
        return registros.stream()
                .filter(r -> r.getStatus() != StatusExtracaoGrade.FALHA && r.getAno() != null && r.getSemestre() != null)
                .collect(Collectors.groupingBy(r -> List.of(r.getAno(), (int) r.getSemestre()), Collectors.counting()))
                .entrySet().stream()
                .map(e -> new PorPeriodoLetivo(e.getKey().get(0), e.getKey().get(1), e.getValue()))
                .sorted(Comparator.comparing(PorPeriodoLetivo::ano).reversed()
                        .thenComparing(Comparator.comparing(PorPeriodoLetivo::semestre).reversed()))
                .toList();
    }

    private List<MotivoFalha> montarPrincipaisFalhas(List<RegistroExtracaoGrade> registros) {
        return registros.stream()
                .filter(r -> r.getStatus() == StatusExtracaoGrade.FALHA)
                .map(r -> r.getMensagemErro() != null ? r.getMensagemErro() : "Erro não identificado.")
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> new MotivoFalha(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(MotivoFalha::quantidade).reversed().thenComparing(MotivoFalha::motivo))
                .limit(10)
                .toList();
    }

    private ItemExtracao paraItem(RegistroExtracaoGrade r) {
        String periodoLetivo = r.getAno() != null && r.getSemestre() != null ? r.getSemestre() + "º/" + r.getAno() : null;
        return new ItemExtracao(
                r.getId(), r.getCreatedAt(),
                r.getUsuario() != null ? r.getUsuario().getNome() : null,
                r.getNomeArquivo(),
                r.getTamanhoBytes() != null ? Math.round(r.getTamanhoBytes() / 1024.0) : null,
                r.getStatus(), r.getProfessorNome(), r.getProfessorMatricula(),
                r.getProfessor() != null, periodoLetivo,
                r.getQuantidadeDisciplinas(), r.getQuantidadeAulas(), r.getQuantidadeAvisos(),
                r.getDuracaoMs(), r.getMensagemErro()
        );
    }

    private static long contar(List<RegistroExtracaoGrade> registros, StatusExtracaoGrade status) {
        return registros.stream().filter(r -> r.getStatus() == status).count();
    }

    static BigDecimal percentual(long parte, long total) {
        if (total == 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(parte * 100.0 / total).setScale(1, RoundingMode.HALF_UP);
    }

    static BigDecimal media(long soma, long quantidade) {
        if (quantidade == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf((double) soma / quantidade).setScale(2, RoundingMode.HALF_UP);
    }
}
