package br.com.fatec.backend.service;

import br.com.fatec.backend.entity.*;
import br.com.fatec.backend.enums.*;
import br.com.fatec.backend.repository.GradeRepository;
import br.com.fatec.backend.repository.ProfessorRepository;
import br.com.fatec.backend.repository.RegistroFolhaPontoRepository;
import br.com.fatec.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoricoFolhaPontoServiceTest {

    @Mock private RegistroFolhaPontoRepository registroRepository;
    @Mock private ProfessorRepository professorRepository;
    @Mock private GradeRepository gradeRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private NotificacaoService notificacaoService;
    @Mock private UsuarioAutenticadoService usuarioAutenticadoService;
    @Mock private PlatformTransactionManager transactionManager;

    private HistoricoFolhaPontoService service;
    private Professor professor;
    private List<AulaDaGrade> aulas;
    private PeriodoLetivo periodo;

    @BeforeEach
    void setUp() {
        service = new HistoricoFolhaPontoService(registroRepository, professorRepository, gradeRepository,
                usuarioRepository, notificacaoService, new CalendarioAcademicoService(),
                usuarioAutenticadoService, transactionManager);

        professor = new Professor("Katia", "100", "12345678901", "M1",
                RegimeContrato.INDETERMINADO, RegimeJuridico.CLT, Titulacao.MESTRE);
        ReflectionTestUtils.setField(professor, "id", 1L);
        periodo = new PeriodoLetivo(2, 2026, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 12, 15));

        Grade grade = new Grade(professor, periodo, BigDecimal.TEN, null, null);
        DisciplinaDaGrade dg = new DisciplinaDaGrade(grade, new Disciplina("ES3", "ESIII", "Eng. Software"),
                new Curso("ADS", Turno.NOITE, "ADS"), (short) 4, "NOITE", (short) 4, 1, StatusDisciplinaGrade.MINISTRANDO);
        ReflectionTestUtils.setField(dg, "id", 30L);
        aulas = List.of(new AulaDaGrade(dg, (short) 1, LocalTime.of(19, 0), LocalTime.of(20, 40)),
                new AulaDaGrade(dg, (short) 3, LocalTime.of(19, 0), LocalTime.of(20, 40)));

        when(usuarioAutenticadoService.idUsuarioAtual()).thenReturn(Optional.of(5L));
        when(professorRepository.getReferenceById(1L)).thenReturn(professor);
        lenient().when(registroRepository.save(any(RegistroFolhaPonto.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Grade gradeComId(PeriodoLetivo p) {
        Grade g = new Grade(professor, p, BigDecimal.TEN, null, null);
        ReflectionTestUtils.setField(g, "id", 10L);
        when(gradeRepository.getReferenceById(10L)).thenReturn(g);
        return g;
    }

    @Test
    @DisplayName("Registra a geração com dados do período e notifica o usuário")
    void registraGeracao() {
        service.registrarGeracao(professor, gradeComId(periodo), aulas, 11, 2026);

        ArgumentCaptor<RegistroFolhaPonto> captor = ArgumentCaptor.forClass(RegistroFolhaPonto.class);
        verify(registroRepository).save(captor.capture());
        RegistroFolhaPonto r = captor.getValue();
        assertEquals((short) 11, r.getMes());
        assertEquals(2026, r.getAno());
        assertFalse(r.isGradeOutroPeriodo());
        assertEquals((short) 1, r.getQuantidadeDisciplinas());
        // Novembro/2026 no calendário em memória: 02, 20 e 21 (15/11 cai no domingo e não conta)
        assertEquals((short) 3, r.getDiasSemAula());

        verify(notificacaoService).notificarUsuario(eq(5L), eq(TipoNotificacao.FOLHA_PONTO_GERADA),
                anyString(), contains("11/2026"), eq(HistoricoFolhaPontoService.REFERENCIA_TIPO), any());
    }

    @Test
    @DisplayName("Grade de outro período gera notificação com aviso")
    void gradeDeOutroPeriodo() {
        PeriodoLetivo primeiroSemestre = new PeriodoLetivo(1, 2026, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30));
        service.registrarGeracao(professor, gradeComId(primeiroSemestre), aulas, 11, 2026);

        verify(notificacaoService).notificarUsuario(eq(5L), eq(TipoNotificacao.FOLHA_PONTO_GERADA_COM_AVISOS),
                anyString(), contains("outro período"), anyString(), any());
    }

    @Test
    @DisplayName("Falha ao gravar o histórico não interrompe a geração da folha")
    void falhaNaoPropaga() {
        when(registroRepository.save(any(RegistroFolhaPonto.class))).thenThrow(new IllegalStateException("banco fora"));
        assertDoesNotThrow(() -> service.registrarGeracao(professor, gradeComId(periodo), aulas, 11, 2026));
        verifyNoInteractions(notificacaoService);
    }
}
