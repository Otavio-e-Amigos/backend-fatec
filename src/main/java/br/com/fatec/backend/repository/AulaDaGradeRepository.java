package br.com.fatec.backend.repository;

import br.com.fatec.backend.entity.AulaDaGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AulaDaGradeRepository  extends JpaRepository<AulaDaGrade, Long> {

    List<AulaDaGrade> findByDisciplinaDaGradeId(Long disciplinaGradeId);

    @Query("SELECT a FROM AulaDaGrade a JOIN FETCH a.disciplinaDaGrade dg JOIN FETCH dg.disciplina WHERE dg.grade.id = :gradeId")
    List<AulaDaGrade> findByGradeIdComDetalhes(@Param("gradeId") Long gradeId);

}
