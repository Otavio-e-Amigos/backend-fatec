package br.com.fatec.backend.repository;

import br.com.fatec.backend.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProfessorRepository extends JpaRepository<Professor, Long>, JpaSpecificationExecutor<Professor> {
    Optional<Professor> findByCpf(String cpf);
    Optional<Professor> findByMatricula(String matricula);
}