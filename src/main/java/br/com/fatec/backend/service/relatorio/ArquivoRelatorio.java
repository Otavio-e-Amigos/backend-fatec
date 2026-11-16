package br.com.fatec.backend.service.relatorio;

/** Documento exportado pronto para download. */
public record ArquivoRelatorio(byte[] conteudo, String nomeArquivo, String mediaType) {
}
