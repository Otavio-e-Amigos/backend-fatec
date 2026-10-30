package br.com.fatec.backend.dto.folha_ponto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informações do dia do mês para a Folha de Ponto")
public record DiaMesDTO(
        @Schema(description = "Dia do mês (1 a 31)", example = "1")
        Integer dia,

        @Schema(description = "Nome do dia da semana", example = "Segunda")
        String diaSemana,

        @Schema(description = "Indica se o dia é domingo", example = "false")
        boolean domingo,

        @Schema(description = "Indica se o dia não possui aula conforme calendário acadêmico", example = "false")
        boolean semAula,

        @Schema(description = "Motivo do dia sem aula ou feriado, se aplicável", example = "Independência do Brasil")
        String motivo
) {}
