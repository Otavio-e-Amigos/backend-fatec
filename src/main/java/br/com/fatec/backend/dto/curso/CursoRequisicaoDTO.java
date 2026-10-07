package br.com.fatec.backend.dto.curso;

import br.com.fatec.backend.entity.Turno;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CursoRequisicaoDTO(
    @Schema(example = "Análise e Desenvolvimento de Sistemas")
    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    String nome,

    @Schema(example = "NOITE", allowableValues = {"MANHA", "TARDE", "NOITE"})
    @NotNull(message = "O turno é obrigatório")
    Turno turno,

    @Schema(example = "ADS")
    @NotBlank(message = "A sigla é obrigatória")
    @Size(max = 3, message = "A sigla deve ter no máximo 3 caracteres")
    String sigla
) {}
