package br.com.fatec.backend.config;

import br.com.fatec.backend.entity.Usuario;
import br.com.fatec.backend.repository.UsuarioRepository;
import br.com.fatec.backend.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public SecurityFilter(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = recuperarToken(request);

        if (token != null) {
            String login = tokenService.validarTokenEObterLogin(token);

            if (!login.isEmpty()) {
                Usuario usuario = usuarioRepository.findByLogin(login).orElse(null);

                // Regra de segurança: Valida se o usuário existe e se continua ATIVO
                if (usuario != null && usuario.isAtivo()) {
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name()));

                    // Colocamos o ID do usuário como "Principal" para o UsuarioController extrair facilmente
                    var authentication = new UsernamePasswordAuthenticationToken(usuario.getId(), null, authorities);

                    // Registra a autenticação no contexto do Spring Security
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.replace("Bearer ", "").trim();
    }
}