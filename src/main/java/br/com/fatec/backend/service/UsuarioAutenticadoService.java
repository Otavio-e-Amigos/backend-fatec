package br.com.fatec.backend.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Obtém o ID do usuário autenticado. O SecurityFilter registra o ID do usuário como "principal".
 */
@Service
public class UsuarioAutenticadoService {

    public Optional<Long> idUsuarioAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return Optional.empty();
        }
        return extrairId(auth.getPrincipal());
    }

    public Long exigirIdUsuarioAtual() {
        return idUsuarioAtual()
                .orElseThrow(() -> new AccessDeniedException("Usuário não autenticado."));
    }

    public static Long exigirId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }
        return extrairId(authentication.getPrincipal())
                .orElseThrow(() -> new AccessDeniedException("Usuário não autenticado."));
    }

    private static Optional<Long> extrairId(Object principal) {
        if (principal instanceof Long id) {
            return Optional.of(id);
        }
        try {
            return Optional.of(Long.parseLong(principal.toString()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
