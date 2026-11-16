package br.com.fatec.backend.document.relatorio;

import br.com.fatec.backend.document.relatorio.DocumentoRelatorio.*;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Renderiza um {@link DocumentoRelatorio} em PDF (OpenPDF), em A4 com cabeçalho de identificação do
 * Appointer, rodapé com data de geração e numeração "Página X de Y".
 */
@Component
public class RelatorioPdfRenderer {

    static final Color COR_PRIMARIA = new Color(0x1F, 0x3A, 0x5F);
    static final Color COR_DESTAQUE = new Color(0x2E, 0x75, 0xB6);
    static final Color COR_FUNDO_SUAVE = new Color(0xEE, 0xF3, 0xF8);
    static final Color COR_ZEBRA = new Color(0xF7, 0xF9, 0xFC);
    static final Color COR_BORDA = new Color(0xD0, 0xD7, 0xE2);
    static final Color COR_TEXTO = new Color(0x22, 0x22, 0x22);
    static final Color COR_SECUNDARIA = new Color(0x6B, 0x72, 0x80);

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final int INDICADORES_POR_LINHA = 4;

    public byte[] renderizar(DocumentoRelatorio modelo) {
        try {
            // Primeira passagem apenas conta as páginas para permitir "Página X de Y" sem ambiguidade.
            int totalPaginas = gerar(modelo, 0).paginas();
            return gerar(modelo, totalPaginas).conteudo();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar o PDF do relatório.", e);
        }
    }

    private record Resultado(byte[] conteudo, int paginas) {
    }

    private Resultado gerar(DocumentoRelatorio modelo, int totalPaginas) throws Exception {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 40, 40, 72, 56);
        PdfWriter writer = PdfWriter.getInstance(documento, saida);
        CabecalhoRodape eventos = new CabecalhoRodape(modelo, totalPaginas);
        writer.setPageEvent(eventos);

        documento.addTitle(modelo.titulo());
        documento.addSubject(modelo.tipoRelatorio());
        documento.addAuthor("Appointer");
        documento.addCreator("Appointer");
        documento.open();

        escreverAbertura(documento, modelo);
        escreverIndicadores(documento, modelo.indicadores());
        for (Secao secao : modelo.secoes()) {
            // Evita título de seção "órfão" no pé da página.
            if (writer.getVerticalPosition(true) - documento.bottom() < 110) {
                documento.newPage();
            }
            escreverSecao(documento, secao);
        }
        if (modelo.observacaoFinal() != null && !modelo.observacaoFinal().isBlank()) {
            Paragraph obs = new Paragraph(modelo.observacaoFinal(), fonte(7.5f, Font.ITALIC, COR_SECUNDARIA));
            obs.setSpacingBefore(14f);
            documento.add(obs);
        }

        documento.close();
        return new Resultado(saida.toByteArray(), eventos.paginas);
    }

    // -------------------------------------------------------------------------
    // Blocos principais
    // -------------------------------------------------------------------------

    private void escreverAbertura(Document documento, DocumentoRelatorio modelo) throws Exception {
        Paragraph tipo = new Paragraph(modelo.tipoRelatorio().toUpperCase(), fonte(8.5f, Font.BOLD, COR_DESTAQUE));
        tipo.setSpacingAfter(2f);
        documento.add(tipo);

        Paragraph titulo = new Paragraph(modelo.titulo(), fonte(18f, Font.BOLD, COR_PRIMARIA));
        titulo.setSpacingAfter(2f);
        documento.add(titulo);

        if (modelo.subtitulo() != null && !modelo.subtitulo().isBlank()) {
            Paragraph subtitulo = new Paragraph(modelo.subtitulo(), fonte(9.5f, Font.NORMAL, COR_SECUNDARIA));
            subtitulo.setSpacingAfter(8f);
            documento.add(subtitulo);
        }

        PdfPTable meta = new PdfPTable(3);
        meta.setWidthPercentage(100f);
        meta.setWidths(new float[]{2.2f, 1.4f, 1.4f});
        meta.setSpacingBefore(4f);
        meta.setSpacingAfter(10f);
        meta.addCell(celulaMeta("Período analisado", modelo.periodoDescricao()));
        meta.addCell(celulaMeta("Data de geração", modelo.geradoEm() != null ? DATA_HORA.format(modelo.geradoEm()) : ""));
        meta.addCell(celulaMeta("Gerado por", modelo.geradoPor() != null ? modelo.geradoPor() : ""));
        documento.add(meta);
    }

    private PdfPCell celulaMeta(String rotulo, String valor) {
        Phrase conteudo = new Phrase();
        conteudo.add(new Chunk(rotulo.toUpperCase() + "\n", fonte(6.5f, Font.BOLD, COR_SECUNDARIA)));
        conteudo.add(new Chunk(valor != null ? valor : "", fonte(9.5f, Font.BOLD, COR_TEXTO)));
        PdfPCell cell = new PdfPCell(conteudo);
        cell.setBackgroundColor(COR_FUNDO_SUAVE);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(7f);
        cell.setLeading(0f, 1.25f);
        return cell;
    }

    private void escreverIndicadores(Document documento, List<Indicador> indicadores) throws Exception {
        if (indicadores.isEmpty()) {
            return;
        }
        int colunas = Math.min(INDICADORES_POR_LINHA, indicadores.size());
        PdfPTable tabela = new PdfPTable(colunas);
        tabela.setWidthPercentage(100f);
        tabela.setSpacingAfter(12f);
        for (Indicador indicador : indicadores) {
            Phrase conteudo = new Phrase();
            conteudo.add(new Chunk(indicador.valor() + "\n", fonte(15f, Font.BOLD, COR_PRIMARIA)));
            conteudo.add(new Chunk(indicador.rotulo(), fonte(7.5f, Font.NORMAL, COR_SECUNDARIA)));
            PdfPCell cell = new PdfPCell(conteudo);
            cell.setBorder(Rectangle.LEFT);
            cell.setBorderWidth(3f);
            cell.setBorderColor(COR_DESTAQUE);
            cell.setBackgroundColor(COR_FUNDO_SUAVE);
            cell.setPadding(8f);
            cell.setLeading(0f, 1.2f);
            tabela.addCell(cell);
        }
        PdfPCell vazia = new PdfPCell(new Phrase(""));
        vazia.setBorder(Rectangle.NO_BORDER);
        tabela.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        int restantes = (colunas - indicadores.size() % colunas) % colunas;
        for (int i = 0; i < restantes; i++) {
            tabela.addCell(vazia);
        }
        documento.add(tabela);
    }

    private void escreverSecao(Document documento, Secao secao) throws Exception {
        PdfPTable titulo = new PdfPTable(1);
        titulo.setWidthPercentage(100f);
        titulo.setSpacingBefore(10f);
        titulo.setSpacingAfter(4f);
        PdfPCell cell = new PdfPCell(new Phrase(secao.titulo(), fonte(12f, Font.BOLD, COR_PRIMARIA)));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderWidth(1.2f);
        cell.setBorderColor(COR_DESTAQUE);
        cell.setPaddingBottom(4f);
        cell.setPaddingLeft(0f);
        titulo.addCell(cell);
        documento.add(titulo);

        if (secao.descricao() != null && !secao.descricao().isBlank()) {
            Paragraph descricao = new Paragraph(secao.descricao(), fonte(8.5f, Font.NORMAL, COR_SECUNDARIA));
            descricao.setSpacingAfter(4f);
            documento.add(descricao);
        }

        for (Bloco bloco : secao.blocos()) {
            switch (bloco) {
                case Paragrafo p -> {
                    Paragraph paragrafo = new Paragraph(p.texto(), fonte(9f, Font.NORMAL, COR_TEXTO));
                    paragrafo.setSpacingAfter(4f);
                    documento.add(paragrafo);
                }
                case Subtitulo s -> {
                    Paragraph subtitulo = new Paragraph(s.texto(), fonte(10f, Font.BOLD, COR_TEXTO));
                    subtitulo.setSpacingBefore(6f);
                    subtitulo.setSpacingAfter(2f);
                    documento.add(subtitulo);
                }
                case Tabela t -> escreverTabela(documento, t);
                case FichaChaveValor f -> escreverFicha(documento, f);
            }
        }
    }

    private void escreverTabela(Document documento, Tabela t) throws Exception {
        if (t.linhas().isEmpty()) {
            Paragraph vazio = new Paragraph(t.mensagemVazia() != null ? t.mensagemVazia() : "Nenhum registro no período.",
                    fonte(8.5f, Font.ITALIC, COR_SECUNDARIA));
            vazio.setSpacingAfter(6f);
            documento.add(vazio);
            return;
        }
        int colunas = t.cabecalhos().size();
        PdfPTable tabela = new PdfPTable(colunas);
        tabela.setWidthPercentage(100f);
        if (t.largurasRelativas() != null && t.largurasRelativas().length == colunas) {
            tabela.setWidths(t.largurasRelativas());
        }
        tabela.setHeaderRows(1);
        tabela.setSpacingBefore(2f);
        tabela.setSpacingAfter(8f);

        for (int c = 0; c < colunas; c++) {
            PdfPCell cab = new PdfPCell(new Phrase(t.cabecalhos().get(c), fonte(7.5f, Font.BOLD, Color.WHITE)));
            cab.setBackgroundColor(COR_PRIMARIA);
            cab.setBorderColor(COR_PRIMARIA);
            cab.setPadding(5f);
            cab.setHorizontalAlignment(direita(t, c) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
            cab.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tabela.addCell(cab);
        }

        for (int l = 0; l < t.linhas().size(); l++) {
            List<String> linha = t.linhas().get(l);
            Color fundo = l % 2 == 0 ? Color.WHITE : COR_ZEBRA;
            for (int c = 0; c < colunas; c++) {
                String valor = c < linha.size() ? linha.get(c) : "";
                PdfPCell cell = new PdfPCell(new Phrase(valor, fonte(7.5f, Font.NORMAL, COR_TEXTO)));
                cell.setBackgroundColor(fundo);
                cell.setBorder(Rectangle.BOTTOM);
                cell.setBorderWidth(0.5f);
                cell.setBorderColor(COR_BORDA);
                cell.setPadding(4f);
                cell.setHorizontalAlignment(direita(t, c) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                tabela.addCell(cell);
            }
        }
        documento.add(tabela);
    }

    private void escreverFicha(Document documento, FichaChaveValor ficha) throws Exception {
        PdfPTable tabela = new PdfPTable(4);
        tabela.setWidthPercentage(100f);
        tabela.setWidths(new float[]{1.2f, 2f, 1.2f, 2f});
        tabela.setSpacingBefore(2f);
        tabela.setSpacingAfter(8f);
        for (Indicador item : ficha.itens()) {
            PdfPCell rotulo = new PdfPCell(new Phrase(item.rotulo(), fonte(7.5f, Font.BOLD, COR_SECUNDARIA)));
            rotulo.setBackgroundColor(COR_FUNDO_SUAVE);
            rotulo.setBorderColor(COR_BORDA);
            rotulo.setBorderWidth(0.5f);
            rotulo.setPadding(5f);
            tabela.addCell(rotulo);

            PdfPCell valor = new PdfPCell(new Phrase(item.valor() != null ? item.valor() : "", fonte(8.5f, Font.NORMAL, COR_TEXTO)));
            valor.setBorderColor(COR_BORDA);
            valor.setBorderWidth(0.5f);
            valor.setPadding(5f);
            tabela.addCell(valor);
        }
        if (ficha.itens().size() % 2 != 0) {
            PdfPCell vazia = new PdfPCell(new Phrase(""));
            vazia.setColspan(2);
            vazia.setBorderColor(COR_BORDA);
            vazia.setBorderWidth(0.5f);
            tabela.addCell(vazia);
        }
        documento.add(tabela);
    }

    private static boolean direita(Tabela t, int coluna) {
        return t.alinharDireita() != null && coluna < t.alinharDireita().length && t.alinharDireita()[coluna];
    }

    static Font fonte(float tamanho, int estilo, Color cor) {
        return new Font(Font.HELVETICA, tamanho, estilo, cor);
    }

    // -------------------------------------------------------------------------
    // Cabeçalho e rodapé de todas as páginas
    // -------------------------------------------------------------------------

    private static final class CabecalhoRodape extends PdfPageEventHelper {

        private final DocumentoRelatorio modelo;
        private final int totalPaginas;
        private int paginas;

        private CabecalhoRodape(DocumentoRelatorio modelo, int totalPaginas) {
            this.modelo = modelo;
            this.totalPaginas = totalPaginas;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            paginas = writer.getPageNumber();
            PdfContentByte cb = writer.getDirectContent();
            float esquerda = document.left();
            float direita = document.right();
            float altura = document.getPageSize().getHeight();

            // Cabeçalho
            Phrase marca = new Phrase();
            marca.add(new Chunk("APPOINTER", fonte(13f, Font.BOLD, COR_PRIMARIA)));
            marca.add(new Chunk("   Sistema de Gestão Acadêmica · FATEC Zona Leste", fonte(7.5f, Font.NORMAL, COR_SECUNDARIA)));
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, marca, esquerda, altura - 40, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase(modelo.tipoRelatorio(), fonte(7.5f, Font.NORMAL, COR_SECUNDARIA)), direita, altura - 40, 0);
            cb.saveState();
            cb.setColorStroke(COR_DESTAQUE);
            cb.setLineWidth(1.2f);
            cb.moveTo(esquerda, altura - 48);
            cb.lineTo(direita, altura - 48);
            cb.stroke();

            // Rodapé
            cb.setColorStroke(COR_BORDA);
            cb.setLineWidth(0.5f);
            cb.moveTo(esquerda, 40);
            cb.lineTo(direita, 40);
            cb.stroke();
            cb.restoreState();

            String gerado = modelo.geradoEm() != null ? "Relatório gerado em " + DATA_HORA.format(modelo.geradoEm()) : "";
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase("Appointer · " + gerado, fonte(7f, Font.NORMAL, COR_SECUNDARIA)), esquerda, 28, 0);
            String pagina = totalPaginas > 0
                    ? "Página " + paginas + " de " + totalPaginas
                    : "Página " + paginas;
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase(pagina, fonte(7f, Font.NORMAL, COR_SECUNDARIA)), direita, 28, 0);
        }
    }
}
