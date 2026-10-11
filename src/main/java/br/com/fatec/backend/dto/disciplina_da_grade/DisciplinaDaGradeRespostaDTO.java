package br.com.fatec.backend.dto.disciplina_da_grade;

import br.com.fatec.backend.entity.DisciplinaDaGrade;
import br.com.fatec.backend.entity.StatusDisciplinaGrade;

public record DisciplinaDaGradeRespostaDTO(
        Long id,
        Long gradeId,
        Long disciplinaId,
        Long cursoId,
        Short semestreCurso,
        String periodo,
        Short quantidadeAulas,
        Integer op,
        StatusDisciplinaGrade status
) {
    public static DisciplinaDaGradeRespostaDTO daEntidade(DisciplinaDaGrade entidade) {
        return new DisciplinaDaGradeRespostaDTO(
                entidade.getId(),
                entidade.getGrade() != null ? entidade.getGrade().getId() : null,
                entidade.getDisciplina() != null ? entidade.getDisciplina().getId() : null,
                entidade.getCurso() != null ? entidade.getCurso().getId() : null,
                entidade.getSemestreCurso(),
                entidade.getPeriodo(),
                entidade.getQuantidadeAulas(),
                entidade.getOp(),
                entidade.getStatus()
        );
    }
}
