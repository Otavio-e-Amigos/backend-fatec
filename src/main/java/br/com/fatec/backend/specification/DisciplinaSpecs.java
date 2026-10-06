package br.com.fatec.backend.specification;

import br.com.fatec.backend.entity.Disciplina;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class DisciplinaSpecs {

    public static Specification<Disciplina> buscarDisciplinaPorNome(String busca) {
        return (root, query, builder) -> {

            // Se a busca for nula ou vazia, retorna "WHERE 1=1" (traz todos sem custo)

            if(busca == null || busca.isBlank() ){
                return builder.conjunction();
            }

            // Evita chamar LOWER() a cada linha no banco; fazemos isso no Java uma vez
            String termoBuscado = "%" + busca.toLowerCase() + "%";

            // Monta: LOWER(nome) LIKE '%termo%'
            Predicate porNome = builder.like(builder.lower(root.get("nome")), termoBuscado);

            return builder.in(porNome);
        };


    }
}
