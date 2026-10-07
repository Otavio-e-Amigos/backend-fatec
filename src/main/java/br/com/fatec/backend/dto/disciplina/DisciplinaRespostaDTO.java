package br.com.fatec.backend.dto.disciplina;

import br.com.fatec.backend.entity.Disciplina;

public record DisciplinaRespostaDTO(
        Long id,
        String codigo,
        String sigla,
        String nome
) {
    public static DisciplinaRespostaDTO daEntidade(Disciplina disciplina) {
        return new DisciplinaRespostaDTO(
                disciplina.getId(),
                disciplina.getCodigo(),
                disciplina.getSigla(),
                disciplina.getNome()
        );
    }
}