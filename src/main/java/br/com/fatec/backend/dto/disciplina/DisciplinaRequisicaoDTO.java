package br.com.fatec.backend.dto.disciplina;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DisciplinaRequisicaoDTO(

        @NotBlank(message = "O código é obrigatório")
        @Size(max = 30, message = "O código deve ter no máximo 30 caracteres")
        String codigo,

        @NotBlank(message = "A sigla é obrigatória")
        @Size(max = 8, message = "A sigla deve ter no máximo 8 caracteres")
        @Pattern(regexp = "^[a-zA-Z]+$", message = "A sigla deve conter apenas letras, sem números ou caracteres especiais")
        String sigla,

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome

) {}