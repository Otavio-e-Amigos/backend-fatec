package br.com.fatec.backend.dto.professor;

import br.com.fatec.backend.entity.RegimeContrato;
import br.com.fatec.backend.entity.RegimeJuridico;
import br.com.fatec.backend.entity.Titulacao;
import br.com.fatec.backend.validation.CpfValido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProfessorRequisicaoDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @Size(max = 10, message = "O código deve ter no máximo 10 caracteres")
        String codigo,

        @NotBlank(message = "O CPF é obrigatório")
        @CpfValido
        String cpf,

        @NotBlank(message = "A matrícula é obrigatória")
        @Size(max = 10, message = "A matrícula deve ter no máximo 10 caracteres")
        String matricula,

        @NotNull(message = "O regime de contrato é obrigatório")
        RegimeContrato regimeContrato,

        @NotNull(message = "O regime jurídico é obrigatório")
        RegimeJuridico regimeJuridico,

        @NotNull(message = "A titulação é obrigatória")
        Titulacao titulacao
) {}