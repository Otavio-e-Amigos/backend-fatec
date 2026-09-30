package br.com.fatec.backend.dto.common;

public record MensagemResponseDTO(
        String mensagem,
        int status
) { }