package br.com.fatec.backend.repository;

import br.com.fatec.backend.entity.RegistroFolhaPonto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface RegistroFolhaPontoRepository extends JpaRepository<RegistroFolhaPonto, Long> {

    /** Folhas geradas no intervalo [inicio, fim), com professor e usuário carregados. */
    @Query("""
            SELECT r FROM RegistroFolhaPonto r
            JOIN FETCH r.professor
            LEFT JOIN FETCH r.usuario
            WHERE r.createdAt >= :inicio AND r.createdAt < :fim
            ORDER BY r.createdAt DESC
            """)
    List<RegistroFolhaPonto> buscarNoIntervalo(@Param("inicio") LocalDateTime inicio,
                                               @Param("fim") LocalDateTime fim);

    /** Folhas de um professor no intervalo [inicio, fim). */
    @Query("""
            SELECT r FROM RegistroFolhaPonto r
            JOIN FETCH r.professor p
            LEFT JOIN FETCH r.usuario
            WHERE p.id = :professorId AND r.createdAt >= :inicio AND r.createdAt < :fim
            ORDER BY r.createdAt DESC
            """)
    List<RegistroFolhaPonto> buscarNoIntervaloPorProfessor(@Param("professorId") Long professorId,
                                                           @Param("inicio") LocalDateTime inicio,
                                                           @Param("fim") LocalDateTime fim);

    /** Linhas: [professorId (Long), total (Long), ultimaGeracao (LocalDateTime)] considerando todo o histórico. */
    @Query("""
            SELECT r.professor.id, COUNT(r), MAX(r.createdAt) FROM RegistroFolhaPonto r
            WHERE r.professor.id IN :professorIds
            GROUP BY r.professor.id
            """)
    List<Object[]> totaisHistoricosPorProfessor(@Param("professorIds") Collection<Long> professorIds);

    /** Uma linha: [total (Long), primeiraGeracao (LocalDateTime), ultimaGeracao (LocalDateTime)]. */
    @Query("""
            SELECT COUNT(r), MIN(r.createdAt), MAX(r.createdAt) FROM RegistroFolhaPonto r
            WHERE r.professor.id = :professorId
            """)
    List<Object[]> resumoHistoricoDoProfessor(@Param("professorId") Long professorId);

    /** Linhas: [gradeId (Long), total (Long)] para as grades de um professor. */
    @Query("""
            SELECT r.grade.id, COUNT(r) FROM RegistroFolhaPonto r
            WHERE r.professor.id = :professorId AND r.grade IS NOT NULL
            GROUP BY r.grade.id
            """)
    List<Object[]> totaisPorGradeDoProfessor(@Param("professorId") Long professorId);
}
