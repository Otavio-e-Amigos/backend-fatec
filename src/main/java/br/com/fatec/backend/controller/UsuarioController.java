package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.dto.usuario.UsuarioCreateDTO;
import br.com.fatec.backend.dto.usuario.UsuarioResponseDTO;
import br.com.fatec.backend.dto.usuario.UsuarioUpdateDTO;
import br.com.fatec.backend.entity.Perfil;
import br.com.fatec.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<RespostaPadraoDTO<UsuarioResponseDTO>> meuPerfil(Authentication authentication) {
        Long idUsuarioLogado = extrairIdUsuarioLogado(authentication);
        UsuarioResponseDTO dados = service.buscarPorId(idUsuarioLogado);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Perfil do usuário carregado com sucesso.", dados));
    }

    @PreAuthorize("hasRole('TI')")
    @GetMapping
    public ResponseEntity<RespostaPadraoDTO<List<UsuarioResponseDTO>>> listarTodos() {
        List<UsuarioResponseDTO> usuarios = service.listarTodos();
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Lista de usuários obtida com sucesso.", usuarios));
    }

    @PreAuthorize("hasRole('TI')")
    @GetMapping("/{id}")
    public ResponseEntity<RespostaPadraoDTO<UsuarioResponseDTO>> buscarPorId(@PathVariable Long id) {
        UsuarioResponseDTO dados = service.buscarPorId(id);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Usuário encontrado com sucesso.", dados));
    }

    @PreAuthorize("hasRole('TI')")
    @PostMapping
    public ResponseEntity<RespostaPadraoDTO<UsuarioResponseDTO>> criar(@Valid @RequestBody UsuarioCreateDTO dto) {
        UsuarioResponseDTO novoUsuario = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RespostaPadraoDTO.criado("Usuário cadastrado com sucesso.", novoUsuario));
    }

    @PreAuthorize("hasRole('TI')")
    @PutMapping("/{id}")
    public ResponseEntity<RespostaPadraoDTO<UsuarioResponseDTO>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO dto) {
        UsuarioResponseDTO usuarioAtualizado = service.atualizar(id, dto);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("O usuário foi alterado com sucesso.", usuarioAtualizado));
    }

    @PreAuthorize("hasRole('TI')")
    @PatchMapping("/{id}/perfil")
    public ResponseEntity<RespostaPadraoDTO<Void>> alterarPerfil(
            @PathVariable Long id,
            @RequestParam Perfil novoPerfil,
            Authentication authentication) {

        Long idUsuarioLogado = extrairIdUsuarioLogado(authentication);
        service.alterarPerfil(id, novoPerfil, idUsuarioLogado);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Perfil do usuário alterado com sucesso."));
    }

    @PreAuthorize("hasRole('TI')")
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<RespostaPadraoDTO<Void>> ativar(@PathVariable Long id) {
        service.ativar(id);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("O usuário foi ativado com sucesso."));
    }

    @PreAuthorize("hasRole('TI')")
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<RespostaPadraoDTO<Void>> desativar(@PathVariable Long id) {
        service.desativar(id);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("O usuário foi desativado com sucesso."));
    }

    private Long extrairIdUsuarioLogado(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Usuário não autenticado.");
        }
        return Long.parseLong(authentication.getPrincipal().toString());
    }
}