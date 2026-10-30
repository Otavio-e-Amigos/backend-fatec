package br.com.fatec.backend.dto.extracao_grade;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/** Dados extraídos do PDF da grade. Independente das entidades do sistema. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GradeExtraidaDTO(
        Integer semestre,
        Integer ano,
        String validadeInicio,   // yyyy-MM-dd
        String validadeFim,      // yyyy-MM-dd
        ProfessorExtraido professor,
        HorasExtraidas horas,
        List<DisciplinaExtraida> disciplinas,
        List<String> avisos
) {
    public GradeExtraidaDTO {
        disciplinas = disciplinas == null ? List.of() : List.copyOf(disciplinas);
        avisos = avisos == null ? List.of() : List.copyOf(avisos);
    }
    public GradeExtraidaDTO comAvisos(List<String> novos) {
        return new GradeExtraidaDTO(semestre, ano, validadeInicio, validadeFim,
                professor, horas, disciplinas, novos);
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProfessorExtraido(
            String nome,
            String matricula,
            String cpf,
            String contrato   // DETERMINADO | INDETERMINADO | TEMPORARIO
    ) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HorasExtraidas(
            BigDecimal semanalAula,
            BigDecimal mensalAula,
            BigDecimal mensalAtividade,
            BigDecimal totalSemanal,
            BigDecimal totalMensal
    ) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DisciplinaExtraida(
            String nome,
            String curso,
            String periodo,          // MANHA | TARDE | NOITE
            String status,           // MINISTRANDO | CANCELADA
            Integer quantidadeAulas,
            Integer op,
            List<AulaExtraida> aulas
    ) {
        public DisciplinaExtraida {
            aulas = aulas == null ? List.of() : List.copyOf(aulas);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AulaExtraida(
            Integer diaSemana,       // 1=segunda ... 7=domingo (ISO-8601)
            String horaInicio,       // HH:mm
            String horaFim           // HH:mm
    ) { }
}