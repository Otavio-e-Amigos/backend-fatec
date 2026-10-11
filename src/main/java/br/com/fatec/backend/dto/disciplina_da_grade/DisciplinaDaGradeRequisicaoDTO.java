package br.com.fatec.backend.dto.disciplina_da_grade;

import br.com.fatec.backend.enums.StatusDisciplinaGrade;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

public record DisciplinaDaGradeRequisicaoDTO(
        @Schema(example = "1")
        @NotNull(message = "O ID da grade é obrigatório")
        Long gradeId,

        @Schema(example = "1")
        @NotNull(message = "O ID da disciplina é obrigatório")
        Long disciplinaId,

        @Schema(example = "1")
        @NotNull(message = "O ID da curso é obrigatório")
        Long cursoId,

        @Schema(example = "1")
        Short semestreCurso,

        @Schema(example = "MANHA")
        @Size(max = 5, message = "O período deve ter no máximo 5 caracteres")
        String periodo,

        @Schema(example = "1")
        @NotNull(message = "A quantidade de aulas é obrigatória")
        @Positive(message = "A quantidade de aulas deve ser maior que zero")
        Short quantidadeAulas,

        @Schema(example = "null", nullable = true)
        @Max(value = 999, message = "O op deve ter no máximo 3 dígitos")
        @Positive(message = "O op tem que ser positivo")
        Integer op,

        @NotNull(message = "O status é obrigatório")
        @Schema(example = "MINISTRANDO")
        StatusDisciplinaGrade status
) {

}
