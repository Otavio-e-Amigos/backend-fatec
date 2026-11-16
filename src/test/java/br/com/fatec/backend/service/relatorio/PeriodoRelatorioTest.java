package br.com.fatec.backend.service.relatorio;

import br.com.fatec.backend.enums.TipoPeriodoRelatorio;
import br.com.fatec.backend.exception.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PeriodoRelatorioTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 18); // quarta-feira

    @Test
    @DisplayName("Diário: início e fim no próprio dia, fim exclusivo à meia-noite seguinte")
    void diario() {
        PeriodoRelatorio p = PeriodoRelatorio.de("diario", LocalDate.of(2026, 11, 16), HOJE);
        assertEquals(TipoPeriodoRelatorio.DIARIO, p.tipo());
        assertEquals(LocalDate.of(2026, 11, 16), p.dataInicio());
        assertEquals(LocalDate.of(2026, 11, 16), p.dataFim());
        assertEquals(LocalDateTime.of(2026, 11, 16, 0, 0), p.inicio());
        assertEquals(LocalDateTime.of(2026, 11, 17, 0, 0), p.fimExclusivo());
        assertEquals(1, p.dias().size());
        assertTrue(p.descricao().startsWith("16/11/2026"));
    }

    @Test
    @DisplayName("Semanal: segunda a domingo da semana da data de referência (ISO-8601)")
    void semanal() {
        PeriodoRelatorio p = PeriodoRelatorio.de("SEMANAL", HOJE, HOJE);
        assertEquals(LocalDate.of(2026, 11, 16), p.dataInicio());
        assertEquals(LocalDate.of(2026, 11, 22), p.dataFim());
        assertEquals(7, p.dias().size());
        assertTrue(p.emAndamento(HOJE));
        assertEquals("Semana de 16/11/2026 a 22/11/2026", p.descricao());
    }

    @Test
    @DisplayName("Mensal: do primeiro ao último dia do mês, considerando ano bissexto")
    void mensalFevereiroBissexto() {
        PeriodoRelatorio p = PeriodoRelatorio.de("MENSAL", LocalDate.of(2024, 2, 10), HOJE);
        assertEquals(LocalDate.of(2024, 2, 1), p.dataInicio());
        assertEquals(LocalDate.of(2024, 2, 29), p.dataFim());
        assertEquals(29, p.dias().size());
        assertFalse(p.emAndamento(HOJE));
        assertEquals("Fevereiro de 2024", p.descricao());
    }

    @Test
    @DisplayName("Sem data de referência, usa a data atual")
    void dataPadraoHoje() {
        PeriodoRelatorio p = PeriodoRelatorio.de("MENSAL", null, HOJE);
        assertEquals(LocalDate.of(2026, 11, 1), p.dataInicio());
        assertEquals(LocalDate.of(2026, 11, 30), p.dataFim());
    }

    @Test
    @DisplayName("Rejeita data futura, ano anterior a 2000 e tipo inválido")
    void validacoes() {
        assertThrows(RegraNegocioException.class, () -> PeriodoRelatorio.de("DIARIO", HOJE.plusDays(1), HOJE));
        assertThrows(RegraNegocioException.class, () -> PeriodoRelatorio.de("DIARIO", LocalDate.of(1999, 12, 31), HOJE));
        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> PeriodoRelatorio.de("ANUAL", HOJE, HOJE));
        assertTrue(ex.getMessage().contains("DIARIO, SEMANAL ou MENSAL"));
        assertThrows(RegraNegocioException.class, () -> PeriodoRelatorio.de(" ", HOJE, HOJE));
    }
}
