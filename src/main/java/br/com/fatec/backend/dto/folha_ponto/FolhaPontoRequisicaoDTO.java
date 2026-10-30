package br.com.fatec.backend.dto.folha_ponto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para solicitação de geração da folha de ponto")
public record FolhaPontoRequisicaoDTO(
        @NotNull(message = "O ID do professor é obrigatório")
        @Schema(description = "ID do professor cadastrado", example = "1")
        Long professorId,

        @Schema(description = "ID da grade horária (opcional, busca a grade ativa correspondente se omitido)", example = "1")
        Long gradeId,

        @NotNull(message = "O mês é obrigatório")
        @Min(value = 1, message = "O mês deve estar entre 1 e 12")
        @Max(value = 12, message = "O mês deve estar entre 1 e 12")
        @Schema(description = "Mês de referência (1 a 12)", example = "9")
        Integer mes,

        @NotNull(message = "O ano é obrigatório")
        @Min(value = 2000, message = "O ano deve ser no mínimo 2000")
        @Max(value = 2050, message = "O ano deve ser no máximo 2050")
        @Schema(description = "Ano de referência", example = "2026")
        Integer ano
) {}
