package br.com.fatec.backend.repository;
import br.com.fatec.backend.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    boolean existsByCpf(String cpf);

    boolean existsByMatricula(String matricula);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByMatriculaAndIdNot(String matricula, Long id);
}
