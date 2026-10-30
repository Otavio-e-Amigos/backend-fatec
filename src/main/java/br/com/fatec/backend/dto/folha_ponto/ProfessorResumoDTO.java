package br.com.fatec.backend.dto.folha_ponto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo dos dados do professor para a Folha de Ponto")
public record ProfessorResumoDTO(
        @Schema(description = "ID do professor", example = "1")
        Long id,

        @Schema(description = "Nome completo do professor", example = "Katia Barbosa Martines")
        String nome,

        @Schema(description = "Código do professor", example = "265")
        String codigo,

        @Schema(description = "Matrícula do professor", example = "123456")
        String matricula,

        @Schema(description = "Regime Jurídico", example = "CLT")
        String regimeJuridico,

        @Schema(description = "Regime de Contrato", example = "INDETERMINADO")
        String regimeContrato
) {}
