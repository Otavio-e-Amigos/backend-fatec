package br.com.fatec.backend.document;

import br.com.fatec.backend.entity.AulaDaGrade;
import br.com.fatec.backend.entity.Grade;
import br.com.fatec.backend.entity.Professor;
import br.com.fatec.backend.exception.RegraNegocioException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Serviço responsável por gerar a Folha de Ponto em formato PDF.
 * Aproveita 100% da estrutura e das regras de preenchimento do modelo oficial DOCX:
 * recebe o documento DOCX já preenchido e converte fielmente todos os seus parágrafos,
 * tabelas, cores de fundo (shading de domingos e cabeçalhos), larguras de grid e mesclagens para PDF.
 */
@Service
public class FolhaPontoPdfService {

    private static final Logger log = LoggerFactory.getLogger(FolhaPontoPdfService.class);

    private final FolhaPontoDocxService folhaPontoDocxService;

    public FolhaPontoPdfService(FolhaPontoDocxService folhaPontoDocxService) {
        this.folhaPontoDocxService = folhaPontoDocxService;
    }

    /**
     * Gera o documento da Folha de Ponto em formato PDF a partir dos dados do professor,
     * grade e calendário.
     * Reaproveita a geração do DOCX oficial e o converte para PDF, garantindo que o layout
     * seja idêntico ao modelo oficial sem duplicação de regras.
     */
    public byte[] gerarDocumento(Professor professor, Grade grade, List<AulaDaGrade> aulas, int mes, int ano) {
        log.info("Gerando Folha de Ponto em PDF (via DOCX oficial) para professor ID: {}, mês: {}/{}",
                professor.getId(), mes, ano);

        // 1. Gera o DOCX oficial preenchido com todas as regras de negócio
        byte[] docxBytes = folhaPontoDocxService.gerarDocumento(professor, grade, aulas, mes, ano);

        // 2. Converte o DOCX resultante fielmente para PDF
        return converterDocxParaPdf(docxBytes);
    }

    /**
     * Converte um arquivo DOCX (da Folha de Ponto oficial) diretamente em PDF,
     * preservando a orientação A4, tabelas, células mescladas (gridSpan)
     * e cores de fundo (incluindo cinza dos domingos).
     */
    public byte[] converterDocxParaPdf(byte[] docxBytes) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(docxBytes);
             XWPFDocument doc = new XWPFDocument(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Template oficial é A4 Retrato com margens compactas (~20pt) para caber todo o conteúdo
            Document pdfDoc = new Document(PageSize.A4, 20f, 20f, 20f, 20f);
            PdfWriter.getInstance(pdfDoc, out);
            pdfDoc.open();

            for (IBodyElement element : doc.getBodyElements()) {
                if (element instanceof XWPFParagraph p) {
                    processarParagrafo(p, pdfDoc);
                } else if (element instanceof XWPFTable t) {
                    processarTabela(t, pdfDoc);
                }
            }

            pdfDoc.close();
            log.info("PDF da Folha de Ponto gerado com sucesso! Tamanho: {} bytes", out.size());
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Erro ao converter DOCX da Folha de Ponto para PDF", e);
            throw new RegraNegocioException("Falha ao gerar o documento PDF da Folha de Ponto: " + e.getMessage());
        }
    }

    private void processarParagrafo(XWPFParagraph p, Document pdfDoc) throws DocumentException {
        String texto = p.getText();
        if (texto == null || texto.isBlank()) {
            return;
        }

        boolean negrito = false;
        int tamanhoFonte = 8;
        if (!p.getRuns().isEmpty()) {
            XWPFRun r = p.getRuns().get(0);
            negrito = r.isBold();
            Double fontSize = r.getFontSizeAsDouble();
            if (fontSize != null && fontSize > 0) {
                tamanhoFonte = (int) Math.round(fontSize);
            }
        }

        Font font = FontFactory.getFont(
                FontFactory.HELVETICA,
                Math.max(6, Math.min(tamanhoFonte, 11)),
                negrito ? Font.BOLD : Font.NORMAL,
                Color.BLACK
        );

        Paragraph pdfParagraph = new Paragraph(texto.trim(), font);

        if (p.getAlignment() != null) {
            switch (p.getAlignment()) {
                case CENTER -> pdfParagraph.setAlignment(Element.ALIGN_CENTER);
                case RIGHT -> pdfParagraph.setAlignment(Element.ALIGN_RIGHT);
                default -> pdfParagraph.setAlignment(Element.ALIGN_LEFT);
            }
        }

        pdfParagraph.setSpacingBefore(1f);
        pdfParagraph.setSpacingAfter(2f);
        pdfDoc.add(pdfParagraph);
    }

    private void processarTabela(XWPFTable table, Document pdfDoc) throws DocumentException {
        int totalColsGrid = calcularMaximoColunas(table);
        PdfPTable pdfTable = new PdfPTable(Math.max(1, totalColsGrid));

        pdfTable.setWidthPercentage(100f);
        pdfTable.setSpacingBefore(1f);
        pdfTable.setSpacingAfter(3f);

        // Ajustar tamanho de fonte conforme a densidade da tabela
        float fontSize = (totalColsGrid >= 20) ? 5.0f : (totalColsGrid >= 10 ? 5.5f : 6.5f);

        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                int colspan = 1;
                if (cell.getCTTc().isSetTcPr() && cell.getCTTc().getTcPr().isSetGridSpan()) {
                    colspan = cell.getCTTc().getTcPr().getGridSpan().getVal().intValue();
                }

                // Cor de fundo da célula (shading)
                Color bgColor = Color.WHITE;
                if (cell.getCTTc().isSetTcPr() && cell.getCTTc().getTcPr().isSetShd()) {
                    CTShd shd = cell.getCTTc().getTcPr().getShd();
                    Object fillObj = shd.getFill();
                    if (fillObj != null) {
                        String hex = fillObj.toString().replace("#", "").trim();
                        if (!hex.equalsIgnoreCase("auto") && hex.length() == 6) {
                            try {
                                bgColor = new Color(Integer.parseInt(hex, 16));
                            } catch (Exception ignored) {}
                        }
                    }
                }

                String textoCelula = cell.getText().trim();
                boolean isHeader = bgColor.getRed() < 240 && bgColor.getRed() == bgColor.getGreen() && bgColor.getRed() == bgColor.getBlue();

                Font font = FontFactory.getFont(
                        FontFactory.HELVETICA,
                        fontSize,
                        isHeader ? Font.BOLD : Font.NORMAL,
                        Color.BLACK
                );

                PdfPCell pdfCell = new PdfPCell(new Phrase(textoCelula, font));
                pdfCell.setColspan(colspan);
                pdfCell.setBackgroundColor(bgColor);
                pdfCell.setPadding(1f);
                pdfCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                pdfCell.setBorderColor(new Color(170, 170, 170));
                pdfCell.setBorderWidth(0.5f);

                pdfTable.addCell(pdfCell);
            }
        }

        pdfDoc.add(pdfTable);
    }

    private int calcularMaximoColunas(XWPFTable table) {
        int max = 1;
        for (XWPFTableRow row : table.getRows()) {
            int count = 0;
            for (XWPFTableCell c : row.getTableCells()) {
                int span = 1;
                if (c.getCTTc().isSetTcPr() && c.getCTTc().getTcPr().isSetGridSpan()) {
                    span = c.getCTTc().getTcPr().getGridSpan().getVal().intValue();
                }
                count += span;
            }
            if (count > max) {
                max = count;
            }
        }
        return max;
    }
}
