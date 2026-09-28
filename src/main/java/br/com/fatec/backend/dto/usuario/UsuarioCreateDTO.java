package br.com.fatec.backend.dto.usuario;

import br.com.fatec.backend.entity.Perfil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO responsável por receber os dados de criação de um novo usuário.
 */
public record UsuarioCreateDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "A senha é obrigatória")
        String senha,

        @NotNull(message = "O perfil é obrigatório")
        Perfil perfil
) {}