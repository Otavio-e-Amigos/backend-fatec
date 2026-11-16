package br.com.fatec.backend.repository;

import br.com.fatec.backend.entity.CalendarioAtualizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalendarioAtualizacaoRepository extends JpaRepository<CalendarioAtualizacao, Integer> {
}
