package br.com.fatec.backend.service;

import br.com.fatec.backend.document.FolhaPontoDocxService;
import br.com.fatec.backend.dto.folha_ponto.*;
import br.com.fatec.backend.entity.AulaDaGrade;
import br.com.fatec.backend.entity.Grade;
import br.com.fatec.backend.entity.Professor;
import br.com.fatec.backend.enums.SlotGrade;
import br.com.fatec.backend.enums.Turno;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.exception.RegraNegocioException;
import br.com.fatec.backend.repository.AulaDaGradeRepository;
import br.com.fatec.backend.repository.GradeRepository;
import br.com.fatec.backend.repository.ProfessorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Serviço que coordena a busca de informações no banco de dados e a geração
 * da Folha de Ponto / Frequência dos docentes.
 */
@Service
public class FolhaPontoService {

    private static final Map<DayOfWeek, String> DIAS_SEMANA_NOMES = Map.of(
            DayOfWeek.MONDAY, "Segunda",
            DayOfWeek.TUESDAY, "Terça",
            DayOfWeek.WEDNESDAY, "Quarta",
            DayOfWeek.THURSDAY, "Quinta",
            DayOfWeek.FRIDAY, "Sexta",
            DayOfWeek.SATURDAY, "Sábado",
            DayOfWeek.SUNDAY, "Domingo"
    );

    private final ProfessorRepository professorRepository;
    private final GradeRepository gradeRepository;
    private final AulaDaGradeRepository aulaDaGradeRepository;
    private final CalendarioAcademicoService calendarioAcademicoService;
    private final FolhaPontoDocxService folhaPontoDocxService;

    public FolhaPontoService(ProfessorRepository professorRepository,
                             GradeRepository gradeRepository,
                             AulaDaGradeRepository aulaDaGradeRepository,
                             CalendarioAcademicoService calendarioAcademicoService,
                             FolhaPontoDocxService folhaPontoDocxService) {
        this.professorRepository = professorRepository;
        this.gradeRepository = gradeRepository;
        this.aulaDaGradeRepository = aulaDaGradeRepository;
        this.calendarioAcademicoService = calendarioAcademicoService;
        this.folhaPontoDocxService = folhaPontoDocxService;
    }

    /**
     * Gera o arquivo DOCX final da folha de ponto para download.
     */
    @Transactional(readOnly = true)
    public byte[] gerarFolhaPonto(FolhaPontoRequisicaoDTO requisicao) {
        validarRequisicao(requisicao);

        Professor professor = buscarProfessor(requisicao.professorId());
        Grade grade = buscarOuDeterminarGrade(professor.getId(), requisicao.gradeId(), requisicao.ano(), requisicao.mes());
        List<AulaDaGrade> aulas = aulaDaGradeRepository.findByGradeIdComDetalhes(grade.getId());

        return folhaPontoDocxService.gerarDocumento(professor, grade, aulas, requisicao.mes(), requisicao.ano());
    }

    /**
     * Monta a prévia estruturada dos dados que comporão a Folha de Ponto para conferência.
     */
    @Transactional(readOnly = true)
    public FolhaPontoPreviewDTO montarPreview(FolhaPontoRequisicaoDTO requisicao) {
        validarRequisicao(requisicao);

        Professor professor = buscarProfessor(requisicao.professorId());
        Grade grade = buscarOuDeterminarGrade(professor.getId(), requisicao.gradeId(), requisicao.ano(), requisicao.mes());
        List<AulaDaGrade> aulas = aulaDaGradeRepository.findByGradeIdComDetalhes(grade.getId());

        ProfessorResumoDTO professorDTO = new ProfessorResumoDTO(
                professor.getId(),
                professor.getNome(),
                professor.getCodigo(),
                professor.getMatricula(),
                professor.getRegimeJuridico() != null ? professor.getRegimeJuridico().name() : null,
                professor.getRegimeContrato() != null ? professor.getRegimeContrato().name() : null
        );

        String titulo = String.format("FOLHA DE FREQUÊNCIA – %02d/%d", requisicao.mes(), requisicao.ano());

        // Mapear slots da grade horária
        List<AulaSlotDTO> gradeHoraria = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (AulaDaGrade aula : aulas) {
            Short dia = aula.getDiaSemana();
            if (dia == null || dia < 1 || dia > 6) {
                continue;
            }

            Turno turno = null;
            if (aula.getDisciplinaDaGrade() != null) {
                if (aula.getDisciplinaDaGrade().getPeriodo() != null && !aula.getDisciplinaDaGrade().getPeriodo().isBlank()) {
                    try {
                        turno = Turno.valueOf(aula.getDisciplinaDaGrade().getPeriodo().trim().toUpperCase());
                    } catch (Exception ignored) {}
                }
                if (turno == null && aula.getDisciplinaDaGrade().getCurso() != null) {
                    turno = aula.getDisciplinaDaGrade().getCurso().getTurno();
                }
            }

            List<SlotGrade> slots = SlotGrade.calcularSlots(turno, aula.getHoraInicio(), aula.getHoraFim());
            String sigla = "";
            String nomeDisciplina = "";
            if (aula.getDisciplinaDaGrade() != null && aula.getDisciplinaDaGrade().getDisciplina() != null) {
                sigla = aula.getDisciplinaDaGrade().getDisciplina().getSigla();
                nomeDisciplina = aula.getDisciplinaDaGrade().getDisciplina().getNome();
            }

            DayOfWeek dow = DayOfWeek.of(dia);
            String nomeDia = DIAS_SEMANA_NOMES.getOrDefault(dow, "Dia " + dia);

            for (SlotGrade slot : slots) {
                gradeHoraria.add(new AulaSlotDTO(
                        dia,
                        nomeDia,
                        slot.getTurno().name(),
                        slot.getOrdemNoTurno(),
                        sigla,
                        nomeDisciplina,
                        aula.getHoraInicio() != null ? aula.getHoraInicio().format(timeFormatter) : "",
                        aula.getHoraFim() != null ? aula.getHoraFim().format(timeFormatter) : ""
                ));
            }
        }

        // Mapear dias do mês
        List<DiaMesDTO> dias = new ArrayList<>();
        List<String> avisos = new ArrayList<>();
        int totalDias = YearMonth.of(requisicao.ano(), requisicao.mes()).lengthOfMonth();

        for (int d = 1; d <= totalDias; d++) {
            LocalDate data = LocalDate.of(requisicao.ano(), requisicao.mes(), d);
            DayOfWeek dow = data.getDayOfWeek();
            boolean isDomingo = (dow == DayOfWeek.SUNDAY);
            boolean isSemAula = calendarioAcademicoService.isDiaSemAula(data);
            String motivo = calendarioAcademicoService.getMotivoDiaSemAula(data);

            dias.add(new DiaMesDTO(
                    d,
                    DIAS_SEMANA_NOMES.getOrDefault(dow, ""),
                    isDomingo,
                    isSemAula,
                    motivo
            ));

            if (isSemAula && motivo != null && !isDomingo) {
                avisos.add(String.format("Dia %02d/%02d (%s): %s", d, requisicao.mes(), DIAS_SEMANA_NOMES.get(dow), motivo));
            }
        }

        return new FolhaPontoPreviewDTO(
                professorDTO,
                requisicao.mes(),
                requisicao.ano(),
                titulo,
                gradeHoraria,
                dias,
                avisos
        );
    }

    private void validarRequisicao(FolhaPontoRequisicaoDTO req) {
        if (req == null) {
            throw new RegraNegocioException("A requisição não pode ser nula.");
        }
        if (req.professorId() == null) {
            throw new RegraNegocioException("O ID do professor é obrigatório.");
        }
        if (req.mes() == null || req.mes() < 1 || req.mes() > 12) {
            throw new RegraNegocioException("O mês informado deve estar entre 1 e 12.");
        }
        if (req.ano() == null || req.ano() < 2000 || req.ano() > 2050) {
            throw new RegraNegocioException("O ano informado deve estar entre 2000 e 2050.");
        }
    }

    private Professor buscarProfessor(Long professorId) {
        return professorRepository.findById(professorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor não encontrado com ID: " + professorId));
    }

    private Grade buscarOuDeterminarGrade(Long professorId, Long gradeId, int ano, int mes) {
        if (gradeId != null) {
            Grade grade = gradeRepository.findById(gradeId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Grade horária não encontrada com ID: " + gradeId));

            if (!grade.getProfessor().getId().equals(professorId)) {
                throw new RegraNegocioException(String.format(
                        "A grade com ID %d pertence ao professor ID %d e não ao professor informado ID %d.",
                        gradeId, grade.getProfessor().getId(), professorId
                ));
            }
            return grade;
        }

        List<Grade> grades = gradeRepository.findByProfessorId(professorId);
        if (grades.isEmpty()) {
            throw new RegraNegocioException("O professor informado não possui nenhuma grade horária cadastrada.");
        }

        // Tenta encontrar a grade referente ao semestre do mês
        int semestreEsperado = (mes <= 6) ? 1 : 2;
        return grades.stream()
                .filter(g -> g.getPeriodoLetivo() != null
                        && g.getPeriodoLetivo().getAno().equals(ano)
                        && g.getPeriodoLetivo().getSemestre().equals(semestreEsperado))
                .findFirst()
                .orElse(grades.get(grades.size() - 1)); // Retorna a última grade cadastrada caso não haja do semestre exato
    }
}
