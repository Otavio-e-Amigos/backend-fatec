package br.com.fatec.backend.service.relatorio;

import br.com.fatec.backend.document.relatorio.DocumentoRelatorio;
import br.com.fatec.backend.document.relatorio.DocumentoRelatorio.*;
import br.com.fatec.backend.dto.relatorio.PeriodoRelatorioDTO;
import br.com.fatec.backend.dto.relatorio.RelatorioExtracaoGradeDTO;
import br.com.fatec.backend.dto.relatorio.RelatorioFolhaPontoDTO;
import br.com.fatec.backend.enums.StatusExtracaoGrade;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Converte os DTOs dos relatórios no modelo neutro {@link DocumentoRelatorio}, que é renderizado em
 * PDF ou DOCX. Apenas dados existentes no banco são apresentados (nada é estimado ou inventado).
 */
@Component
public class RelatorioDocumentoMontador {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    static final String TIPO_EXTRACAO = "Relatório de Extração de Grades";
    static final String TIPO_FOLHA = "Relatório de Folhas de Ponto";

    // =========================================================================
    // Extração de grades
    // =========================================================================

    public DocumentoRelatorio extracaoGrades(RelatorioExtracaoGradeDTO r, String geradoPor) {
        var resumo = r.resumo();

        List<Indicador> indicadores = List.of(
                new Indicador("Grades extraídas", inteiro(resumo.gradesExtraidas())),
                new Indicador("Tentativas de extração", inteiro(resumo.totalExtracoes())),
                new Indicador("Taxa de sucesso", percentual(resumo.taxaSucessoPercentual())),
                new Indicador("Falhas", inteiro(resumo.falhas())),
                new Indicador("Disciplinas extraídas", inteiro(resumo.totalDisciplinas())),
                new Indicador("Horários de aula extraídos", inteiro(resumo.totalAulas())),
                new Indicador("Avisos para revisão", inteiro(resumo.totalAvisos())),
                new Indicador("Tempo médio de processamento", duracao(resumo.tempoMedioMs()))
        );

        List<Secao> secoes = new ArrayList<>();

        List<Bloco> blocosResumo = new ArrayList<>();
        blocosResumo.add(new FichaChaveValor(List.of(
                new Indicador("Sucesso sem avisos", inteiro(resumo.sucessoSemAvisos())),
                new Indicador("Sucesso com avisos", inteiro(resumo.sucessoComAvisos())),
                new Indicador("Média de disciplinas por grade", decimal(resumo.mediaDisciplinasPorGrade())),
                new Indicador("Maior tempo de processamento", duracao(resumo.tempoMaximoMs())),
                new Indicador("Volume de PDFs processados", inteiro(resumo.volumeTotalKb()) + " KB"),
                new Indicador("Professores distintos", inteiro(resumo.professoresDistintos())),
                new Indicador("Professores já cadastrados", inteiro(resumo.professoresCadastrados())),
                new Indicador("Professores não cadastrados", inteiro(resumo.professoresNaoCadastrados()))
        )));
        adicionarAvisoPeriodo(blocosResumo, r.periodo());
        secoes.add(new Secao("Resumo do processamento",
                "Grades extraídas são as extrações concluídas com sucesso, com ou sem avisos de revisão.", blocosResumo));

        secoes.add(new Secao("Extrações por usuário", null, List.of(new Tabela(
                List.of("Usuário", "Tentativas", "Sucesso", "Falhas"),
                r.porUsuario().stream().map(u -> List.of(
                        u.usuario(), inteiro(u.total()), inteiro(u.sucesso()), inteiro(u.falhas()))).toList(),
                new float[]{3f, 1f, 1f, 1f},
                new boolean[]{false, true, true, true},
                "Nenhuma extração no período."))));

        secoes.add(new Secao("Grades extraídas por período letivo",
                "Período letivo identificado no próprio PDF da grade.", List.of(new Tabela(
                List.of("Período letivo", "Grades extraídas"),
                r.porPeriodoLetivo().stream().map(p -> List.of(
                        p.semestre() + "º semestre de " + p.ano(), inteiro(p.quantidade()))).toList(),
                new float[]{3f, 1f},
                new boolean[]{false, true},
                "Nenhuma grade extraída no período."))));

        secoes.add(new Secao("Principais motivos de falha", null, List.of(new Tabela(
                List.of("Motivo", "Ocorrências"),
                r.principaisFalhas().stream().map(f -> List.of(f.motivo(), inteiro(f.quantidade()))).toList(),
                new float[]{5f, 1f},
                new boolean[]{false, true},
                "Nenhuma falha registrada no período."))));

        secoes.add(new Secao("Grades processadas",
                "Detalhamento de cada extração realizada no período (mais recentes primeiro). "
                        + "Cad. indica se o professor do PDF já está cadastrado no sistema.", List.of(new Tabela(
                List.of("Data/hora", "Arquivo", "Professor", "Cad.", "Período", "Disc.", "Aulas", "Avisos", "Situação", "Tempo"),
                r.extracoes().stream().map(e -> List.of(
                        dataHora(e.dataHora()),
                        textoOuVazio(e.arquivo()),
                        professorComMatricula(e.professor(), e.matricula()),
                        e.status() == StatusExtracaoGrade.FALHA ? "" : (e.professorCadastrado() ? "Sim" : "Não"),
                        textoOuVazio(e.periodoLetivo()),
                        inteiro(e.disciplinas()), inteiro(e.aulas()), inteiro(e.avisos()),
                        situacao(e.status()),
                        duracao(e.duracaoMs()))).toList(),
                new float[]{1.3f, 1.9f, 2.3f, 0.7f, 0.9f, 0.75f, 0.8f, 0.9f, 1.1f, 0.8f},
                new boolean[]{false, false, false, false, false, true, true, true, false, true},
                "Nenhuma extração no período."))));

        return new DocumentoRelatorio(
                TIPO_EXTRACAO,
                "Relatório " + r.periodo().tipo().getRotulo().toLowerCase(PT_BR) + " de extração de grades",
                "Quantidade de grades extraídas de PDFs e informações sobre o processamento no período selecionado.",
                r.periodo().descricao(),
                r.geradoEm(),
                geradoPor,
                indicadores,
                secoes,
                "Fonte: histórico de extrações registrado pelo Appointer. Extrações anteriores à implantação do histórico "
                        + "não são contabilizadas. O CPF dos professores não é armazenado nem exibido neste relatório."
        );
    }

    // =========================================================================
    // Folhas de ponto
    // =========================================================================

    public DocumentoRelatorio folhasPonto(RelatorioFolhaPontoDTO r, String geradoPor) {
        boolean individual = RelatorioFolhaPontoDTO.ESCOPO_INDIVIDUAL.equals(r.escopo());
        var resumo = r.resumo();
        var professor = r.professor();

        List<Indicador> indicadores = new ArrayList<>();
        indicadores.add(new Indicador("Folhas geradas no período", inteiro(resumo.totalFolhas())));
        if (individual) {
            indicadores.add(new Indicador("Total histórico do professor", inteiro(professor.totalFolhasHistorico())));
            indicadores.add(new Indicador("Grades vinculadas", inteiro(professor.grades().size())));
        } else {
            indicadores.add(new Indicador("Professores atendidos", inteiro(resumo.professoresAtendidos())));
            indicadores.add(new Indicador("Média de folhas por professor", decimal(resumo.mediaFolhasPorProfessor())));
            indicadores.add(new Indicador("Professores ativos", inteiro(resumo.professoresAtivos())));
            indicadores.add(new Indicador("Professores ativos sem folha", inteiro(resumo.professoresAtivosSemFolha())));
        }
        indicadores.add(new Indicador("Competências (mês/ano) atendidas", inteiro(resumo.competenciasDistintas())));
        indicadores.add(new Indicador("Regerações da mesma folha", inteiro(resumo.regeracoes())));
        indicadores.add(new Indicador("Folhas com grade de outro período", inteiro(resumo.folhasComGradeDeOutroPeriodo())));

        List<Secao> secoes = new ArrayList<>();

        if (individual) {
            secoes.add(new Secao("Identificação do professor", null, List.of(new FichaChaveValor(List.of(
                    new Indicador("Nome", professor.nome()),
                    new Indicador("Matrícula", professor.matricula()),
                    new Indicador("Código", textoOuVazio(professor.codigo())),
                    new Indicador("Titulação", enumLegivel(professor.titulacao())),
                    new Indicador("Regime jurídico", textoOuVazio(professor.regimeJuridico())),
                    new Indicador("Regime de contrato", enumLegivel(professor.regimeContrato())),
                    new Indicador("Situação", enumLegivel(professor.status())),
                    new Indicador("Cursos em que leciona", professor.cursos().isEmpty() ? "" : String.join("; ", professor.cursos())),
                    new Indicador("Folhas geradas (histórico)", inteiro(professor.totalFolhasHistorico())),
                    new Indicador("Primeira geração registrada", dataHora(professor.primeiraGeracao())),
                    new Indicador("Última geração registrada", dataHora(professor.ultimaGeracao()))
            )))));

            secoes.add(new Secao("Grades acadêmicas vinculadas",
                    "Grades horárias do professor por período letivo, com carga horária cadastrada e folhas geradas a partir de cada uma.",
                    List.of(new Tabela(
                            List.of("Grade", "Período letivo", "Vigência", "Disciplinas", "Aulas/sem.", "Carga sem.", "Carga mensal", "Carga total", "Folhas"),
                            professor.grades().stream().map(g -> List.of(
                                    "#" + g.gradeId(),
                                    textoOuVazio(g.periodoLetivo()),
                                    vigencia(g.inicio(), g.fim()),
                                    g.disciplinasMinistrando() + " de " + g.disciplinas(),
                                    inteiro(g.aulasSemanais()),
                                    decimal(g.cargaSemanal()), decimal(g.cargaMensal()), decimal(g.cargaTotal()),
                                    inteiro(g.folhasGeradas()))).toList(),
                            new float[]{0.7f, 1.1f, 2f, 1f, 1f, 0.9f, 1f, 1f, 0.7f},
                            new boolean[]{false, false, false, true, true, true, true, true, true},
                            "O professor não possui grades cadastradas."))));
        }

        if (!individual) {
            secoes.add(new Secao("Comparativo por professor",
                    "Folhas geradas por professor no período, participação no total e histórico acumulado para comparação.",
                    List.of(new Tabela(
                            List.of("Professor", "Matrícula", "Situação", "No período", "% do período", "Histórico", "Última geração", "Competências"),
                            r.porProfessor().stream().map(p -> List.of(
                                    p.nome(), p.matricula(), enumLegivel(p.status()),
                                    inteiro(p.folhasNoPeriodo()), percentual(p.percentualDoPeriodo()),
                                    inteiro(p.totalHistorico()), dataHora(p.ultimaGeracao()),
                                    p.competencias().isEmpty() ? "" : String.join(", ", p.competencias()))).toList(),
                            new float[]{2.4f, 1f, 0.9f, 0.8f, 0.9f, 0.8f, 1.3f, 1.4f},
                            new boolean[]{false, false, false, true, true, true, false, false},
                            "Nenhuma folha de ponto gerada no período."))));
        }

        secoes.add(new Secao("Folhas por competência",
                "Competência é o mês/ano de referência da folha (pode ser diferente da data em que foi gerada).",
                List.of(new Tabela(
                        List.of("Competência", "Folhas", "Professores"),
                        r.porCompetencia().stream().map(c -> List.of(
                                c.competencia(), inteiro(c.folhas()), inteiro(c.professores()))).toList(),
                        new float[]{2f, 1f, 1f},
                        new boolean[]{false, true, true},
                        "Nenhuma folha de ponto gerada no período."))));

        secoes.add(new Secao("Gerações por usuário", null, List.of(new Tabela(
                List.of("Usuário", "Folhas geradas"),
                r.porUsuario().stream().map(u -> List.of(u.usuario(), inteiro(u.quantidade()))).toList(),
                new float[]{3f, 1f},
                new boolean[]{false, true},
                "Nenhuma folha de ponto gerada no período."))));

        List<String> cabecalhosHistorico = individual
                ? List.of("Data/hora", "Competência", "Grade utilizada", "Dias sem aula", "Disciplinas", "Gerado por")
                : List.of("Data/hora", "Professor", "Matrícula", "Competência", "Grade utilizada", "Dias sem aula", "Disciplinas", "Gerado por");
        secoes.add(new Secao("Histórico de folhas de ponto geradas",
                "Cada linha corresponde a um download do documento oficial (mais recentes primeiro).",
                List.of(new Tabela(
                        cabecalhosHistorico,
                        r.historico().stream().map(h -> {
                            String grade = textoOuVazio(h.periodoLetivoGrade()) + (h.gradeOutroPeriodo() ? " (outro período)" : "");
                            return individual
                                    ? List.of(dataHora(h.dataHora()), h.competencia(), grade,
                                    inteiro(h.diasSemAula()), inteiro(h.disciplinas()), textoOuVazio(h.geradoPor()))
                                    : List.of(dataHora(h.dataHora()), h.professor(), h.matricula(), h.competencia(), grade,
                                    inteiro(h.diasSemAula()), inteiro(h.disciplinas()), textoOuVazio(h.geradoPor()));
                        }).toList(),
                        individual ? new float[]{1.3f, 1f, 1.6f, 1f, 1f, 1.8f}
                                : new float[]{1.3f, 2.2f, 1f, 1f, 1.5f, 0.9f, 0.9f, 1.5f},
                        individual ? new boolean[]{false, false, false, true, true, false}
                                : new boolean[]{false, false, false, false, false, true, true, false},
                        "Nenhuma folha de ponto gerada no período."))));

        String titulo = individual
                ? "Folhas de ponto de " + professor.nome()
                : "Folhas de ponto de todos os professores";
        String subtitulo = individual
                ? "Relatório " + r.periodo().tipo().getRotulo().toLowerCase(PT_BR)
                + " individual com histórico de geração e grades acadêmicas do professor."
                : "Relatório " + r.periodo().tipo().getRotulo().toLowerCase(PT_BR)
                + " consolidado com comparação entre professores e histórico de geração.";

        return new DocumentoRelatorio(TIPO_FOLHA, titulo, subtitulo, r.periodo().descricao(), r.geradoEm(), geradoPor,
                indicadores, secoes,
                avisoPeriodo(r.periodo())
                        + "Fonte: histórico de geração de folhas de ponto registrado pelo Appointer (download do DOCX oficial). "
                        + "Prévias não são contabilizadas. Gerações anteriores à implantação do histórico não aparecem neste relatório.");
    }

    // =========================================================================
    // Formatação
    // =========================================================================

    private static void adicionarAvisoPeriodo(List<Bloco> blocos, PeriodoRelatorioDTO periodo) {
        if (periodo.emAndamento()) {
            blocos.add(new Paragrafo("Período em andamento: os números são parciais e refletem os registros até o momento da geração."));
        }
    }

    private static String avisoPeriodo(PeriodoRelatorioDTO periodo) {
        return periodo.emAndamento()
                ? "Período em andamento: os números são parciais e refletem os registros até o momento da geração. "
                : "";
    }

    static String inteiro(Number valor) {
        if (valor == null) {
            return "";
        }
        return NumberFormat.getIntegerInstance(PT_BR).format(valor.longValue());
    }

    static String decimal(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        NumberFormat f = NumberFormat.getNumberInstance(PT_BR);
        f.setMinimumFractionDigits(0);
        f.setMaximumFractionDigits(2);
        return f.format(valor);
    }

    static String percentual(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        NumberFormat f = NumberFormat.getNumberInstance(PT_BR);
        f.setMinimumFractionDigits(1);
        f.setMaximumFractionDigits(1);
        return f.format(valor) + "%";
    }

    static String duracao(Long ms) {
        if (ms == null) {
            return "";
        }
        if (ms < 1000) {
            return ms + " ms";
        }
        NumberFormat f = NumberFormat.getNumberInstance(PT_BR);
        f.setMinimumFractionDigits(1);
        f.setMaximumFractionDigits(1);
        return f.format(ms / 1000.0) + " s";
    }

    static String dataHora(LocalDateTime valor) {
        return valor != null ? DATA_HORA.format(valor) : "";
    }

    private static String vigencia(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            return "";
        }
        return DATA.format(inicio) + " a " + DATA.format(fim);
    }

    private static String textoOuVazio(String valor) {
        return valor == null || valor.isBlank() ? "" : valor;
    }

    private static String professorComMatricula(String nome, String matricula) {
        if (nome == null || nome.isBlank()) {
            return "";
        }
        return matricula != null ? nome + " (mat. " + matricula + ")" : nome;
    }

    private static String situacao(StatusExtracaoGrade status) {
        return switch (status) {
            case SUCESSO -> "Sucesso";
            case SUCESSO_COM_AVISOS -> "Com avisos";
            case FALHA -> "Falha";
        };
    }

    /** POS_DOUTOR → "Pos doutor"; mantém nulos como traço. */
    static String enumLegivel(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        String texto = valor.replace('_', ' ').toLowerCase(PT_BR);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
