package br.com.fatec.backend.dto.aula_da_grade;

import br.com.fatec.backend.entity.AulaDaGrade;
import java.time.LocalTime;

public record AulaDaGradeRespostaDTO(
        Long id,
        Long disciplinaGradeId,
        Short diaSemana,
        LocalTime horaInicio,
        LocalTime horaFim
) {
    public static AulaDaGradeRespostaDTO daEntidade(AulaDaGrade entidade) {
        return new AulaDaGradeRespostaDTO(
                entidade.getId(),
                entidade.getDisciplinaDaGrade() != null ? entidade.getDisciplinaDaGrade().getId() : null,
                entidade.getDiaSemana(),
                entidade.getHoraInicio(),
                entidade.getHoraFim()
        );
    }
}