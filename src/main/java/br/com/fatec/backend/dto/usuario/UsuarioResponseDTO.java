package br.com.fatec.backend.dto.usuario;

import br.com.fatec.backend.entity.Perfil;
import br.com.fatec.backend.entity.Usuario;

/**
 * DTO usado para enviar os dados do usuário ao Frontend.
 *
 * A senha não é enviada por segurança.
 */
public record UsuarioResponseDTO(
        Long id,
        String nome,
        String login,
        Perfil perfil,
        boolean ativo
) {
    /**
     * Cria um DTO com os dados do usuário que podem ser enviados ao Frontend.
     */
    public static UsuarioResponseDTO daEntidade(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getLogin(),
                usuario.getPerfil(),
                usuario.isAtivo()
        );
    }
}