package br.com.fatec.backend.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Encapsula todas as respostas da API em um formato padrão.
 * @param <T> O tipo de dado retornado (ex: UsuarioResponseDTO, List<UsuarioResponseDTO>, null, etc.)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RespostaPadraoDTO<T>(
        String mensagem,
        int status,
        T dados
) {
    public static <T> RespostaPadraoDTO<T> sucesso(String mensagem, T dados) {
        return new RespostaPadraoDTO<>(mensagem, 200, dados);
    }

    public static <T> RespostaPadraoDTO<T> sucesso(String mensagem) {
        return new RespostaPadraoDTO<>(mensagem, 200, null);
    }

    public static <T> RespostaPadraoDTO<T> criado(String mensagem, T dados) {
        return new RespostaPadraoDTO<>(mensagem, 201, dados);
    }

    public static <T> RespostaPadraoDTO<T> erro(String mensagem, int status) {
        return new RespostaPadraoDTO<>(mensagem, status, null);
    }
}