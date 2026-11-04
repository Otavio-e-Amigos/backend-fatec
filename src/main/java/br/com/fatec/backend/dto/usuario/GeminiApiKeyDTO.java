package br.com.fatec.backend.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Chave da API do Gemini fornecida pelo usuário")
public record GeminiApiKeyDTO(
        @NotBlank(message = "A chave da API não pode estar em branco")
        @Size(min = 10, max = 200, message = "A chave da API deve ter entre 10 e 200 caracteres")
        @Schema(description = "Chave da API do Gemini (obtida em https://aistudio.google.com/)", example = "AIza...")
        String apiKey
) {}
