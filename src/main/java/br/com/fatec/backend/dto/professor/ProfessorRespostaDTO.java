package br.com.fatec.backend.dto.professor;

import br.com.fatec.backend.entity.Professor;
import br.com.fatec.backend.entity.RegimeContrato;
import br.com.fatec.backend.entity.StatusProfessor;
import br.com.fatec.backend.entity.Titulacao;

/**
 * Dados de saída do professor. O CPF não é devolvido (dado pessoal).
 */
public record ProfessorRespostaDTO(
        Long id,
        String nome,
        String codigo,
        String matricula,
        RegimeContrato regimeContrato,
        String regimeJuridico,
        StatusProfessor status,
        Titulacao titulacao
) {
    public static ProfessorRespostaDTO daEntidade(Professor professor) {
        return new ProfessorRespostaDTO(
                professor.getId(),
                professor.getNome(),
                professor.getCodigo(),
                professor.getMatricula(),
                professor.getRegimeContrato(),
                professor.getRegimeJuridico(),
                professor.getStatus(),
                professor.getTitulacao()
        );
    }
}