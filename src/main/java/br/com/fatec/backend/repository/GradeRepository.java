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
}