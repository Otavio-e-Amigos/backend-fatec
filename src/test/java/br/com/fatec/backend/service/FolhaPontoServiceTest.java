package br.com.fatec.backend.service;

import br.com.fatec.backend.document.FolhaPontoDocxService;
import br.com.fatec.backend.dto.folha_ponto.FolhaPontoPreviewDTO;
import br.com.fatec.backend.dto.folha_ponto.FolhaPontoRequisicaoDTO;
import br.com.fatec.backend.entity.*;
import br.com.fatec.backend.enums.RegimeContrato;
import br.com.fatec.backend.enums.RegimeJuridico;
import br.com.fatec.backend.enums.StatusDisciplinaGrade;
import br.com.fatec.backend.enums.Titulacao;
import br.com.fatec.backend.enums.Turno;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.exception.RegraNegocioException;
import br.com.fatec.backend.repository.AulaDaGradeRepository;
import br.com.fatec.backend.repository.GradeRepository;
import br.com.fatec.backend.repository.ProfessorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolhaPontoServiceTest {

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private AulaDaGradeRepository aulaDaGradeRepository;

    @Spy
    private CalendarioAcademicoService calendarioAcademicoService;

    @Mock
    private FolhaPontoDocxService folhaPontoDocxService;

    @InjectMocks
    private FolhaPontoService folhaPontoService;

    private Professor professor;
    private Grade grade;
    private AulaDaGrade aula;

    @BeforeEach
    void setUp() {
        professor = new Professor("Katia Barbosa", "100", "12345678901", "123456",
                RegimeContrato.INDETERMINADO, RegimeJuridico.CLT, Titulacao.MESTRE);
        ReflectionTestUtils.setField(professor, "id", 1L);

        PeriodoLetivo periodo = new PeriodoLetivo(2, 2026, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 12, 15));
        ReflectionTestUtils.setField(periodo, "id", 100L);

        grade = new Grade(professor, periodo, new BigDecimal("20.00"), new BigDecimal("80.00"), new BigDecimal("400.00"));
        ReflectionTestUtils.setField(grade, "id", 10L);

        Curso curso = new Curso("ADS", Turno.MANHA, "ADS");
        Disciplina disciplina = new Disciplina("BD001", "BD", "Banco de Dados");
        DisciplinaDaGrade dg = new DisciplinaDaGrade(grade, disciplina, curso, (short) 1, "MANHA", (short) 4, 1, StatusDisciplinaGrade.MINISTRANDO);

        aula = new AulaDaGrade(dg, (short) 1, LocalTime.of(8, 0), LocalTime.of(8, 50));
    }

    @Test
    @DisplayName("Deve montar a prévia com sucesso")
    void deveMontarPreviewComSucesso() {
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(gradeRepository.findById(10L)).thenReturn(Optional.of(grade));
        when(aulaDaGradeRepository.findByGradeIdComDetalhes(10L)).thenReturn(List.of(aula));

        FolhaPontoRequisicaoDTO req = new FolhaPontoRequisicaoDTO(1L, 10L, 9, 2026);
        FolhaPontoPreviewDTO preview = folhaPontoService.montarPreview(req);

        assertNotNull(preview);
        assertEquals("FOLHA DE FREQUÊNCIA – 09/2026", preview.titulo());
        assertEquals("Katia Barbosa", preview.professor().nome());
        assertEquals("123456", preview.professor().matricula());
        assertEquals("CLT", preview.professor().regimeJuridico());

        // Grade horária mapeada
        assertEquals(1, preview.gradeHoraria().size());
        assertEquals("BD", preview.gradeHoraria().get(0).sigla());
        assertEquals((short) 1, preview.gradeHoraria().get(0).diaSemana());
        assertEquals(2, preview.gradeHoraria().get(0).slot());

        // Calendário do mês
        assertEquals(30, preview.dias().size());
        // 06/09 é domingo
        assertTrue(preview.dias().get(5).domingo());
        // 07/09 é feriado sem aula
        assertTrue(preview.dias().get(6).semAula());
        assertEquals("Feriado - Independência do Brasil", preview.dias().get(6).motivo());
    }

    @Test
    @DisplayName("Deve chamar o serviço de DOCX e gerar a folha com sucesso")
    void deveGerarFolhaPontoComSucesso() {
        byte[] docxFake = new byte[]{80, 75, 3, 4};
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(gradeRepository.findById(10L)).thenReturn(Optional.of(grade));
        when(aulaDaGradeRepository.findByGradeIdComDetalhes(10L)).thenReturn(List.of(aula));
        when(folhaPontoDocxService.gerarDocumento(eq(professor), eq(grade), any(), eq(9), eq(2026)))
                .thenReturn(docxFake);

        FolhaPontoRequisicaoDTO req = new FolhaPontoRequisicaoDTO(1L, 10L, 9, 2026);
        byte[] resultado = folhaPontoService.gerarFolhaPonto(req);

        assertNotNull(resultado);
        assertArrayEquals(docxFake, resultado);
        verify(folhaPontoDocxService).gerarDocumento(eq(professor), eq(grade), any(), eq(9), eq(2026));
    }

    @Test
    @DisplayName("Deve lançar exceção se a grade pertencer a outro professor")
    void deveLancarExcecaoSeGradePertencerAOutroProfessor() {
        Professor outroProfessor = new Professor("Outro", "200", "98765432100", "654321",
                RegimeContrato.DETERMINADO, RegimeJuridico.CLT, Titulacao.DOUTOR);
        ReflectionTestUtils.setField(outroProfessor, "id", 2L);

        Grade gradeDeOutro = new Grade(outroProfessor, grade.getPeriodoLetivo(), null, null, null);
        ReflectionTestUtils.setField(gradeDeOutro, "id", 99L);

        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(gradeRepository.findById(99L)).thenReturn(Optional.of(gradeDeOutro));

        FolhaPontoRequisicaoDTO req = new FolhaPontoRequisicaoDTO(1L, 99L, 9, 2026);

        assertThrows(RegraNegocioException.class, () -> folhaPontoService.montarPreview(req));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o mês for inválido")
    void deveLancarExcecaoQuandoMesInvalido() {
        FolhaPontoRequisicaoDTO req = new FolhaPontoRequisicaoDTO(1L, 10L, 13, 2026);
        assertThrows(RegraNegocioException.class, () -> folhaPontoService.montarPreview(req));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o professor não for encontrado")
    void deveLancarExcecaoQuandoProfessorNaoEncontrado() {
        when(professorRepository.findById(999L)).thenReturn(Optional.empty());
        FolhaPontoRequisicaoDTO req = new FolhaPontoRequisicaoDTO(999L, 10L, 9, 2026);

        assertThrows(RecursoNaoEncontradoException.class, () -> folhaPontoService.montarPreview(req));
    }
}
