package br.com.fatec.backend.service;

import br.com.fatec.backend.entity.Grade;
import br.com.fatec.backend.exception.ConflitoException;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.repository.DisciplinaDaGradeRepository;
import br.com.fatec.backend.repository.GradeRepository;
import br.com.fatec.backend.repository.PeriodoLetivoRepository;
import br.com.fatec.backend.repository.ProfessorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private GradeRepository repository;

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private PeriodoLetivoRepository periodoLetivoRepository;

    @Mock
    private DisciplinaDaGradeRepository disciplinaDaGradeRepository;

    @InjectMocks
    private GradeService gradeService;

    @Test
    @DisplayName("Deve excluir grade com sucesso quando não houver disciplinas associadas")
    void deveExcluirGradeComSucesso() {
        Long id = 1L;
        Grade grade = mock(Grade.class);

        when(repository.findById(id)).thenReturn(Optional.of(grade));
        when(disciplinaDaGradeRepository.existsByGradeId(id)).thenReturn(false);

        gradeService.excluir(id);

        verify(repository).delete(grade);
    }

    @Test
    @DisplayName("Deve lançar ConflitoException ao tentar excluir grade com disciplinas associadas")
    void deveLancarConflitoAoExcluirGradeComDisciplinas() {
        Long id = 1L;
        Grade grade = mock(Grade.class);

        when(repository.findById(id)).thenReturn(Optional.of(grade));
        when(disciplinaDaGradeRepository.existsByGradeId(id)).thenReturn(true);

        assertThrows(ConflitoException.class, () -> gradeService.excluir(id));
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao tentar excluir grade inexistente")
    void deveLancarRecursoNaoEncontradoAoExcluirGradeInexistente() {
        Long id = 999L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> gradeService.excluir(id));
        verify(repository, never()).delete(any());
    }
}
