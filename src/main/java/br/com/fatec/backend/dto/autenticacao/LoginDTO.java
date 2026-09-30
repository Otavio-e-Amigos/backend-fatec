package br.com.fatec.backend.dto.autenticacao;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank(message = "O login é obrigatório")
        String login,

        @NotBlank(message = "A senha é obrigatório")
        String senha
) { }
