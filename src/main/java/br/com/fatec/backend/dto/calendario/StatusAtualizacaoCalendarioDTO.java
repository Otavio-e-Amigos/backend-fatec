package br.com.fatec.backend.dto.calendario;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Situação de atualização do calendário acadêmico de um ano")
public record StatusAtualizacaoCalendarioDTO(
        Integer ano,
        @Schema(description = "Indica se o calendário do ano já foi confirmado como atualizado")
        boolean atualizado,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime atualizadoEm,
        @Schema(description = "Nome do usuário que confirmou a atualização")
        String atualizadoPor,
        String observacao,
        @Schema(description = "Quantidade de eventos do ano atualmente cadastrados no calendário (em memória)")
        int eventosCadastradosNoAno,
        @Schema(description = "Indica se o lembrete de fevereiro está ativo agora para este ano")
        boolean lembreteAtivo
) {
}
