package br.com.fatec.backend.dto.professor;

import br.com.fatec.backend.entity.StatusProfessor;
import jakarta.validation.constraints.NotNull;

public record ProfessorStatusRequisicaoDTO(
        @NotNull(message = "O status é obrigatório")
        StatusProfessor status
) {}