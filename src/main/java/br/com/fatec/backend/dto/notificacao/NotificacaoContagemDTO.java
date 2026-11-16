package br.com.fatec.backend.dto.notificacao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Contadores para o indicador (badge) de notificações")
public record NotificacaoContagemDTO(
        @Schema(description = "Notificações não lidas e ainda visíveis") long naoLidas,
        @Schema(description = "Pendências ainda não resolvidas") long pendentes
) {
}
