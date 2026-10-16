package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.periodo_letivo.PeriodoLetivoRequisicaoDTO;
import br.com.fatec.backend.dto.periodo_letivo.PeriodoLetivoRespostaDTO;
import br.com.fatec.backend.entity.PeriodoLetivo;
import br.com.fatec.backend.exception.ConflitoException;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.exception.RegraNegocioException;
import br.com.fatec.backend.repository.PeriodoLetivoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeriodoLetivoServiceTest {

    @Mock
    private PeriodoLetivoRepository repository;

    @InjectMocks
    private PeriodoLetivoService service;

    @Test
    @DisplayName("Deve atualizar período letivo com sucesso")
    void deveAtualizarPeriodoLetivoComSucesso() {
        Long id = 1L;
        PeriodoLetivo periodo = new PeriodoLetivo(1, 2026, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30));

        PeriodoLetivoRequisicaoDTO dto = new PeriodoLetivoRequisicaoDTO(
                2,
                2026,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 12, 15)
        );

        when(repository.findById(id)).thenReturn(Optional.of(periodo));
        when(repository.existsByAnoAndSemestreAndIdNot(2026, 2, id)).thenReturn(false);

        PeriodoLetivoRespostaDTO resposta = service.atualizar(id, dto);

        assertNotNull(resposta);
        assertEquals(2, resposta.semestre());
        assertEquals(2026, resposta.ano());
        assertEquals(LocalDate.of(2026, 8, 1), resposta.dataInicio());
        assertEquals(LocalDate.of(2026, 12, 15), resposta.dataFim());
    }

    @Test
    @DisplayName("Deve lançar ConflitoException quando ano e semestre já existirem em outro registro")
    void deveLancarConflitoQuandoAnoSemestreDuplicado() {
        Long id = 1L;
        PeriodoLetivo periodo = new PeriodoLetivo(1, 2026, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30));

        PeriodoLetivoRequisicaoDTO dto = new PeriodoLetivoRequisicaoDTO(
                2,
                2026,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 12, 15)
        );

        when(repository.findById(id)).thenReturn(Optional.of(periodo));
        when(repository.existsByAnoAndSemestreAndIdNot(2026, 2, id)).thenReturn(true);

        assertThrows(ConflitoException.class, () -> service.atualizar(id, dto));
    }

    @Test
    @DisplayName("Deve lançar RegraNegocioException quando dataInicio não for anterior a dataFim")
    void deveLancarRegraNegocioQuandoDatasInvalidas() {
        Long id = 1L;
        PeriodoLetivo periodo = new PeriodoLetivo(1, 2026, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30));

        PeriodoLetivoRequisicaoDTO dto = new PeriodoLetivoRequisicaoDTO(
                1,
                2026,
                LocalDate.of(2026, 12, 15),
                LocalDate.of(2026, 8, 1)
        );

        when(repository.findById(id)).thenReturn(Optional.of(periodo));
        when(repository.existsByAnoAndSemestreAndIdNot(2026, 1, id)).thenReturn(false);

        assertThrows(RegraNegocioException.class, () -> service.atualizar(id, dto));
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException quando período letivo não for encontrado")
    void deveLancarNaoEncontradoQuandoPeriodoNaoExistir() {
        Long id = 999L;
        PeriodoLetivoRequisicaoDTO dto = new PeriodoLetivoRequisicaoDTO(
                1,
                2026,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 6, 30)
        );

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.atualizar(id, dto));
    }
}
