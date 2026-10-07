package br.com.fatec.backend.dto.periodo_letivo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PeriodoLetivoRequisicaoDTO(
        @NotNull(message = "O semestre é obrigatório.")
        @Min(value = 1, message = "O semestre deve ser 1 ou 2.")
        @Max(value = 2, message = "O semestre deve ser 1 ou 2.")
        Integer semestre,

        @NotNull(message = "O ano é obrigatório.")
        @Min(value = 2000, message = "O ano mínimo é 2000.")
        @Max(value = 2050, message = "O ano máximo é 205-.")
        Integer ano,

        @NotNull(message = "A data inicial é obrigatória.")
        LocalDate dataInicio,

        @NotNull(message = "A data final é obrigatória.")
        LocalDate dataFim
) {}