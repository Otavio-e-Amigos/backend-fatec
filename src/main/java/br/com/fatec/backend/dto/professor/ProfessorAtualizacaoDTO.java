package br.com.fatec.backend.dto.professor;

import br.com.fatec.backend.entity.RegimeContrato;
import br.com.fatec.backend.entity.StatusProfessor;
import br.com.fatec.backend.entity.Titulacao;
import jakarta.validation.constraints.Size;

public record ProfessorAtualizacaoDTO (

        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @Size(max = 30, message = "O código deve ter no máximo 30 caracteres")
        String codigo,

        String cpf,

        @Size(max = 30, message = "A matrícula deve ter no máximo 30 caracteres")
        String matricula,

        RegimeContrato regimeContrato,

        @Size(max = 20, message = "O regime jurídico deve ter no máximo 20 caracteres")
        String regimeJuridico,

        StatusProfessor status,

        Titulacao titulacao

) {}