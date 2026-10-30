package br.com.fatec.backend.dto.calendario;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Dados para cadastro ou atualização de evento no Calendário Acadêmico")
public record EventoCalendarioRequisicaoDTO(
        @NotNull(message = "A data do evento é obrigatória")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "Data do evento (formato ISO YYYY-MM-DD)", example = "2026-09-07")
        LocalDate data,

        @NotBlank(message = "A descrição do evento é obrigatória")
        @Schema(description = "Descrição do feriado ou motivo da suspensão de aula", example = "Feriado - Independência do Brasil")
        String descricao,

        @Schema(description = "Tipo do evento (ex: FERIADO, EMENDA, RECESSO, SEM_AULA)", example = "FERIADO")
        String tipo,

        @Schema(description = "Indica se o evento suspende as aulas", example = "true", defaultValue = "true")
        Boolean semAula
) {}
