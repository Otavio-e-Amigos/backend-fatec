package br.com.fatec.backend.document.relatorio;

import br.com.fatec.backend.document.relatorio.DocumentoRelatorio.*;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Renderiza um {@link DocumentoRelatorio} em DOCX (Apache POI), com o mesmo conteúdo e identidade
 * visual do PDF: página A4, cabeçalho do Appointer, rodapé com data de geração e "Página X de Y".
 */
@Component
public class RelatorioDocxRenderer {

    private static final String NS_W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
    private static final String FONTE = "Calibri";
    private static final String COR_PRIMARIA = "1F3A5F";
    private static final String COR_DESTAQUE = "2E75B6";
    private static final String COR_FUNDO_SUAVE = "EEF3F8";
    private static final String COR_ZEBRA = "F7F9FC";
    private static final String COR_BORDA = "D0D7E2";
    private static final String COR_TEXTO = "222222";
    private static final String COR_SECUNDARIA = "6B7280";
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final int INDICADORES_POR_LINHA = 4;
    /** Largura útil da página A4 com margens de 2 cm, em twips. */
    private static final int LARGURA_UTIL = 11906 - 2 * 1134;

    public byte[] renderizar(DocumentoRelatorio modelo) {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            doc.getProperties().getCoreProperties().setTitle(modelo.titulo());
            doc.getProperties().getCoreProperties().setCreator("Appointer");

            configurarCabecalho(doc, modelo);
            configurarRodape(doc, modelo);
            configurarPaginaA4(doc);

            escreverAbertura(doc, modelo);
            escreverIndicadores(doc, modelo.indicadores());
            for (Secao secao : modelo.secoes()) {
                escreverSecao(doc, secao);
            }
            if (modelo.observacaoFinal() != null && !modelo.observacaoFinal().isBlank()) {
                XWPFParagraph obs = doc.createParagraph();
                obs.setSpacingBefore(240);
                adicionarTexto(obs, modelo.observacaoFinal(), false, 8, COR_SECUNDARIA).setItalic(true);
            }

            doc.write(saida);
            return saida.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar o DOCX do relatório.", e);
        }
    }

    // -------------------------------------------------------------------------
    // Página, cabeçalho e rodapé
    // -------------------------------------------------------------------------

    private void configurarCabecalho(XWPFDocument doc, DocumentoRelatorio modelo) {
        XWPFHeader header = doc.createHeader(HeaderFooterType.DEFAULT);
        XWPFParagraph p = header.createParagraph();
        p.setBorderBottom(Borders.SINGLE);
        p.setSpacingAfter(120);
        adicionarTexto(p, "APPOINTER", true, 14, COR_PRIMARIA);
        adicionarTexto(p, "   Sistema de Gestão Acadêmica · FATEC Zona Leste   |   " + modelo.tipoRelatorio(), false, 8, COR_SECUNDARIA);
    }

    /**
     * O rodapé usa campos PAGE e NUMPAGES. O parágrafo é montado em XML para garantir a ordem
     * texto/campo (a API de runs do POI acrescenta campos fora de ordem).
     */
    private void configurarRodape(XWPFDocument doc, DocumentoRelatorio modelo) throws Exception {
        XWPFFooter footer = doc.createFooter(HeaderFooterType.DEFAULT);
        XWPFParagraph p = footer.createParagraph();
        String gerado = modelo.geradoEm() != null ? "Relatório gerado em " + DATA_HORA.format(modelo.geradoEm()) + "   ·   " : "";

        String xml = "<xml-fragment xmlns:w=\"" + NS_W + "\">"
                + "<w:pPr><w:pBdr><w:top w:val=\"single\" w:sz=\"4\" w:space=\"4\" w:color=\"" + COR_BORDA + "\"/></w:pBdr>"
                + "<w:jc w:val=\"center\"/></w:pPr>"
                + runXml("Appointer   ·   " + gerado + "Página ")
                + campoXml("PAGE")
                + runXml(" de ")
                + campoXml("NUMPAGES")
                + "</xml-fragment>";
        p.getCTP().set(CTP.Factory.parse(xml));
    }

    private static String runXml(String texto) {
        return "<w:r>" + rPrRodape() + "<w:t xml:space=\"preserve\">" + escaparXml(texto) + "</w:t></w:r>";
    }

    private static String campoXml(String instrucao) {
        return "<w:fldSimple w:instr=\" " + instrucao + " \"><w:r>" + rPrRodape() + "<w:t>1</w:t></w:r></w:fldSimple>";
    }

    private static String rPrRodape() {
        return "<w:rPr><w:rFonts w:ascii=\"" + FONTE + "\" w:hAnsi=\"" + FONTE + "\"/>"
                + "<w:color w:val=\"" + COR_SECUNDARIA + "\"/><w:sz w:val=\"16\"/></w:rPr>";
    }

    /** Define A4 (11906 x 16838 twips) e margens de 2 cm sem depender das classes "full" do schema OOXML. */
    private void configurarPaginaA4(XWPFDocument doc) {
        CTSectPr sectPr = doc.getDocument().getBody().isSetSectPr()
                ? doc.getDocument().getBody().getSectPr()
                : doc.getDocument().getBody().addNewSectPr();
        XmlCursor cursor = sectPr.newCursor();
        try {
            cursor.toEndToken();
            cursor.beginElement(new QName(NS_W, "pgSz", "w"));
            cursor.insertAttributeWithValue(new QName(NS_W, "w", "w"), "11906");
            cursor.insertAttributeWithValue(new QName(NS_W, "h", "w"), "16838");
            cursor.toNextToken();
            cursor.beginElement(new QName(NS_W, "pgMar", "w"));
            cursor.insertAttributeWithValue(new QName(NS_W, "top", "w"), "1247");
            cursor.insertAttributeWithValue(new QName(NS_W, "right", "w"), "1134");
            cursor.insertAttributeWithValue(new QName(NS_W, "bottom", "w"), "1134");
            cursor.insertAttributeWithValue(new QName(NS_W, "left", "w"), "1134");
            cursor.insertAttributeWithValue(new QName(NS_W, "header", "w"), "567");
            cursor.insertAttributeWithValue(new QName(NS_W, "footer", "w"), "567");
            cursor.insertAttributeWithValue(new QName(NS_W, "gutter", "w"), "0");
        } finally {
            cursor.dispose();
        }
    }

    // -------------------------------------------------------------------------
    // Conteúdo
    // -------------------------------------------------------------------------

    private void escreverAbertura(XWPFDocument doc, DocumentoRelatorio modelo) {
        XWPFParagraph tipo = doc.createParagraph();
        tipo.setSpacingAfter(0);
        adicionarTexto(tipo, modelo.tipoRelatorio().toUpperCase(), true, 9, COR_DESTAQUE);

        XWPFParagraph titulo = doc.createParagraph();
        titulo.setSpacingAfter(40);
        adicionarTexto(titulo, modelo.titulo(), true, 20, COR_PRIMARIA);

        if (modelo.subtitulo() != null && !modelo.subtitulo().isBlank()) {
            XWPFParagraph subtitulo = doc.createParagraph();
            subtitulo.setSpacingAfter(160);
            adicionarTexto(subtitulo, modelo.subtitulo(), false, 10, COR_SECUNDARIA);
        }

        int[] larguras = {44, 28, 28};
        XWPFTable meta = criarTabela(doc, 1, larguras);
        definirBordas(meta, XWPFTable.XWPFBorderType.SINGLE, 12, "FFFFFF");
        String[][] dados = {
                {"PERÍODO ANALISADO", modelo.periodoDescricao()},
                {"DATA DE GERAÇÃO", modelo.geradoEm() != null ? DATA_HORA.format(modelo.geradoEm()) : ""},
                {"GERADO POR", modelo.geradoPor() != null ? modelo.geradoPor() : ""}
        };
        for (int c = 0; c < 3; c++) {
            XWPFTableCell cell = meta.getRow(0).getCell(c);
            cell.setWidth(twips(larguras[c]));
            cell.setColor(COR_FUNDO_SUAVE);
            escreverNaCelula(cell, dados[c][0], true, 7, COR_SECUNDARIA, ParagraphAlignment.LEFT);
            XWPFParagraph valor = cell.addParagraph();
            adicionarTexto(valor, dados[c][1] != null ? dados[c][1] : "", true, 10, COR_TEXTO);
        }
        espaco(doc, 160);
    }

    private void escreverIndicadores(XWPFDocument doc, List<Indicador> indicadores) {
        if (indicadores.isEmpty()) {
            return;
        }
        int colunas = Math.min(INDICADORES_POR_LINHA, indicadores.size());
        int linhas = (indicadores.size() + colunas - 1) / colunas;
        int[] larguras = new int[colunas];
        java.util.Arrays.fill(larguras, 100 / colunas);
        XWPFTable tabela = criarTabela(doc, linhas, larguras);
        definirBordas(tabela, XWPFTable.XWPFBorderType.SINGLE, 18, "FFFFFF");
        for (int i = 0; i < linhas * colunas; i++) {
            XWPFTableCell cell = tabela.getRow(i / colunas).getCell(i % colunas);
            cell.setWidth(twips(larguras[i % colunas]));
            if (i >= indicadores.size()) {
                continue;
            }
            Indicador indicador = indicadores.get(i);
            cell.setColor(COR_FUNDO_SUAVE);
            escreverNaCelula(cell, indicador.valor(), true, 18, COR_PRIMARIA, ParagraphAlignment.LEFT);
            XWPFParagraph rotulo = cell.addParagraph();
            adicionarTexto(rotulo, indicador.rotulo(), false, 8, COR_SECUNDARIA);
        }
        espaco(doc, 200);
    }

    private void escreverSecao(XWPFDocument doc, Secao secao) {
        XWPFParagraph titulo = doc.createParagraph();
        titulo.setSpacingBefore(240);
        titulo.setSpacingAfter(80);
        titulo.setBorderBottom(Borders.SINGLE);
        titulo.setKeepNext(true);
        adicionarTexto(titulo, secao.titulo(), true, 13, COR_PRIMARIA);

        if (secao.descricao() != null && !secao.descricao().isBlank()) {
            XWPFParagraph descricao = doc.createParagraph();
            descricao.setSpacingAfter(80);
            descricao.setKeepNext(true);
            adicionarTexto(descricao, secao.descricao(), false, 9, COR_SECUNDARIA);
        }

        for (Bloco bloco : secao.blocos()) {
            switch (bloco) {
                case Paragrafo p -> {
                    XWPFParagraph paragrafo = doc.createParagraph();
                    paragrafo.setSpacingAfter(80);
                    adicionarTexto(paragrafo, p.texto(), false, 10, COR_TEXTO);
                }
                case Subtitulo s -> {
                    XWPFParagraph subtitulo = doc.createParagraph();
                    subtitulo.setSpacingBefore(120);
                    subtitulo.setSpacingAfter(40);
                    subtitulo.setKeepNext(true);
                    adicionarTexto(subtitulo, s.texto(), true, 11, COR_TEXTO);
                }
                case Tabela t -> escreverTabela(doc, t);
                case FichaChaveValor f -> escreverFicha(doc, f);
            }
        }
    }

    private void escreverTabela(XWPFDocument doc, Tabela t) {
        if (t.linhas().isEmpty()) {
            XWPFParagraph vazio = doc.createParagraph();
            vazio.setSpacingAfter(120);
            adicionarTexto(vazio, t.mensagemVazia() != null ? t.mensagemVazia() : "Nenhum registro no período.",
                    false, 9, COR_SECUNDARIA).setItalic(true);
            return;
        }
        int colunas = t.cabecalhos().size();
        int[] larguras = percentuais(t.largurasRelativas(), colunas);
        XWPFTable tabela = criarTabela(doc, t.linhas().size() + 1, larguras);
        definirBordas(tabela, XWPFTable.XWPFBorderType.SINGLE, 4, COR_BORDA);

        XWPFTableRow cabecalho = tabela.getRow(0);
        cabecalho.setRepeatHeader(true);
        cabecalho.setCantSplitRow(true);
        for (int c = 0; c < colunas; c++) {
            XWPFTableCell cell = cabecalho.getCell(c);
            cell.setWidth(twips(larguras[c]));
            cell.setColor(COR_PRIMARIA);
            escreverNaCelula(cell, t.cabecalhos().get(c), true, 8, "FFFFFF", alinhamento(t, c));
        }

        for (int l = 0; l < t.linhas().size(); l++) {
            XWPFTableRow row = tabela.getRow(l + 1);
            row.setCantSplitRow(true);
            List<String> linha = t.linhas().get(l);
            for (int c = 0; c < colunas; c++) {
                XWPFTableCell cell = row.getCell(c);
                cell.setWidth(twips(larguras[c]));
                if (l % 2 == 1) {
                    cell.setColor(COR_ZEBRA);
                }
                escreverNaCelula(cell, c < linha.size() ? linha.get(c) : "", false, 8, COR_TEXTO, alinhamento(t, c));
            }
        }
        espaco(doc, 120);
    }

    private void escreverFicha(XWPFDocument doc, FichaChaveValor ficha) {
        int linhas = (ficha.itens().size() + 1) / 2;
        int[] larguras = {18, 32, 18, 32};
        XWPFTable tabela = criarTabela(doc, linhas, larguras);
        definirBordas(tabela, XWPFTable.XWPFBorderType.SINGLE, 4, COR_BORDA);
        for (int i = 0; i < linhas * 2; i++) {
            XWPFTableRow row = tabela.getRow(i / 2);
            XWPFTableCell rotulo = row.getCell((i % 2) * 2);
            XWPFTableCell valor = row.getCell((i % 2) * 2 + 1);
            rotulo.setWidth(twips(larguras[(i % 2) * 2]));
            valor.setWidth(twips(larguras[(i % 2) * 2 + 1]));
            rotulo.setColor(COR_FUNDO_SUAVE);
            if (i >= ficha.itens().size()) {
                continue;
            }
            Indicador item = ficha.itens().get(i);
            escreverNaCelula(rotulo, item.rotulo(), true, 8, COR_SECUNDARIA, ParagraphAlignment.LEFT);
            escreverNaCelula(valor, item.valor() != null ? item.valor() : "", false, 9, COR_TEXTO, ParagraphAlignment.LEFT);
        }
        espaco(doc, 120);
    }

    // -------------------------------------------------------------------------
    // Utilitários
    // -------------------------------------------------------------------------

    /**
     * Cria a tabela com grade de colunas explícita (tblGrid em twips), para que Word e LibreOffice
     * respeitem as proporções definidas no relatório.
     */
    private XWPFTable criarTabela(XWPFDocument doc, int linhas, int[] percentuais) {
        XWPFTable tabela = doc.createTable(linhas, percentuais.length);
        tabela.setWidth(String.valueOf(LARGURA_UTIL));
        tabela.setCellMargins(60, 100, 60, 100);
        CTTblGrid grade = tabela.getCTTbl().getTblGrid() != null
                ? tabela.getCTTbl().getTblGrid()
                : tabela.getCTTbl().addNewTblGrid();
        while (grade.sizeOfGridColArray() > 0) {
            grade.removeGridCol(0);
        }
        for (int percentual : percentuais) {
            grade.addNewGridCol().setW(BigInteger.valueOf(Long.parseLong(twips(percentual))));
        }
        return tabela;
    }

    private static String twips(int percentual) {
        return String.valueOf(LARGURA_UTIL * percentual / 100);
    }

    private void definirBordas(XWPFTable tabela, XWPFTable.XWPFBorderType tipo, int tamanho, String cor) {
        tabela.setTopBorder(tipo, tamanho, 0, cor);
        tabela.setBottomBorder(tipo, tamanho, 0, cor);
        tabela.setLeftBorder(tipo, tamanho, 0, cor);
        tabela.setRightBorder(tipo, tamanho, 0, cor);
        tabela.setInsideHBorder(tipo, tamanho, 0, cor);
        tabela.setInsideVBorder(tipo, tamanho, 0, cor);
    }

    private void escreverNaCelula(XWPFTableCell cell, String texto, boolean negrito, int tamanho, String cor,
                                  ParagraphAlignment alinhamento) {
        XWPFParagraph p = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
        p.setAlignment(alinhamento);
        p.setSpacingAfter(0);
        p.setSpacingBefore(0);
        adicionarTexto(p, texto, negrito, tamanho, cor);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
    }

    private XWPFRun adicionarTexto(XWPFParagraph p, String texto, boolean negrito, int tamanho, String cor) {
        XWPFRun run = p.createRun();
        run.setText(texto != null ? texto : "");
        run.setBold(negrito);
        run.setFontSize(tamanho);
        run.setColor(cor);
        run.setFontFamily(FONTE);
        return run;
    }

    private void espaco(XWPFDocument doc, int twips) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingAfter(0);
        p.setSpacingBefore(0);
        p.setSpacingAfter(twips);
    }

    private static ParagraphAlignment alinhamento(Tabela t, int coluna) {
        boolean direita = t.alinharDireita() != null && coluna < t.alinharDireita().length && t.alinharDireita()[coluna];
        return direita ? ParagraphAlignment.RIGHT : ParagraphAlignment.LEFT;
    }

    private static int[] percentuais(float[] relativas, int colunas) {
        int[] resultado = new int[colunas];
        if (relativas == null || relativas.length != colunas) {
            for (int i = 0; i < colunas; i++) {
                resultado[i] = 100 / colunas;
            }
            return resultado;
        }
        float soma = 0;
        for (float r : relativas) {
            soma += r;
        }
        for (int i = 0; i < colunas; i++) {
            resultado[i] = Math.max(1, Math.round(relativas[i] * 100f / soma));
        }
        return resultado;
    }

    static String escaparXml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
