package br.com.fatec.backend.dto.calendario;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Dados de um evento do Calendário Acadêmico")
public record EventoCalendarioRespostaDTO(

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "Data do evento", example = "2026-09-07")
        LocalDate data,

        @Schema(description = "Descrição do feriado ou motivo da suspensão de aula", example = "Feriado - Independência do Brasil")
        String descricao,

        @Schema(description = "Tipo do evento (ex: FERIADO, EMENDA, RECESSO, SEM_AULA)", example = "FERIADO")
        String tipo,

        @Schema(description = "Indica se o evento suspende as aulas", example = "true")
        boolean semAula
) {}
