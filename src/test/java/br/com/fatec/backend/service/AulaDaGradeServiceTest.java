package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.aula_da_grade.AulaDaGradeRequisicaoDTO;
import br.com.fatec.backend.dto.aula_da_grade.AulaDaGradeRespostaDTO;
import br.com.fatec.backend.entity.AulaDaGrade;
import br.com.fatec.backend.entity.DisciplinaDaGrade;
import br.com.fatec.backend.entity.Grade;
import br.com.fatec.backend.exception.ConflitoException;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.exception.RegraNegocioException;
import br.com.fatec.backend.repository.AulaDaGradeRepository;
import br.com.fatec.backend.repository.DisciplinaDaGradeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AulaDaGradeServiceTest {

    @Mock
    private AulaDaGradeRepository aulaRepository;

    @Mock
    private DisciplinaDaGradeRepository disciplinaDaGradeRepository;

    @InjectMocks
    private AulaDaGradeService aulaDaGradeService;

    @Test
    @DisplayName("Deve atualizar aula da grade com sucesso")
    void deveAtualizarAulaComSucesso() {
        Long aulaId = 1L;
        Long gradeId = 10L;

        Grade grade = mock(Grade.class);
        when(grade.getId()).thenReturn(gradeId);

        DisciplinaDaGrade dg = mock(DisciplinaDaGrade.class);
        when(dg.getGrade()).thenReturn(grade);

        AulaDaGrade aula = new AulaDaGrade(dg, (short) 2, LocalTime.of(8, 0), LocalTime.of(10, 0));
        ReflectionTestUtils.setField(aula, "id", aulaId);

        AulaDaGradeRequisicaoDTO dto = new AulaDaGradeRequisicaoDTO(
                (short) 2,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0)
        );

        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aula));
        when(aulaRepository.findByGradeIdComDetalhes(gradeId)).thenReturn(List.of(aula));

        AulaDaGradeRespostaDTO resposta = aulaDaGradeService.atualizar(aulaId, dto);

        assertNotNull(resposta);
        assertEquals((short) 2, resposta.diaSemana());
        assertEquals(LocalTime.of(10, 0), resposta.horaInicio());
        assertEquals(LocalTime.of(12, 0), resposta.horaFim());
    }

    @Test
    @DisplayName("Deve lançar RegraNegocioException quando horaFim não for posterior a horaInicio na atualização")
    void deveLancarRegraNegocioQuandoHoraFimInvalida() {
        Long aulaId = 1L;
        AulaDaGrade aula = mock(AulaDaGrade.class);

        AulaDaGradeRequisicaoDTO dto = new AulaDaGradeRequisicaoDTO(
                (short) 2,
                LocalTime.of(12, 0),
                LocalTime.of(10, 0)
        );

        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aula));

        assertThrows(RegraNegocioException.class, () -> aulaDaGradeService.atualizar(aulaId, dto));
    }

    @Test
    @DisplayName("Deve lançar ConflitoException quando houver sobreposição de horários com outra aula da mesma grade")
    void deveLancarConflitoQuandoHouverSobreposicao() {
        Long aulaId = 1L;
        Long outraAulaId = 2L;
        Long gradeId = 10L;

        Grade grade = mock(Grade.class);
        when(grade.getId()).thenReturn(gradeId);

        DisciplinaDaGrade dg = mock(DisciplinaDaGrade.class);
        when(dg.getGrade()).thenReturn(grade);

        AulaDaGrade aula = new AulaDaGrade(dg, (short) 2, LocalTime.of(8, 0), LocalTime.of(10, 0));
        ReflectionTestUtils.setField(aula, "id", aulaId);

        AulaDaGrade outraAula = new AulaDaGrade(dg, (short) 2, LocalTime.of(10, 0), LocalTime.of(12, 0));
        ReflectionTestUtils.setField(outraAula, "id", outraAulaId);

        AulaDaGradeRequisicaoDTO dto = new AulaDaGradeRequisicaoDTO(
                (short) 2,
                LocalTime.of(11, 0),
                LocalTime.of(13, 0)
        );

        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aula));
        when(aulaRepository.findByGradeIdComDetalhes(gradeId)).thenReturn(List.of(aula, outraAula));

        assertThrows(ConflitoException.class, () -> aulaDaGradeService.atualizar(aulaId, dto));
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException quando aula não existir na atualização")
    void deveLancarNaoEncontradoQuandoAulaNaoExistir() {
        Long aulaId = 999L;
        AulaDaGradeRequisicaoDTO dto = new AulaDaGradeRequisicaoDTO(
                (short) 2,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0)
        );

        when(aulaRepository.findById(aulaId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> aulaDaGradeService.atualizar(aulaId, dto));
    }
}
