package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.extracao_grade.GradeExtraidaDTO;
import br.com.fatec.backend.entity.Professor;
import br.com.fatec.backend.entity.RegistroExtracaoGrade;
import br.com.fatec.backend.entity.Usuario;
import br.com.fatec.backend.enums.StatusExtracaoGrade;
import br.com.fatec.backend.enums.TipoNotificacao;
import br.com.fatec.backend.repository.ProfessorRepository;
import br.com.fatec.backend.repository.RegistroExtracaoGradeRepository;
import br.com.fatec.backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Registra o histórico das extrações de grade e dispara as notificações correspondentes.
 * <p>
 * O registro roda em transação própria (REQUIRES_NEW) e qualquer falha aqui é apenas registrada em log:
 * o histórico nunca pode quebrar a extração em si. O CPF extraído não é persistido.
 */
@Service
public class HistoricoExtracaoGradeService {

    public static final String REFERENCIA_TIPO = "REGISTRO_EXTRACAO_GRADE";

    private static final Logger log = LoggerFactory.getLogger(HistoricoExtracaoGradeService.class);

    private final RegistroExtracaoGradeRepository registroRepository;
    private final ProfessorRepository professorRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;
    private final TransactionTemplate transacaoIndependente;

    public HistoricoExtracaoGradeService(RegistroExtracaoGradeRepository registroRepository,
                                         ProfessorRepository professorRepository,
                                         UsuarioRepository usuarioRepository,
                                         NotificacaoService notificacaoService,
                                         PlatformTransactionManager transactionManager) {
        this.registroRepository = registroRepository;
        this.professorRepository = professorRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
        this.transacaoIndependente = new TransactionTemplate(transactionManager);
        this.transacaoIndependente.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void registrarSucesso(Long usuarioId, String nomeArquivo, Long tamanhoBytes,
                                 GradeExtraidaDTO dados, long duracaoMs) {
        try {
            transacaoIndependente.executeWithoutResult(status -> {
                int avisos = dados.avisos().size();
                StatusExtracaoGrade resultado = avisos > 0 ? StatusExtracaoGrade.SUCESSO_COM_AVISOS : StatusExtracaoGrade.SUCESSO;

                RegistroExtracaoGrade registro = new RegistroExtracaoGrade(
                        referenciaUsuario(usuarioId), limitar(nomeArquivo, 255), tamanhoBytes, resultado, duracaoMs);

                String nomeProfessor = dados.professor() != null ? limitar(dados.professor().nome(), 150) : null;
                String matricula = dados.professor() != null ? limitar(apararOuNulo(dados.professor().matricula()), 20) : null;
                Professor cadastrado = matricula != null ? professorRepository.findByMatricula(matricula).orElse(null) : null;
                int aulas = dados.disciplinas().stream().mapToInt(d -> d.aulas().size()).sum();

                registro.preencherResultado(cadastrado, nomeProfessor, matricula,
                        dados.semestre() != null ? dados.semestre().shortValue() : null, dados.ano(),
                        dados.disciplinas().size(), aulas, avisos);
                registroRepository.save(registro);

                notificacaoService.notificarUsuario(usuarioId,
                        avisos > 0 ? TipoNotificacao.EXTRACAO_GRADE_COM_AVISOS : TipoNotificacao.EXTRACAO_GRADE_CONCLUIDA,
                        avisos > 0 ? "Extração de grade concluída com avisos" : "Extração de grade concluída",
                        montarMensagemSucesso(nomeArquivo, nomeProfessor, dados, aulas, avisos),
                        REFERENCIA_TIPO, registro.getId());
            });
        } catch (RuntimeException e) {
            log.warn("Não foi possível registrar o histórico da extração de grade: {}", e.getClass().getSimpleName());
        }
    }

    public void registrarFalha(Long usuarioId, String nomeArquivo, Long tamanhoBytes,
                               String motivo, long duracaoMs) {
        try {
            transacaoIndependente.executeWithoutResult(status -> {
                RegistroExtracaoGrade registro = new RegistroExtracaoGrade(
                        referenciaUsuario(usuarioId), limitar(nomeArquivo, 255), tamanhoBytes,
                        StatusExtracaoGrade.FALHA, duracaoMs);
                String motivoLimitado = limitar(motivo != null ? motivo : "Erro não identificado.", 500);
                registro.registrarErro(motivoLimitado);
                registroRepository.save(registro);

                notificacaoService.notificarUsuario(usuarioId, TipoNotificacao.EXTRACAO_GRADE_FALHA,
                        "Falha na extração de grade",
                        String.format("Não foi possível extrair a grade do arquivo \"%s\". Motivo: %s",
                                nomeOuPadrao(nomeArquivo), motivoLimitado),
                        REFERENCIA_TIPO, registro.getId());
            });
        } catch (RuntimeException e) {
            log.warn("Não foi possível registrar a falha da extração de grade: {}", e.getClass().getSimpleName());
        }
    }

    private String montarMensagemSucesso(String nomeArquivo, String nomeProfessor, GradeExtraidaDTO dados,
                                         int aulas, int avisos) {
        StringBuilder sb = new StringBuilder("A grade");
        if (nomeProfessor != null && !nomeProfessor.isBlank()) {
            sb.append(" de ").append(nomeProfessor);
        }
        if (dados.semestre() != null && dados.ano() != null) {
            sb.append(" (").append(dados.semestre()).append("º semestre de ").append(dados.ano()).append(')');
        }
        sb.append(" foi extraída do arquivo \"").append(nomeOuPadrao(nomeArquivo)).append("\": ")
                .append(dados.disciplinas().size()).append(" disciplina(s) e ")
                .append(aulas).append(" horário(s) de aula.");
        if (avisos > 0) {
            sb.append(' ').append(avisos).append(" aviso(s) precisam de revisão antes da validação.");
        }
        return sb.toString();
    }

    private Usuario referenciaUsuario(Long usuarioId) {
        return usuarioId != null ? usuarioRepository.getReferenceById(usuarioId) : null;
    }

    private static String nomeOuPadrao(String nomeArquivo) {
        return nomeArquivo == null || nomeArquivo.isBlank() ? "sem nome" : nomeArquivo;
    }

    private static String apararOuNulo(String valor) {
        if (valor == null) {
            return null;
        }
        String aparado = valor.trim();
        return aparado.isEmpty() ? null : aparado;
    }

    private static String limitar(String valor, int maximo) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= maximo ? valor : valor.substring(0, maximo);
    }
}
