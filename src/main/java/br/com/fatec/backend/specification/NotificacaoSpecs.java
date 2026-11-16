package br.com.fatec.backend.specification;

import br.com.fatec.backend.entity.Notificacao;
import br.com.fatec.backend.enums.CategoriaNotificacao;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificacaoSpecs {

    private NotificacaoSpecs() {
    }

    /**
     * Filtro da listagem: sempre restrito ao usuário dono; os demais filtros são opcionais.
     * Notificações expiradas (ex: lembrete de calendário fora de fevereiro) só aparecem quando solicitado.
     */
    public static Specification<Notificacao> filtrar(Long usuarioId, Boolean lida, CategoriaNotificacao categoria,
                                                     boolean apenasPendentes, boolean incluirExpiradas,
                                                     LocalDateTime agora) {
        return (root, query, builder) -> {
            List<Predicate> predicados = new ArrayList<>();
            predicados.add(builder.equal(root.get("usuario").get("id"), usuarioId));

            if (lida != null) {
                predicados.add(builder.equal(root.get("lida"), lida));
            }
            if (categoria != null) {
                predicados.add(builder.equal(root.get("categoria"), categoria));
            }
            if (apenasPendentes) {
                predicados.add(builder.isTrue(root.get("pendente")));
            }
            if (!incluirExpiradas) {
                predicados.add(builder.or(
                        builder.isNull(root.get("expiraEm")),
                        builder.greaterThan(root.get("expiraEm"), agora)));
            }
            return builder.and(predicados.toArray(new Predicate[0]));
        };
    }
}
