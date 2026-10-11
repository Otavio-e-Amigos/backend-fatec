package br.com.fatec.backend.dto.aula_da_grade;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;

public record AulaDaGradeRequisicaoDTO(
        @NotNull(message = "O dia da semana é obrigatório")
        @Min(value = 1, message = "O dia da semana deve ser entre 1 e 7")
        @Max(value = 7, message = "O dia da semana deve ser entre 1 e 7")
        @Schema(example = "2", description = "1=Segunda-feira até 7=Domingo (ISO-8601)")
        Short diaSemana,

        @NotNull(message = "A hora de início é obrigatória")
        @Schema(example = "13:00")
        LocalTime horaInicio,

        @NotNull(message = "A hora de término é obrigatória")
        @Schema(example = "18:20")
        LocalTime horaFim
) {}
