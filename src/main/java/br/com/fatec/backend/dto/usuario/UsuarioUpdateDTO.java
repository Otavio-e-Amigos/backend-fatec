package br.com.fatec.backend.dto.usuario;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para atualização de dados básicos. Não inclui senha nem perfil.
 */
public record UsuarioUpdateDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        String novaSenha
) {}