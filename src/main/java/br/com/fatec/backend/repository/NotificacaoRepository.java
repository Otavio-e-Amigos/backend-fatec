package br.com.fatec.backend.repository;

import br.com.fatec.backend.entity.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long>, JpaSpecificationExecutor<Notificacao> {

    Optional<Notificacao> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByUsuarioIdAndChaveDeduplicacao(Long usuarioId, String chaveDeduplicacao);

    @Query("""
            SELECT COUNT(n) FROM Notificacao n
            WHERE n.usuario.id = :usuarioId
              AND n.lida = false
              AND (n.expiraEm IS NULL OR n.expiraEm > :agora)
            """)
    long contarNaoLidasVisiveis(@Param("usuarioId") Long usuarioId, @Param("agora") LocalDateTime agora);

    @Query("""
            SELECT COUNT(n) FROM Notificacao n
            WHERE n.usuario.id = :usuarioId
              AND n.pendente = true
              AND (n.expiraEm IS NULL OR n.expiraEm > :agora)
            """)
    long contarPendentesVisiveis(@Param("usuarioId") Long usuarioId, @Param("agora") LocalDateTime agora);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Notificacao n SET n.lida = true, n.lidaEm = :agora
            WHERE n.usuario.id = :usuarioId AND n.lida = false
            """)
    int marcarTodasComoLidas(@Param("usuarioId") Long usuarioId, @Param("agora") LocalDateTime agora);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Notificacao n SET n.pendente = false, n.resolvidaEm = :agora
            WHERE n.chaveDeduplicacao = :chave AND n.pendente = true
            """)
    int resolverPendenciasPorChave(@Param("chave") String chave, @Param("agora") LocalDateTime agora);
}
