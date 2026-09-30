package br.com.fatec.backend.service;

import br.com.fatec.backend.entity.Usuario;
import br.com.fatec.backend.exception.RegraNegocioException;
import br.com.fatec.backend.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutenticacaoService implements UserDetailsService {
    private final UsuarioRepository repository;

    public AutenticacaoService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = repository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário ou senha inválidos"));

        if (!usuario.isAtivo()) {
            throw new RegraNegocioException("Usuário inativo. Contate o administrador.");
        }

        return new User(
                usuario.getLogin(),
                usuario.getSenhaHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name()))
        );
    }
}
