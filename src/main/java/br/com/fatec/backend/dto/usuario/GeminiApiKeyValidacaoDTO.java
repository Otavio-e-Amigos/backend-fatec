package br.com.fatec.backend.dto.usuario;

public record GeminiApiKeyValidacaoDTO(
        boolean valida,
        String mensagem,
        String modelo
) {}
