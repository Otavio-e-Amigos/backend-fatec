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
}
