package br.com.fatec.backend.dto.professor;

import br.com.fatec.backend.entity.Professor;
import br.com.fatec.backend.entity.RegimeContrato;
import br.com.fatec.backend.entity.StatusProfessor;
import br.com.fatec.backend.entity.Titulacao;
import br.com.fatec.backend.entity.RegimeJuridico;
import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProfessorRespostaDTO(
        Long id,
        String nome,
        String codigo,
        String cpf,
        String matricula,
        RegimeContrato regimeContrato,
        RegimeJuridico regimeJuridico,
        StatusProfessor status,
        Titulacao titulacao
) {
    public static ProfessorRespostaDTO daEntidadeCompleta(Professor professor) {
        return new ProfessorRespostaDTO(
                professor.getId(),
                professor.getNome(),
                professor.getCodigo(),
                professor.getCpf(),
                professor.getMatricula(),
                professor.getRegimeContrato(),
                professor.getRegimeJuridico(),
                professor.getStatus(),
                professor.getTitulacao()
        );
    }


    public static ProfessorRespostaDTO daEntidadeComCpfMascarado(Professor professor) {
        return new ProfessorRespostaDTO(
                professor.getId(),
                professor.getNome(),
                professor.getCodigo(),
                mascararCpf(professor.getCpf()),
                professor.getMatricula(),
                professor.getRegimeContrato(),
                professor.getRegimeJuridico(),
                professor.getStatus(),
                professor.getTitulacao()
        );
    }

    private static String mascararCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;
        // Pega do 4º ao 6º dígito, e do 7º ao 9º dígito. Retorna: ***.456.789-**
        return "***." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**";
    }
}