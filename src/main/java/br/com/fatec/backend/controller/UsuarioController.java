package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.dto.usuario.GeminiApiKeyDTO;
import br.com.fatec.backend.dto.usuario.GeminiApiKeyValidacaoDTO;
import br.com.fatec.backend.dto.usuario.UsuarioCreateDTO;
import br.com.fatec.backend.dto.usuario.UsuarioResponseDTO;
import br.com.fatec.backend.dto.usuario.UsuarioUpdateDTO;
import br.com.fatec.backend.enums.Perfil;
import br.com.fatec.backend.service.GeminiApiKeyService;
import br.com.fatec.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
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
    private final GeminiApiKeyService geminiApiKeyService;

    public UsuarioController(UsuarioService service, GeminiApiKeyService geminiApiKeyService) {
        this.service = service;
        this.geminiApiKeyService = geminiApiKeyService;
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

    // === GEMINI API KEY ===

    @Operation(summary = "Validar e salvar chave da API do Gemini",
            description = "Valida se a chave funciona e, se válida, a salva. Usará esta chave no lugar da padrão do sistema.")
    @PutMapping("/me/gemini-api-key")
    public ResponseEntity<RespostaPadraoDTO<GeminiApiKeyValidacaoDTO>> salvarGeminiApiKey(
            @Valid @RequestBody GeminiApiKeyDTO dto,
            Authentication authentication) {

        Long idUsuarioLogado = extrairIdUsuarioLogado(authentication);
        GeminiApiKeyValidacaoDTO resultado = geminiApiKeyService.validar(dto.apiKey());

        if (resultado.valida()) {
            service.salvarGeminiApiKey(idUsuarioLogado, dto.apiKey());
            return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                    "Chave da API validada e salva com sucesso. Suas extrações usarão esta chave.", resultado));
        }

        return ResponseEntity.badRequest().body(RespostaPadraoDTO.erro(
                "Chave inválida: " + resultado.mensagem(), 400));
    }

    @Operation(summary = "Remover chave da API do Gemini do usuário",
            description = "Remove a chave personalizada. O sistema voltará a usar a chave padrão configurada no servidor.")
    @DeleteMapping("/me/gemini-api-key")
    public ResponseEntity<RespostaPadraoDTO<Void>> removerGeminiApiKey(Authentication authentication) {
        Long idUsuarioLogado = extrairIdUsuarioLogado(authentication);
        service.removerGeminiApiKey(idUsuarioLogado);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Chave da API removida. O sistema usará a chave padrão do servidor."));
    }

    @Operation(summary = "Verificar se possui chave da API do Gemini configurada")
    @GetMapping("/me/gemini-api-key")
    public ResponseEntity<RespostaPadraoDTO<Boolean>> verificarGeminiApiKey(Authentication authentication) {
        Long idUsuarioLogado = extrairIdUsuarioLogado(authentication);
        String chave = service.obterGeminiApiKey(idUsuarioLogado);
        boolean possui = chave != null && !chave.isBlank();
        String msg = possui
                ? "Você tem uma chave da API do Gemini configurada."
                : "Nenhuma chave personalizada configurada. O sistema usa a chave padrão do servidor.";
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(msg, possui));
    }

    private Long extrairIdUsuarioLogado(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Usuário não autenticado.");
        }
        return Long.parseLong(authentication.getPrincipal().toString());
    }
}
