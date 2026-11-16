package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.dto.notificacao.NotificacaoContagemDTO;
import br.com.fatec.backend.dto.notificacao.NotificacaoRespostaDTO;
import br.com.fatec.backend.enums.CategoriaNotificacao;
import br.com.fatec.backend.service.NotificacaoService;
import br.com.fatec.backend.service.UsuarioAutenticadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificacoes")
@Tag(name = "Notificações", description = """
        Notificações internas do usuário autenticado (extração de grades, folhas de ponto, relatórios e calendário).         Cada usuário vê somente as próprias.

        **Fluxo de uso no frontend:**
        * `GET /api/notificacoes/contagem` mostra o número no sininho.
        * `GET /api/notificacoes` abre a lista.
        * `PATCH /api/notificacoes/{id}/lida` marca uma notificação como lida quando o usuário clica nela.
        * `PATCH /api/notificacoes/lidas` atende o botão "marcar todas como lidas".
        """)
public class NotificacaoController {

    private final NotificacaoService service;

    public NotificacaoController(NotificacaoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar minhas notificações",
            description = "Abre a lista de notificações do usuário logado (painel do sininho ou tela de histórico). "
                    + "Paginada, mais recentes primeiro. Use lida=false para só as não lidas e categoria para filtrar por módulo. "
                    + "Notificações expiradas, como o lembrete do calendário após fevereiro, só aparecem com incluirExpiradas=true.")
    public ResponseEntity<RespostaPadraoDTO<Page<NotificacaoRespostaDTO>>> listar(
            @Parameter(description = "Filtra por lidas (true) ou não lidas (false)") @RequestParam(required = false) Boolean lida,
            @Parameter(description = "Filtra por categoria") @RequestParam(required = false) CategoriaNotificacao categoria,
            @Parameter(description = "Somente pendências não resolvidas") @RequestParam(defaultValue = "false") boolean apenasPendentes,
            @Parameter(description = "Inclui notificações expiradas (histórico completo)") @RequestParam(defaultValue = "false") boolean incluirExpiradas,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        Long usuarioId = UsuarioAutenticadoService.exigirId(authentication);
        Page<NotificacaoRespostaDTO> pagina = service.listar(usuarioId, lida, categoria, apenasPendentes, incluirExpiradas, pageable);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Notificações listadas com sucesso.", pagina));
    }

    @GetMapping("/contagem")
    @Operation(summary = "Contadores de notificações", description = "Mostra o número no sininho: quantidade de não lidas e de pendências abertas. "
            + "Chamada leve, pensada para ser consultada periodicamente pelo frontend.")
    public ResponseEntity<RespostaPadraoDTO<NotificacaoContagemDTO>> contagem(Authentication authentication) {
        Long usuarioId = UsuarioAutenticadoService.exigirId(authentication);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Contagem obtida com sucesso.", service.contar(usuarioId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar notificação", description = "Retorna uma notificação do usuário logado pelo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificação encontrada"),
            @ApiResponse(responseCode = "404", description = "Notificação inexistente ou de outro usuário")
    })
    public ResponseEntity<RespostaPadraoDTO<NotificacaoRespostaDTO>> buscar(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = UsuarioAutenticadoService.exigirId(authentication);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Notificação encontrada.", service.buscar(id, usuarioId)));
    }

    @PatchMapping("/{id}/lida")
    @Operation(summary = "Marcar notificação como lida", description = "Chamada quando o usuário clica na notificação. "
            + "Registra a data de leitura. Não encerra pendências (o lembrete do calendário só some quando o calendário é confirmado).")
    public ResponseEntity<RespostaPadraoDTO<NotificacaoRespostaDTO>> marcarComoLida(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = UsuarioAutenticadoService.exigirId(authentication);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Notificação marcada como lida.", service.marcarComoLida(id, usuarioId)));
    }

    @PatchMapping("/{id}/nao-lida")
    @Operation(summary = "Marcar notificação como não lida", description = "Volta a notificação para não lida, para o usuário revisar depois.")
    public ResponseEntity<RespostaPadraoDTO<NotificacaoRespostaDTO>> marcarComoNaoLida(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = UsuarioAutenticadoService.exigirId(authentication);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Notificação marcada como não lida.", service.marcarComoNaoLida(id, usuarioId)));
    }

    @PatchMapping("/lidas")
    @Operation(summary = "Marcar todas como lidas", description = "Atende o botão \"marcar todas como lidas\". "
            + "Retorna a quantidade de notificações atualizadas. Pendências continuam abertas até serem resolvidas.")
    public ResponseEntity<RespostaPadraoDTO<Integer>> marcarTodasComoLidas(Authentication authentication) {
        Long usuarioId = UsuarioAutenticadoService.exigirId(authentication);
        int atualizadas = service.marcarTodasComoLidas(usuarioId);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(atualizadas + " notificação(ões) marcada(s) como lida(s).", atualizadas));
    }
}
