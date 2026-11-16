package br.com.fatec.backend.dto.relatorio;

import br.com.fatec.backend.enums.StatusExtracaoGrade;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Relatório gerencial de extração de grades (PDF)")
public record RelatorioExtracaoGradeDTO(
        PeriodoRelatorioDTO periodo,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime geradoEm,
        Resumo resumo,
        List<SerieDiaria> evolucaoDiaria,
        List<PorUsuario> porUsuario,
        List<PorPeriodoLetivo> porPeriodoLetivo,
        List<MotivoFalha> principaisFalhas,
        List<ItemExtracao> extracoes
) {

    @Schema(description = "Indicadores consolidados do período")
    public record Resumo(
            @Schema(description = "Total de tentativas de extração") long totalExtracoes,
            @Schema(description = "Grades extraídas com sucesso (com ou sem avisos)") long gradesExtraidas,
            long sucessoSemAvisos,
            long sucessoComAvisos,
            long falhas,
            @Schema(description = "Percentual de extrações bem-sucedidas") BigDecimal taxaSucessoPercentual,
            long totalDisciplinas,
            long totalAulas,
            long totalAvisos,
            BigDecimal mediaDisciplinasPorGrade,
            @Schema(description = "Tempo médio de processamento (ms)") Long tempoMedioMs,
            @Schema(description = "Maior tempo de processamento (ms)") Long tempoMaximoMs,
            @Schema(description = "Professores distintos identificados (por matrícula)") long professoresDistintos,
            @Schema(description = "Professores extraídos que já estão cadastrados no sistema") long professoresCadastrados,
            @Schema(description = "Professores extraídos ainda não cadastrados") long professoresNaoCadastrados,
            @Schema(description = "Volume total de PDFs processados (KB)") long volumeTotalKb
    ) {
    }

    public record SerieDiaria(@JsonFormat(pattern = "yyyy-MM-dd") LocalDate data, long total, long sucesso, long falhas) {
    }

    public record PorUsuario(Long usuarioId, String usuario, long total, long sucesso, long falhas) {
    }

    public record PorPeriodoLetivo(Integer ano, Integer semestre, long quantidade) {
    }

    public record MotivoFalha(String motivo, long quantidade) {
    }

    public record ItemExtracao(
            Long id,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime dataHora,
            String usuario,
            String arquivo,
            Long tamanhoKb,
            StatusExtracaoGrade status,
            String professor,
            String matricula,
            boolean professorCadastrado,
            String periodoLetivo,
            int disciplinas,
            int aulas,
            int avisos,
            Long duracaoMs,
            String mensagemErro
    ) {
    }
}
