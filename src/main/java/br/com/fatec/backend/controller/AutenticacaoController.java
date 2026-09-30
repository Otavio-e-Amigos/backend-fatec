package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.autenticacao.AuthResponseDTO;
import br.com.fatec.backend.dto.autenticacao.LoginDTO;
import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.entity.Usuario;
import br.com.fatec.backend.repository.UsuarioRepository;
import br.com.fatec.backend.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/autenticacao")
public class AutenticacaoController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public AutenticacaoController(AuthenticationManager authenticationManager,
                                  TokenService tokenService,
                                  UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<RespostaPadraoDTO<AuthResponseDTO>> login(@Valid @RequestBody LoginDTO dados) {
        // 1. Busca o usuário pelo login
        Usuario usuario = usuarioRepository.findByLogin(dados.login())
                .orElseThrow(() -> new BadCredentialsException("Usuário inexistente ou senha inválida."));

        // 2. Valida se a conta está desativada ANTES da autenticação para garantir a mensagem correta
        if (!usuario.isAtivo()) {
            throw new DisabledException("Usuário desativado. Por favor, entre em contato com a equipe de TI.");
        }

        // 3. Valida a senha digitada no Spring Security
        var usernamePassword = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());
        this.authenticationManager.authenticate(usernamePassword);

        // 4. Gera o Token JWT
        String token = tokenService.gerarToken(usuario);

        // 5. Prepara a resposta
        AuthResponseDTO responseDTO = new AuthResponseDTO(token, usuario.getId(), usuario.getNome(), usuario.getPerfil());
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Login realizado com sucesso.", responseDTO));
    }
}