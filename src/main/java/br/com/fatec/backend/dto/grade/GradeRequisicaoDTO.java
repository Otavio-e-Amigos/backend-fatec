package br.com.fatec.backend.dto.grade;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record GradeRequisicaoDTO(

        @Schema(example = "1")
        @NotNull(message = "O ID do professor é obrigatório")
        Long professorId,

        @Schema(example = "1")
        @NotNull(message = "O ID do período letivo é obrigatório")
        Long periodoLetivoId,

        @Schema(example = "null", nullable = true)
        @PositiveOrZero(message = "A carga horária semanal deve ser maior ou igual a zero")
        BigDecimal semanal,

        @Schema(example = "null", nullable = true)
        @PositiveOrZero(message = "A carga horária mensal deve ser maior ou igual a zero")
        BigDecimal mensal,

        @Schema(example = "null", nullable = true)
        @PositiveOrZero(message = "A carga horária total deve ser maior ou igual a zero")
        BigDecimal total
) {}