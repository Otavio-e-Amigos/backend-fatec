package br.com.fatec.backend.dto.relatorio;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Relatório gerencial de geração de folhas de ponto")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RelatorioFolhaPontoDTO(
        PeriodoRelatorioDTO periodo,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime geradoEm,
        @Schema(description = "INDIVIDUAL (um professor) ou TODOS_PROFESSORES") String escopo,
        Resumo resumo,
        List<PorProfessor> porProfessor,
        List<SerieDiaria> evolucaoDiaria,
        List<PorCompetencia> porCompetencia,
        List<PorUsuario> porUsuario,
        List<ItemHistorico> historico,
        @Schema(description = "Informações complementares (somente no relatório individual)") DetalheProfessor professor
) {

    public static final String ESCOPO_INDIVIDUAL = "INDIVIDUAL";
    public static final String ESCOPO_TODOS = "TODOS_PROFESSORES";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Resumo(
            long totalFolhas,
            long professoresAtendidos,
            BigDecimal mediaFolhasPorProfessor,
            @Schema(description = "Professores com status ATIVO (somente no relatório geral)") Long professoresAtivos,
            @Schema(description = "Professores ativos sem folha gerada no período (somente no relatório geral)") Long professoresAtivosSemFolha,
            @Schema(description = "Competências (mês/ano) distintas atendidas") long competenciasDistintas,
            @Schema(description = "Gerações repetidas da mesma folha (professor + competência) no período") long regeracoes,
            @Schema(description = "Folhas geradas com grade de outro período letivo") long folhasComGradeDeOutroPeriodo
    ) {
    }

    public record PorProfessor(
            Long professorId,
            String nome,
            String matricula,
            String status,
            long folhasNoPeriodo,
            BigDecimal percentualDoPeriodo,
            long totalHistorico,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime ultimaGeracao,
            List<String> competencias
    ) {
    }

    public record SerieDiaria(@JsonFormat(pattern = "yyyy-MM-dd") LocalDate data, long quantidade) {
    }

    public record PorCompetencia(String competencia, long folhas, long professores) {
    }

    public record PorUsuario(Long usuarioId, String usuario, long quantidade) {
    }

    public record ItemHistorico(
            Long id,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime dataHora,
            Long professorId,
            String professor,
            String matricula,
            String competencia,
            String periodoLetivoGrade,
            boolean gradeOutroPeriodo,
            int diasSemAula,
            int disciplinas,
            String geradoPor
    ) {
    }

    public record DetalheProfessor(
            Long id,
            String nome,
            String codigo,
            String matricula,
            String titulacao,
            String regimeJuridico,
            String regimeContrato,
            String status,
            long totalFolhasHistorico,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime primeiraGeracao,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime ultimaGeracao,
            List<String> cursos,
            List<GradeDoProfessor> grades,
            List<DisciplinaDoProfessor> principaisDisciplinas
    ) {
    }

    public record GradeDoProfessor(
            Long gradeId,
            String periodoLetivo,
            @JsonFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @JsonFormat(pattern = "yyyy-MM-dd") LocalDate fim,
            BigDecimal cargaSemanal,
            BigDecimal cargaMensal,
            BigDecimal cargaTotal,
            int disciplinas,
            int disciplinasMinistrando,
            int aulasSemanais,
            long folhasGeradas
    ) {
    }

    public record DisciplinaDoProfessor(
            Long disciplinaId,
            String sigla,
            String nome,
            String codigo,
            @Schema(description = "Quantidade de períodos letivos em que o professor ministrou a disciplina") int periodosLetivos,
            @Schema(description = "Soma das aulas semanais em todas as grades") int aulasSemanaisTotal,
            List<String> cursos,
            @Schema(description = "Período letivo mais recente em que aparece") String ultimoPeriodo
    ) {
    }
}
