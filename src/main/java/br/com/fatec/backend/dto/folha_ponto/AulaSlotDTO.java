package br.com.fatec.backend.dto.folha_ponto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mapeamento de aula no slot correspondente da grade")
public record AulaSlotDTO(
        @Schema(description = "Dia da semana ISO (1 a 7)", example = "1")
        Short diaSemana,

        @Schema(description = "Nome do dia da semana", example = "Segunda")
        String nomeDiaSemana,

        @Schema(description = "Turno da aula", example = "MANHA")
        String turno,

        @Schema(description = "Número ordinal do slot no turno (1 a 6)", example = "2")
        Integer slot,

        @Schema(description = "Sigla da disciplina preenchida na célula", example = "BD")
        String sigla,

        @Schema(description = "Nome completo da disciplina", example = "Banco de Dados")
        String nomeDisciplina,

        @Schema(description = "Horário de início real da aula", example = "08:00")
        String horarioInicio,

        @Schema(description = "Horário de término real da aula", example = "08:50")
        String horarioFim
) {}
