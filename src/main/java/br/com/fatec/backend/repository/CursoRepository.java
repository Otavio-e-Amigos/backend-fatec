package br.com.fatec.backend.repository;

import br.com.fatec.backend.entity.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    boolean existsByUnidadeAndSiglaIgnoreCase(String unidade, String sigla);

    boolean existsByUnidadeAndSiglaIgnoreCaseAndIdNot(String unidade, String sigla, Long id);

    boolean existsByUnidadeAndNomeIgnoreCase(String unidade, String nome);

    boolean existsByUnidadeAndNomeIgnoreCaseAndIdNot(String unidade, String nome, Long id);
}