package br.com.fatec.backend.service;

import br.com.fatec.backend.entity.AulaDaGrade;
import br.com.fatec.backend.entity.Grade;
import br.com.fatec.backend.entity.PeriodoLetivo;
import br.com.fatec.backend.entity.Professor;
import br.com.fatec.backend.entity.RegistroFolhaPonto;
import br.com.fatec.backend.enums.TipoNotificacao;
import br.com.fatec.backend.repository.GradeRepository;
import br.com.fatec.backend.repository.ProfessorRepository;
import br.com.fatec.backend.repository.RegistroFolhaPontoRepository;
import br.com.fatec.backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/**
 * Registra cada folha de ponto gerada (histórico usado pelos relatórios) e notifica o usuário.
 * <p>
 * A geração da folha roda em transação somente leitura; por isso o registro usa uma transação
 * própria (REQUIRES_NEW). Falhas no registro são apenas logadas e não impedem o download do DOCX.
 */
@Service
public class HistoricoFolhaPontoService {

    public static final String REFERENCIA_TIPO = "REGISTRO_FOLHA_PONTO";

    private static final Logger log = LoggerFactory.getLogger(HistoricoFolhaPontoService.class);

    private final RegistroFolhaPontoRepository registroRepository;
    private final ProfessorRepository professorRepository;
    private final GradeRepository gradeRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;
    private final CalendarioAcademicoService calendarioAcademicoService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final TransactionTemplate transacaoIndependente;

    public HistoricoFolhaPontoService(RegistroFolhaPontoRepository registroRepository,
                                      ProfessorRepository professorRepository,
                                      GradeRepository gradeRepository,
                                      UsuarioRepository usuarioRepository,
                                      NotificacaoService notificacaoService,
                                      CalendarioAcademicoService calendarioAcademicoService,
                                      UsuarioAutenticadoService usuarioAutenticadoService,
                                      PlatformTransactionManager transactionManager) {
        this.registroRepository = registroRepository;
        this.professorRepository = professorRepository;
        this.gradeRepository = gradeRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
        this.calendarioAcademicoService = calendarioAcademicoService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.transacaoIndependente = new TransactionTemplate(transactionManager);
        this.transacaoIndependente.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void registrarGeracao(Professor professor, Grade grade, List<AulaDaGrade> aulas, int mes, int ano) {
        try {
            // Dados calculados ainda dentro da transação de leitura de quem chamou (acesso lazy seguro).
            PeriodoLetivo periodo = grade.getPeriodoLetivo();
            Integer periodoAno = periodo != null ? periodo.getAno() : null;
            Integer periodoSemestre = periodo != null ? periodo.getSemestre() : null;
            int semestreEsperado = mes <= 6 ? 1 : 2;
            boolean gradeOutroPeriodo = periodoAno == null
                    || periodoAno != ano
                    || periodoSemestre == null
                    || periodoSemestre != semestreEsperado;
            int diasSemAula = contarDiasSemAula(mes, ano);
            int quantidadeDisciplinas = (int) aulas.stream()
                    .map(AulaDaGrade::getDisciplinaDaGrade)
                    .filter(Objects::nonNull)
                    .map(dg -> dg.getId())
                    .distinct()
                    .count();

            Long usuarioId = usuarioAutenticadoService.idUsuarioAtual().orElse(null);
            Long professorId = professor.getId();
            Long gradeId = grade.getId();
            String nomeProfessor = professor.getNome();

            transacaoIndependente.executeWithoutResult(status -> {
                RegistroFolhaPonto registro = new RegistroFolhaPonto(
                        professorRepository.getReferenceById(professorId),
                        gradeId != null ? gradeRepository.getReferenceById(gradeId) : null,
                        usuarioId != null ? usuarioRepository.getReferenceById(usuarioId) : null,
                        mes, ano, periodoAno, periodoSemestre, gradeOutroPeriodo, diasSemAula, quantidadeDisciplinas);
                registroRepository.save(registro);

                notificacaoService.notificarUsuario(usuarioId,
                        gradeOutroPeriodo ? TipoNotificacao.FOLHA_PONTO_GERADA_COM_AVISOS : TipoNotificacao.FOLHA_PONTO_GERADA,
                        gradeOutroPeriodo ? "Folha de ponto gerada com avisos" : "Folha de ponto gerada",
                        montarMensagem(nomeProfessor, mes, ano, periodoAno, periodoSemestre, gradeOutroPeriodo, diasSemAula),
                        REFERENCIA_TIPO, registro.getId());
            });
        } catch (RuntimeException e) {
            log.warn("Não foi possível registrar o histórico da folha de ponto: {}", e.getClass().getSimpleName());
        }
    }

    private int contarDiasSemAula(int mes, int ano) {
        int dias = 0;
        YearMonth competencia = YearMonth.of(ano, mes);
        for (int d = 1; d <= competencia.lengthOfMonth(); d++) {
            LocalDate data = competencia.atDay(d);
            if (data.getDayOfWeek() != DayOfWeek.SUNDAY && calendarioAcademicoService.isDiaSemAula(data)) {
                dias++;
            }
        }
        return dias;
    }

    private static String montarMensagem(String nomeProfessor, int mes, int ano, Integer periodoAno,
                                         Integer periodoSemestre, boolean gradeOutroPeriodo, int diasSemAula) {
        StringBuilder sb = new StringBuilder(String.format(
                "Folha de frequência de %s referente a %02d/%d gerada com sucesso", nomeProfessor, mes, ano));
        if (periodoAno != null && periodoSemestre != null) {
            sb.append(String.format(" (grade do %dº semestre de %d)", periodoSemestre, periodoAno));
        }
        sb.append('.');
        if (gradeOutroPeriodo) {
            sb.append(" Atenção: não há grade cadastrada para o semestre desta competência; foi utilizada a grade de outro período. Confira os horários.");
        }
        if (diasSemAula > 0) {
            sb.append(String.format(" O mês possui %d dia(s) sem aula no calendário acadêmico.", diasSemAula));
        }
        return sb.toString();
    }
}
