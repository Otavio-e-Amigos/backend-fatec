package br.com.fatec.backend.enums;

import br.com.fatec.backend.exception.RegraNegocioException;

import java.util.Locale;

/** Formatos de exportação dos relatórios. */
public enum FormatoRelatorio {
    PDF("application/pdf", "pdf"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");

    private final String mediaType;
    private final String extensao;

    FormatoRelatorio(String mediaType, String extensao) {
        this.mediaType = mediaType;
        this.extensao = extensao;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getExtensao() {
        return extensao;
    }

    public static FormatoRelatorio de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("Informe o formato de exportação: PDF ou DOCX.");
        }
        try {
            return valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException("Formato inválido: '" + valor + "'. Valores aceitos: PDF ou DOCX.");
        }
    }
}
