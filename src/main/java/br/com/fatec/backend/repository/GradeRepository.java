package br.com.fatec.backend.repository;

import br.com.fatec.backend.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    boolean existsByProfessorIdAndPeriodoLetivoId(Long professorId, Long periodoLetivoId);

    boolean existsByProfessorIdAndPeriodoLetivoIdAndIdNot(Long professorId, Long periodoLetivoId, Long id);

    List<Grade> findByProfessorId(Long professorId);

    /** Grades do professor com o período letivo carregado, da mais recente para a mais antiga (relatórios). */
    @org.springframework.data.jpa.repository.Query("""
            SELECT g FROM Grade g JOIN FETCH g.periodoLetivo p
            WHERE g.professor.id = :professorId
            ORDER BY p.ano DESC, p.semestre DESC
            """)
    List<Grade> findByProfessorIdComPeriodo(@org.springframework.data.repository.query.Param("professorId") Long professorId);
}