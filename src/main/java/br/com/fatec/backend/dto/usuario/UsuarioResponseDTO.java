package br.com.fatec.backend.dto.usuario;

import br.com.fatec.backend.entity.Perfil;
import br.com.fatec.backend.entity.Usuario;

import java.time.LocalDateTime;

/**
 * DTO usado para enviar os dados do usuário ao Frontend.
 * A senha nunca é enviada por segurança.
 */
public record UsuarioResponseDTO(
        Long id,
        String nome,
        String login,
        Perfil perfil,
        boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UsuarioResponseDTO daEntidade(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getLogin(),
                usuario.getPerfil(),
                usuario.isAtivo(),
                usuario.getCriadoEm(),
                usuario.getAtualizadoEm()
        );
    }
}