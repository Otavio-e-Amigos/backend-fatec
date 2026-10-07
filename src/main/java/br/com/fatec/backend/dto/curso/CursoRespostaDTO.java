package br.com.fatec.backend.dto.curso;

import br.com.fatec.backend.entity.Curso;
import br.com.fatec.backend.entity.Turno;

public record CursoRespostaDTO(
        Long id,
        String nome,
        Turno turno,
        String unidade,
        String sigla
) {

    public static CursoRespostaDTO daEntidadeCompleta(Curso curso) {
        return new CursoRespostaDTO(
                curso.getId(),
                curso.getNome(),
                curso.getTurno(),
                curso.getUnidade(),
                curso.getSigla()
        );
    }
}