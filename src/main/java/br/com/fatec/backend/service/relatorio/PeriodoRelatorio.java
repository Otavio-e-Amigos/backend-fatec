package br.com.fatec.backend.service.relatorio;

import br.com.fatec.backend.enums.TipoPeriodoRelatorio;
import br.com.fatec.backend.exception.RegraNegocioException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Intervalo fechado de datas [dataInicio, dataFim] de um relatório, calculado a partir do tipo e da
 * data de referência. A semana segue o padrão ISO-8601 (segunda a domingo).
 */
public record PeriodoRelatorio(TipoPeriodoRelatorio tipo, LocalDate dataReferencia,
                               LocalDate dataInicio, LocalDate dataFim) {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int ANO_MINIMO = 2000;

    /**
     * @param tipoTexto      DIARIO, SEMANAL ou MENSAL (sem diferenciar maiúsculas)
     * @param dataReferencia qualquer data dentro do período desejado; quando nula, usa hoje
     * @param hoje           data atual (injetada para permitir testes)
     */
    public static PeriodoRelatorio de(String tipoTexto, LocalDate dataReferencia, LocalDate hoje) {
        TipoPeriodoRelatorio tipo = TipoPeriodoRelatorio.de(tipoTexto);
        LocalDate referencia = dataReferencia != null ? dataReferencia : hoje;

        if (referencia.isAfter(hoje)) {
            throw new RegraNegocioException("A data de referência não pode ser futura.");
        }
        if (referencia.getYear() < ANO_MINIMO) {
            throw new RegraNegocioException("A data de referência deve ser a partir do ano " + ANO_MINIMO + ".");
        }

        return switch (tipo) {
            case DIARIO -> new PeriodoRelatorio(tipo, referencia, referencia, referencia);
            case SEMANAL -> {
                LocalDate segunda = referencia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                yield new PeriodoRelatorio(tipo, referencia, segunda, segunda.plusDays(6));
            }
            case MENSAL -> new PeriodoRelatorio(tipo, referencia,
                    referencia.withDayOfMonth(1), referencia.with(TemporalAdjusters.lastDayOfMonth()));
        };
    }

    /** Início inclusivo (00:00 do primeiro dia). */
    public LocalDateTime inicio() {
        return dataInicio.atStartOfDay();
    }

    /** Fim exclusivo (00:00 do dia seguinte ao último dia). */
    public LocalDateTime fimExclusivo() {
        return dataFim.plusDays(1).atStartOfDay();
    }

    /** Indica se o período ainda não terminou (dados parciais). */
    public boolean emAndamento(LocalDate hoje) {
        return !dataFim.isBefore(hoje);
    }

    public List<LocalDate> dias() {
        List<LocalDate> dias = new ArrayList<>();
        for (LocalDate d = dataInicio; !d.isAfter(dataFim); d = d.plusDays(1)) {
            dias.add(d);
        }
        return dias;
    }

    public String descricao() {
        return switch (tipo) {
            case DIARIO -> DATA.format(dataInicio) + " (" + dataInicio.getDayOfWeek().getDisplayName(TextStyle.FULL, PT_BR) + ")";
            case SEMANAL -> "Semana de " + DATA.format(dataInicio) + " a " + DATA.format(dataFim);
            case MENSAL -> {
                String mes = dataInicio.getMonth().getDisplayName(TextStyle.FULL, PT_BR);
                yield Character.toUpperCase(mes.charAt(0)) + mes.substring(1) + " de " + dataInicio.getYear();
            }
        };
    }
}
