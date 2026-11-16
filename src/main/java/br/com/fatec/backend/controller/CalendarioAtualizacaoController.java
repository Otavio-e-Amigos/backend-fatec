package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.calendario.ConfirmacaoAtualizacaoCalendarioDTO;
import br.com.fatec.backend.dto.calendario.StatusAtualizacaoCalendarioDTO;
import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.service.CalendarioAtualizacaoService;
import br.com.fatec.backend.service.UsuarioAutenticadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendario/atualizacao")
@Tag(name = "Calendário Acadêmico: atualização anual",
        description = "Confirmação anual de atualização do calendário e situação do lembrete de fevereiro")
public class CalendarioAtualizacaoController {

    private final CalendarioAtualizacaoService service;

    public CalendarioAtualizacaoController(CalendarioAtualizacaoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Situação do calendário do ano atual",
            description = "Informa se o calendário do ano vigente já foi confirmado como atualizado e se o lembrete está ativo.")
    public ResponseEntity<RespostaPadraoDTO<StatusAtualizacaoCalendarioDTO>> statusAnoAtual() {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Situação do calendário consultada com sucesso.",
                service.consultarStatus(null)));
    }

    @GetMapping("/{ano}")
    @Operation(summary = "Situação do calendário de um ano")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Situação retornada"),
            @ApiResponse(responseCode = "400", description = "Ano fora do intervalo permitido")
    })
    public ResponseEntity<RespostaPadraoDTO<StatusAtualizacaoCalendarioDTO>> status(@PathVariable Integer ano) {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Situação do calendário consultada com sucesso.",
                service.consultarStatus(ano)));
    }

    @PostMapping("/{ano}/confirmar")
    @Operation(summary = "Confirmar atualização do calendário do ano",
            description = "Registra que o calendário acadêmico do ano foi revisado. Encerra as pendências de lembrete "
                    + "do ano e impede novos lembretes. Pode ser feito antes ou durante fevereiro; repetir a confirmação apenas atualiza o registro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualização confirmada"),
            @ApiResponse(responseCode = "400", description = "Ano inválido")
    })
    public ResponseEntity<RespostaPadraoDTO<StatusAtualizacaoCalendarioDTO>> confirmar(
            @PathVariable Integer ano,
            @Valid @RequestBody(required = false) ConfirmacaoAtualizacaoCalendarioDTO dados,
            Authentication authentication) {
        Long usuarioId = UsuarioAutenticadoService.exigirId(authentication);
        StatusAtualizacaoCalendarioDTO status = service.confirmarAtualizacao(
                ano, dados != null ? dados.observacao() : null, usuarioId);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Calendário acadêmico de " + ano + " confirmado como atualizado.", status));
    }
}
