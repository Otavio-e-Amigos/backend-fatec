package br.com.fatec.backend.dto.periodo_letivo;

import br.com.fatec.backend.entity.PeriodoLetivo;
import java.time.LocalDate;

public record PeriodoLetivoRespostaDTO(
        Long id,
        Integer semestre,
        Integer ano,
        LocalDate dataInicio,
        LocalDate dataFim
) {
    public static PeriodoLetivoRespostaDTO daEntidade(PeriodoLetivo periodoLetivo) {
        return new PeriodoLetivoRespostaDTO(
                periodoLetivo.getId(),
                periodoLetivo.getSemestre(),
                periodoLetivo.getAno(),
                periodoLetivo.getDataInicio(),
                periodoLetivo.getDataFim()
        );
    }
}