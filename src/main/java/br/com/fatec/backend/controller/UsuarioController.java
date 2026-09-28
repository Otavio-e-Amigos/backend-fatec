package br.com.fatec.backend.controller;

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
    public ResponseEntity<UsuarioResponseDTO> meuPerfil(Authentication authentication) {
        Long idUsuarioLogado = extrairIdUsuarioLogado(authentication);
        return ResponseEntity.ok(service.buscarPorId(idUsuarioLogado));
    }

    @PreAuthorize("hasRole('TI')")
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @PreAuthorize("hasRole('TI')")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PreAuthorize("hasRole('TI')")
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @PreAuthorize("hasRole('TI')")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @PreAuthorize("hasRole('TI')")
    @PatchMapping("/{id}/perfil")
    public ResponseEntity<Void> alterarPerfil(
            @PathVariable Long id,
            @RequestParam Perfil novoPerfil,
            Authentication authentication) {

        Long idUsuarioLogado = extrairIdUsuarioLogado(authentication);
        service.alterarPerfil(id, novoPerfil, idUsuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('TI')")
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        service.ativar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('TI')")
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extrai o ID do usuário diretamente do objeto de Autenticação do Spring.
     */
    private Long extrairIdUsuarioLogado(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Usuário não autenticado.");
        }
        // O JWT injetará o ID do usuário como String no "Principal" (configuraremos isso no JwtFilter)
        return Long.parseLong(authentication.getPrincipal().toString());
    }
}