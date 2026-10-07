package br.com.fatec.backend.repository;

import br.com.fatec.backend.entity.Curso;
import br.com.fatec.backend.entity.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

    List<Curso> findAllByOrderByNomeAsc();

    boolean existsByUnidadeAndNomeIgnoreCaseAndTurno(String unidade, String nome, Turno turno);

    boolean existsByUnidadeAndNomeIgnoreCaseAndTurnoAndIdNot(String unidade, String nome, Turno turno, Long id);

    boolean existsByUnidadeAndSiglaIgnoreCaseAndTurno(String unidade, String sigla, Turno turno);

    boolean existsByUnidadeAndSiglaIgnoreCaseAndTurnoAndIdNot(String unidade, String sigla, Turno turno, Long id);
}