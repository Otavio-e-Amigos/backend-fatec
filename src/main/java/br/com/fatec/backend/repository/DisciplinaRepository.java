package br.com.fatec.backend.repository;

import br.com.fatec.backend.entity.Disciplina;
import br.com.fatec.backend.specification.DisciplinaSpecs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long>, JpaSpecificationExecutor<Disciplina>{
    Optional<Disciplina> findByNome(String nome);
}
