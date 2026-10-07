package br.com.fatec.backend.dto.disciplina;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record DisciplinaRequisicaoDTO(

        @NotBlank(message = "O código é obrigatório")
        @Size(max = 30, message = "O nome deve ter no máximo 30 caracteres")
        String codigo,

        @NotBlank(message = "O código é obrigatório")
        @Size(max = 8, message = "A sigla deve ter no máximo 8 caracteres")
        String sigla,

        @NotBlank(message="O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome

){}
