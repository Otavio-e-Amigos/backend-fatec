package br.com.fatec.backend.specification;

import br.com.fatec.backend.entity.Disciplina;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class DisciplinaSpecs {

    public static Specification<Disciplina> buscarPorTermo(String busca) {
        return (root, query, builder) -> {
            if (busca == null || busca.isBlank()) {
                return builder.conjunction();
            }

            String termoBuscado = "%" + busca.toLowerCase().trim() + "%";

            // Permite pesquisar por Nome, Código OU Sigla
            Predicate porNome = builder.like(builder.lower(root.get("nome")), termoBuscado);
            Predicate porCodigo = builder.like(builder.lower(root.get("codigo")), termoBuscado);
            Predicate porSigla = builder.like(builder.lower(root.get("sigla")), termoBuscado);

            return builder.or(porNome, porCodigo, porSigla);
        };
    }
}