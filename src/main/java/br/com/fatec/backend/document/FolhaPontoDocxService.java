package br.com.fatec.backend.document;

import br.com.fatec.backend.entity.AulaDaGrade;
import br.com.fatec.backend.entity.Grade;
import br.com.fatec.backend.entity.Professor;
import br.com.fatec.backend.enums.SlotGrade;
import br.com.fatec.backend.enums.Turno;
import br.com.fatec.backend.exception.RegraNegocioException;
import br.com.fatec.backend.service.CalendarioAcademicoService;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço responsável por abrir o template DOCX oficial da Folha de Ponto da FATEC,
 * preencher os dados do professor, grade horária e calendário mensal, e gerar o documento preenchido.
 */
@Service
public class FolhaPontoDocxService {

    private static final Logger log = LoggerFactory.getLogger(FolhaPontoDocxService.class);

    private static final String TEMPLATE_PATH = "/templates/folha-de-ponto-modelo.docx";

    /**
     * Índices das células de Assinatura nas linhas de data da Tabela de Frequência.
     * TC 2 = Assinatura Manhã, TC 9 = Assinatura Tarde, TC 16 = Assinatura Noite.
     */
    private static final int[] INDICES_ASSINATURA = {2, 9, 16};

    private final CalendarioAcademicoService calendarioService;

    public FolhaPontoDocxService(CalendarioAcademicoService calendarioService) {
        this.calendarioService = calendarioService;
    }

    private static final Map<DayOfWeek, String> DIAS_SEMANA_NOMES = Map.of(
            DayOfWeek.MONDAY, "Segunda",
            DayOfWeek.TUESDAY, "Terça",
            DayOfWeek.WEDNESDAY, "Quarta",
            DayOfWeek.THURSDAY, "Quinta",
            DayOfWeek.FRIDAY, "Sexta",
            DayOfWeek.SATURDAY, "Sábado",
            DayOfWeek.SUNDAY, "Domingo"
    );

    /**
     * Gera o documento da Folha de Ponto preenchido em formato binário DOCX (byte array).
     */
    public byte[] gerarDocumento(Professor professor, Grade grade, List<AulaDaGrade> aulas, int mes, int ano) {
        log.info("Iniciando geração de documento da Folha de Ponto para professor ID: {}, mês: {}/{}",
                professor.getId(), mes, ano);

        try (InputStream templateStream = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (templateStream == null) {
                throw new RegraNegocioException("Template oficial '" + TEMPLATE_PATH + "' não encontrado no classpath.");
            }

            try (XWPFDocument documento = new XWPFDocument(templateStream);
                 ByteArrayOutputStream saida = new ByteArrayOutputStream()) {

                // 1. Substituir placeholders do cabeçalho e título
                preencherCabecalhoEPlaceholders(documento, professor, mes, ano);

                // 2. Preencher a Grade Horária (Tabela 1)
                preencherGradeHoraria(documento, aulas);

                // 3. Preencher a tabela da Folha de Frequência (Tabela 2)
                preencherCalendarioFrequencia(documento, mes, ano);

                documento.write(saida);
                log.info("Folha de Ponto gerada com sucesso! Tamanho: {} bytes", saida.size());
                return saida.toByteArray();
            }
        } catch (RegraNegocioException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao gerar a Folha de Ponto em DOCX", e);
            throw new RegraNegocioException("Falha ao gerar o documento DOCX da Folha de Ponto: " + e.getMessage());
        }
    }

    /**
     * Substitui os placeholders de identificação e título no documento.
     */
    private void preencherCabecalhoEPlaceholders(XWPFDocument documento, Professor professor, int mes, int ano) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{{PROFESSOR}}", professor.getNome() != null ? professor.getNome() : "");
        placeholders.put("{{COD}}", professor.getCodigo() != null ? professor.getCodigo() : "");
        placeholders.put("{{MAT}}", professor.getMatricula() != null ? professor.getMatricula() : "");

        String regime = "";
        if (professor.getRegimeJuridico() != null) {
            regime = professor.getRegimeJuridico().name();
        } else if (professor.getRegimeContrato() != null) {
            regime = professor.getRegimeContrato().name();
        }
        placeholders.put("{{RJ}}", regime);

        placeholders.put("{{MES}}", String.format("%02d", mes));
        placeholders.put("{{ANO}}", String.valueOf(ano));

        // Substitui em parágrafos do corpo
        for (XWPFParagraph p : documento.getParagraphs()) {
            substituirPlaceholdersNoParagrafo(p, placeholders);
        }

        // Substitui em tabelas
        for (XWPFTable tabela : documento.getTables()) {
            for (XWPFTableRow linha : tabela.getRows()) {
                for (XWPFTableCell celula : linha.getTableCells()) {
                    for (XWPFParagraph p : celula.getParagraphs()) {
                        substituirPlaceholdersNoParagrafo(p, placeholders);
                    }
                }
            }
        }
    }

    /**
     * Substitui placeholders mesmo quando o Word os dividiu em múltiplos Runs.
     */
    private void substituirPlaceholdersNoParagrafo(XWPFParagraph paragrafo, Map<String, String> placeholders) {
        if (paragrafo == null || paragrafo.getRuns().isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> item : placeholders.entrySet()) {
            String placeholder = item.getKey();
            String valor = item.getValue() != null ? item.getValue() : "";

            while (true) {
                List<XWPFRun> runs = paragrafo.getRuns();
                StringBuilder textoCompleto = new StringBuilder();
                for (XWPFRun r : runs) {
                    String t = r.getText(0);
                    if (t != null) {
                        textoCompleto.append(t);
                    }
                }

                String total = textoCompleto.toString();
                int inicio = total.indexOf(placeholder);
                if (inicio == -1) {
                    break;
                }

                int fim = inicio + placeholder.length();
                int acumulado = 0;
                int runInicio = -1;
                int runFim = -1;
                int inicioNoRun = 0;
                int fimNoRun = 0;

                for (int i = 0; i < runs.size(); i++) {
                    XWPFRun r = runs.get(i);
                    String t = r.getText(0);
                    if (t == null) {
                        continue;
                    }

                    int rStart = acumulado;
                    int rEnd = acumulado + t.length();

                    if (runInicio == -1 && inicio >= rStart && inicio < rEnd) {
                        runInicio = i;
                        inicioNoRun = inicio - rStart;
                    }

                    if (fim > rStart && fim <= rEnd) {
                        runFim = i;
                        fimNoRun = fim - rStart;
                        break;
                    }

                    acumulado = rEnd;
                }

                if (runInicio == -1 || runFim == -1) {
                    break;
                }

                if (runInicio == runFim) {
                    XWPFRun r = runs.get(runInicio);
                    String t = r.getText(0);
                    String novo = t.substring(0, inicioNoRun) + valor + t.substring(fimNoRun);
                    r.setText(novo, 0);
                } else {
                    XWPFRun primeiroRun = runs.get(runInicio);
                    XWPFRun ultimoRun = runs.get(runFim);

                    String tPrimeiro = primeiroRun.getText(0);
                    String tUltimo = ultimoRun.getText(0);

                    primeiroRun.setText(tPrimeiro.substring(0, inicioNoRun) + valor, 0);
                    ultimoRun.setText(tUltimo.substring(fimNoRun), 0);

                    for (int i = runInicio + 1; i < runFim; i++) {
                        runs.get(i).setText("", 0);
                    }
                }
            }
        }
    }

    /**
     * Localiza a tabela da Grade Horária no documento.
     */
    private XWPFTable encontrarTabelaGrade(XWPFDocument doc) {
        for (XWPFTable t : doc.getTables()) {
            StringBuilder sb = new StringBuilder();
            for (XWPFTableRow r : t.getRows()) {
                for (XWPFTableCell c : r.getTableCells()) {
                    sb.append(c.getText().toUpperCase()).append(" ");
                }
            }
            String texto = sb.toString();
            if (texto.contains("MANHÃ") && texto.contains("TARDE") && texto.contains("NOITE")
                    && texto.contains("TEMPO DE AULA 50 MIN")) {
                return t;
            }
        }
        throw new RegraNegocioException("Tabela da Grade Horária não foi localizada no modelo.");
    }

    /**
     * Localiza a tabela da Folha de Frequência no documento.
     */
    private XWPFTable encontrarTabelaFrequencia(XWPFDocument doc) {
        for (XWPFTable t : doc.getTables()) {
            if (t.getRows().size() < 30) {
                continue;
            }
            StringBuilder sb = new StringBuilder();
            for (XWPFTableRow r : t.getRows()) {
                for (XWPFTableCell c : r.getTableCells()) {
                    sb.append(c.getText().toUpperCase()).append(" ");
                }
            }
            String texto = sb.toString();
            if (texto.contains("DIA") && texto.contains("SEMANA") && texto.contains("ASSINATURA")) {
                return t;
            }
        }
        throw new RegraNegocioException("Tabela da Folha de Frequência não foi localizada no modelo.");
    }

    /**
     * Preenche a Grade Horária (Tabela 1) com as siglas das disciplinas.
     */
    private void preencherGradeHoraria(XWPFDocument doc, List<AulaDaGrade> aulas) {
        XWPFTable tabela = encontrarTabelaGrade(doc);

        // Limpa os slots prévios nas linhas 3 a 8 (Segunda a Sábado) sem remover a letra do dia na coluna 0
        for (int r = 3; r <= 8 && r < tabela.getRows().size(); r++) {
            XWPFTableRow row = tabela.getRow(r);
            List<XWPFTableCell> cells = row.getTableCells();
            for (int c = 1; c < cells.size(); c++) {
                limparCelula(cells.get(c));
            }
        }

        if (aulas == null || aulas.isEmpty()) {
            return;
        }

        // Preenche as aulas
        for (AulaDaGrade aula : aulas) {
            Short dia = aula.getDiaSemana();
            // Domingo (7) não recebe aula na grade semanal
            if (dia == null || dia < 1 || dia > 6) {
                continue;
            }

            int linhaIndex = dia + 2; // Segunda (1) -> Linha 3, ..., Sábado (6) -> Linha 8
            if (linhaIndex >= tabela.getRows().size()) {
                continue;
            }
            XWPFTableRow row = tabela.getRow(linhaIndex);

            Turno turno = null;
            if (aula.getDisciplinaDaGrade() != null) {
                if (aula.getDisciplinaDaGrade().getPeriodo() != null && !aula.getDisciplinaDaGrade().getPeriodo().isBlank()) {
                    try {
                        turno = Turno.valueOf(aula.getDisciplinaDaGrade().getPeriodo().trim().toUpperCase());
                    } catch (Exception ignored) {}
                }
                if (turno == null && aula.getDisciplinaDaGrade().getCurso() != null) {
                    turno = aula.getDisciplinaDaGrade().getCurso().getTurno();
                }
            }

            List<SlotGrade> slots = SlotGrade.calcularSlots(turno, aula.getHoraInicio(), aula.getHoraFim());

            String textoGrade = montarTextoGrade(aula);

            for (SlotGrade slot : slots) {
                XWPFTableCell cell = pegarCelulaPorColunaGrid(row, slot.getColunaGrid());
                if (cell != null) {
                    escreverSigla(cell, textoGrade);
                }
            }
        }
    }

    /**
     * Atualiza os dias do mês, nomes dos dias da semana e sombreamento de domingos na Tabela de Frequência.
     * Conforme regra oficial:
     * - Coluna Dia (célula 0) e Coluna Semana (célula 1) NUNCA ficam cinza (permanecem brancos #FFFFFF).
     * - Somente os quadradinhos internos de horários/assinaturas (células 2 a 20) ficam cinza (#D9D9D9) nos domingos.
     * - Fonte dos dias da semana preservada em 8pt negrito centralizado para não quebrar a largura da célula.
     */
    private void preencherCalendarioFrequencia(XWPFDocument doc, int mes, int ano) {
        XWPFTable tabela = encontrarTabelaFrequencia(doc);
        int totalDias = YearMonth.of(ano, mes).lengthOfMonth();

        for (int dia = 1; dia <= 31; dia++) {
            int linhaIndex = dia + 1;
            if (linhaIndex >= tabela.getRows().size()) {
                continue;
            }

            XWPFTableRow row = tabela.getRow(linhaIndex);
            List<XWPFTableCell> cells = row.getTableCells();
            if (cells.size() < 2) {
                continue;
            }

            if (dia > totalDias) {
                // Dias inexistentes (ex: 29, 30, 31 em fevereiro): limpa texto e deixa branco
                for (XWPFTableCell c : cells) {
                    limparCelula(c);
                    aplicarSombreamento(c, "FFFFFF");
                }
            } else {
                LocalDate data = LocalDate.of(ano, mes, dia);
                DayOfWeek dow = data.getDayOfWeek();
                String nomeSemana = DIAS_SEMANA_NOMES.getOrDefault(dow, "");

                // Coluna Dia (célula 0) e Coluna Semana (célula 1): 8pt negrito centralizado
                escreverTextoFormatado(cells.get(0), String.format("%02d", dia), ParagraphAlignment.CENTER, 8, true);
                escreverTextoFormatado(cells.get(1), nomeSemana, ParagraphAlignment.CENTER, 8, true);

                // Coluna Dia (0) e Coluna Semana (1) SEMPRE com fundo branco (#FFFFFF)
                aplicarSombreamento(cells.get(0), "FFFFFF");
                aplicarSombreamento(cells.get(1), "FFFFFF");

                // Somente os quadradinhos internos (célula 2 até 20) ficam cinza nos domingos
                // Cor #808080 (cinza escuro), conforme o template original.
                String corFundoInterno = (dow == DayOfWeek.SUNDAY) ? "808080" : "FFFFFF";
                for (int c = 2; c < cells.size(); c++) {
                    aplicarSombreamento(cells.get(c), corFundoInterno);
                }

                // Texto nos slots de Assinatura (Manhã TC2, Tarde TC9, Noite TC16):
                // Feriados -> "Feriado", Expediente Suspenso -> "Exp. Susp."
                // Calibri, Negrito, Fonte 8, Centralizado. Domingos não recebem texto.
                if (dow != DayOfWeek.SUNDAY) {
                    String textoAssinatura = resolverTextoAssinatura(data);
                    if (textoAssinatura != null) {
                        for (int idx : INDICES_ASSINATURA) {
                            if (idx < cells.size()) {
                                escreverTextoAssinatura(cells.get(idx), textoAssinatura);
                                aplicarSombreamento(cells.get(idx), "FFFFFF");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Retorna o texto a ser exibido nos slots de Assinatura para dias especiais.
     * - Feriado (tipo "FERIADO") -> "Feriado"
     * - Expediente Suspenso (tipo "EMENDA", "RECESSO", "SEM_AULA") -> "Exp. Susp."
     * - Dia normal -> null (sem texto especial)
     */
    private String resolverTextoAssinatura(LocalDate data) {
        if (!calendarioService.isDiaSemAula(data)) {
            return null;
        }
        // Busca o tipo do evento para diferenciar Feriado de Expediente Suspenso
        String motivo = calendarioService.getMotivoDiaSemAula(data);
        if (motivo != null) {
            String motivoUpper = motivo.toUpperCase();
            if (motivoUpper.contains("FERIADO")) {
                return "Feriado";
            }
        }
        return "Exp. Susp.";
    }

    /**
     * Obtém a célula correspondente à coluna visual do grid, considerando células mescladas (gridSpan).
     */
    private XWPFTableCell pegarCelulaPorColunaGrid(XWPFTableRow row, int targetGridCol) {
        if (row == null) {
            return null;
        }
        int currentGridCol = 0;
        for (XWPFTableCell cell : row.getTableCells()) {
            int span = 1;
            if (cell.getCTTc().isSetTcPr() && cell.getCTTc().getTcPr().isSetGridSpan()) {
                span = cell.getCTTc().getTcPr().getGridSpan().getVal().intValue();
            }
            if (targetGridCol >= currentGridCol && targetGridCol < currentGridCol + span) {
                return cell;
            }
            currentGridCol += span;
        }
        return null;
    }

    /**
     * Monta o texto de identificação da aula para a célula da Grade Horária.
     * Formato: "{sigla_disciplina} {semestre}\u00bA{sigla_curso}"
     * Exemplo: "G.C. 4\u00baLM"
     * \u00ba = \u00ba (indicador ordinal masculino).
     */
    private String montarTextoGrade(AulaDaGrade aula) {
        if (aula.getDisciplinaDaGrade() == null) {
            return "";
        }
        String sigla = "";
        if (aula.getDisciplinaDaGrade().getDisciplina() != null) {
            sigla = aula.getDisciplinaDaGrade().getDisciplina().getSigla();
        }
        if (sigla == null || sigla.isBlank()) {
            return "";
        }

        String siglaPontuada = formatarSiglaComPontos(sigla);

        Short semestre = aula.getDisciplinaDaGrade().getSemestreCurso();
        String cursoSigla = "";
        if (aula.getDisciplinaDaGrade().getCurso() != null) {
            cursoSigla = aula.getDisciplinaDaGrade().getCurso().getSigla();
            if (cursoSigla == null) cursoSigla = "";
        }
        if (semestre != null && !cursoSigla.isBlank()) {
            // Usa indicador ordinal masculino U+00BA (\u00ba), ex: "G.C. 4\u00baLM" ou "B.D. 4\u00baADS"
            return siglaPontuada + " " + semestre + "\u00ba" + cursoSigla;
        } else if (semestre != null) {
            return siglaPontuada + " " + semestre + "\u00ba";
        } else if (!cursoSigla.isBlank()) {
            return siglaPontuada + " " + cursoSigla;
        }
        return siglaPontuada;
    }

    /**
     * Pontua a sigla adicionando um ponto após cada caractere, caso ainda não tenha.
     * Exemplo: "BD" -> "B.D.", "GC" -> "G.C.", "B.D." -> "B.D."
     */
    private String formatarSiglaComPontos(String sigla) {
        if (sigla == null || sigla.isBlank()) {
            return "";
        }
        String limpa = sigla.replace(".", "").trim();
        if (limpa.isEmpty()) {
            return sigla;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < limpa.length(); i++) {
            sb.append(limpa.charAt(i)).append('.');
        }
        return sb.toString();
    }

    /**
     * Escreve o texto da grade (sigla + semestre + curso) na célula.
     * Formato: Calibri, 9pt, Sublinhado, Azul (#0000FF), Centralizado.
     * Regra: apenas UMA entrada por slot. Se já houver conteúdo, não substitui.
     */
    private void escreverSigla(XWPFTableCell cell, String texto) {
        if (cell == null || texto == null || texto.isBlank()) {
            return;
        }

        // Apenas 1 por slot: se já tiver texto, não sobrescreve
        if (!cell.getText().trim().isEmpty()) {
            return;
        }

        limparCelula(cell);
        XWPFParagraph p = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = p.createRun();
        r.setText(texto);
        r.setFontFamily("Calibri");
        r.setFontSize(9);
        r.setBold(false);
        r.setUnderline(UnderlinePatterns.SINGLE);
        r.setColor("0000FF");  // Azul #0000FF
    }

    /**
     * Escreve texto nos slots de Assinatura (Feriado / Exp. Susp.).
     * Formato: Calibri, Negrito, 8pt, Centralizado.
     */
    private void escreverTextoAssinatura(XWPFTableCell cell, String texto) {
        if (cell == null || texto == null || texto.isBlank()) {
            return;
        }
        limparCelula(cell);
        XWPFParagraph p = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = p.createRun();
        r.setText(texto);
        r.setFontFamily("Calibri");
        r.setFontSize(8);
        r.setBold(true);
    }

    /**
     * Escreve texto em uma célula com estilo, alinhamento, tamanho e opcionalmente negrito.
     * Usa Calibri como fonte padrão quando Calibri for necessario.
     */
    private void escreverTextoFormatado(XWPFTableCell cell, String texto, ParagraphAlignment alignment, int fontSize, boolean bold) {
        if (cell == null) {
            return;
        }
        limparCelula(cell);
        XWPFParagraph p = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
        p.setAlignment(alignment);
        XWPFRun r = p.createRun();
        r.setText(texto);
        if (fontSize > 0) {
            r.setFontSize(fontSize);
        }
        r.setBold(bold);
    }

    /**
     * Escreve texto em uma célula sem negrito (overload de compatibilidade).
     */
    private void escreverTextoFormatado(XWPFTableCell cell, String texto, ParagraphAlignment alignment, int fontSize) {
        escreverTextoFormatado(cell, texto, alignment, fontSize, false);
    }

    /**
     * Remove todos os runs da célula mantendo a estrutura original da tabela.
     */
    private void limparCelula(XWPFTableCell cell) {
        if (cell == null) {
            return;
        }
        for (XWPFParagraph p : cell.getParagraphs()) {
            for (int i = p.getRuns().size() - 1; i >= 0; i--) {
                p.removeRun(i);
            }
        }
    }

    /**
     * Aplica cor de fundo na célula e remove atributos de tema para garantir renderização idêntica ao modelo.
     */
    private void aplicarSombreamento(XWPFTableCell cell, String hexCor) {
        if (cell == null) {
            return;
        }
        CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();

        if (tcPr.isSetShd()) {
            tcPr.unsetShd();
        }

        CTShd shd = tcPr.addNewShd();
        shd.setVal(STShd.CLEAR);
        shd.setColor("auto");
        shd.setFill(hexCor);

        if (shd.isSetThemeFill()) {
            shd.unsetThemeFill();
        }
        if (shd.isSetThemeFillShade()) {
            shd.unsetThemeFillShade();
        }
        if (shd.isSetThemeFillTint()) {
            shd.unsetThemeFillTint();
        }
    }
}
