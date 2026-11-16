package br.com.fatec.backend.dto.relatorio;

import br.com.fatec.backend.enums.TipoPeriodoRelatorio;
import br.com.fatec.backend.service.relatorio.PeriodoRelatorio;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Período analisado pelo relatório")
public record PeriodoRelatorioDTO(
        TipoPeriodoRelatorio tipo,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dataReferencia,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,
        String descricao,
        @Schema(description = "true quando o período ainda não terminou (dados parciais)")
        boolean emAndamento
) {
    public static PeriodoRelatorioDTO de(PeriodoRelatorio periodo, LocalDate hoje) {
        return new PeriodoRelatorioDTO(periodo.tipo(), periodo.dataReferencia(), periodo.dataInicio(),
                periodo.dataFim(), periodo.descricao(), periodo.emAndamento(hoje));
    }
}
