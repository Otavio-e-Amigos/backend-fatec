package br.com.fatec.backend.repository;

import br.com.fatec.backend.entity.RegistroExtracaoGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RegistroExtracaoGradeRepository extends JpaRepository<RegistroExtracaoGrade, Long> {

    /** Extrações no intervalo [inicio, fim), já com usuário e professor carregados (evita N+1). */
    @Query("""
            SELECT r FROM RegistroExtracaoGrade r
            LEFT JOIN FETCH r.usuario
            LEFT JOIN FETCH r.professor
            WHERE r.createdAt >= :inicio AND r.createdAt < :fim
            ORDER BY r.createdAt DESC
            """)
    List<RegistroExtracaoGrade> buscarNoIntervalo(@Param("inicio") LocalDateTime inicio,
                                                  @Param("fim") LocalDateTime fim);
}
