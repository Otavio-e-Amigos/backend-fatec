package br.com.fatec.backend.repository;

import br.com.fatec.backend.entity.PeriodoLetivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeriodoLetivoRepository extends JpaRepository<PeriodoLetivo, Long> {

    boolean existsByAnoAndSemestre(Integer ano, Integer semestre);

    List<PeriodoLetivo> findAllByOrderByAnoDescSemestreDesc();
}