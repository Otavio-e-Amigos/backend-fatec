package br.com.fatec.backend.enums;

import br.com.fatec.backend.exception.RegraNegocioException;

import java.util.Locale;

/** Recorte temporal aceito pelos relatórios gerenciais. */
public enum TipoPeriodoRelatorio {
    DIARIO("Diário"),
    SEMANAL("Semanal"),
    MENSAL("Mensal");

    private final String rotulo;

    TipoPeriodoRelatorio(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    /** Converte o texto recebido na requisição, com mensagem amigável em caso de valor inválido. */
    public static TipoPeriodoRelatorio de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("Informe o período do relatório: DIARIO, SEMANAL ou MENSAL.");
        }
        try {
            return valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException("Período inválido: '" + valor + "'. Valores aceitos: DIARIO, SEMANAL ou MENSAL.");
        }
    }
}
