package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.autenticacao.AuthResponseDTO;
import br.com.fatec.backend.dto.autenticacao.LoginDTO;
import br.com.fatec.backend.entity.Usuario;
import br.com.fatec.backend.repository.UsuarioRepository;
import br.com.fatec.backend.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/autenticacao")
public class AutenticacaoController {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public AutenticacaoController(AuthenticationManager authenticationManager, TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO dados) {
        // 1. Prepara as credenciais
        var usernamePassword = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());

        // 2. O Spring Security valida no banco usando o AutenticacaoService e o PasswordEncoder
        Authentication auth = this.authenticationManager.authenticate(usernamePassword);

        // 3. Busca a entidade original para extrair os dados extras (nome, ID, perfil)
        Usuario usuario = usuarioRepository.findByLogin(dados.login()).orElseThrow();

        // 4. Gera o Token JWT
        String token = tokenService.gerarToken(usuario);

        // 5. Retorna o DTO conforme Critério de Aceite da Issue
        return ResponseEntity.ok(new AuthResponseDTO(token, usuario.getId(), usuario.getNome(), usuario.getPerfil()));
    }

}
