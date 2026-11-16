package br.com.fatec.backend.repository;

import br.com.fatec.backend.entity.DisciplinaDaGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisciplinaDaGradeRepository extends JpaRepository<DisciplinaDaGrade, Long> {
    List<DisciplinaDaGrade> findByGradeId(Long gradeId);

    boolean existsByGradeId(Long gradeId);

    boolean existsByGradeIdAndDisciplinaIdAndCursoId(Long gradeId, Long disciplinaId, Long cursoId);

    /** Todos os vínculos de disciplina das grades de um professor, com disciplina, curso e período carregados (relatórios). */
    @org.springframework.data.jpa.repository.Query("""
            SELECT dg FROM DisciplinaDaGrade dg
            JOIN FETCH dg.disciplina
            JOIN FETCH dg.curso
            JOIN FETCH dg.grade g
            JOIN FETCH g.periodoLetivo
            WHERE g.professor.id = :professorId
            """)
    List<DisciplinaDaGrade> findByProfessorIdComDetalhes(@org.springframework.data.repository.query.Param("professorId") Long professorId);
}
