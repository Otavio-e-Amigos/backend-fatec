package br.com.fatec.backend.document.relatorio;

import br.com.fatec.backend.dto.relatorio.PeriodoRelatorioDTO;
import br.com.fatec.backend.dto.relatorio.RelatorioExtracaoGradeDTO;
import br.com.fatec.backend.dto.relatorio.RelatorioExtracaoGradeDTO.*;
import br.com.fatec.backend.enums.StatusExtracaoGrade;
import br.com.fatec.backend.enums.TipoPeriodoRelatorio;
import br.com.fatec.backend.service.relatorio.RelatorioDocumentoMontador;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RelatorioRenderersTest {

    private final RelatorioDocumentoMontador montador = new RelatorioDocumentoMontador();

    private DocumentoRelatorio documentoExemplo(int quantidadeExtracoes) {
        List<ItemExtracao> extracoes = new ArrayList<>();
        for (int i = 1; i <= quantidadeExtracoes; i++) {
            extracoes.add(new ItemExtracao((long) i, LocalDateTime.of(2026, 11, 3, 9, 0).plusHours(i),
                    "Ana & Cia", "grade_" + i + ".pdf", 200L, StatusExtracaoGrade.SUCESSO,
                    "Kátia Gonçalves", "M" + i, i % 2 == 0, "2º/2026", 3, 6, 0, 4200L, null));
        }
        RelatorioExtracaoGradeDTO dto = new RelatorioExtracaoGradeDTO(
                new PeriodoRelatorioDTO(TipoPeriodoRelatorio.MENSAL, LocalDate.of(2026, 11, 3),
                        LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), "Novembro de 2026", true),
                LocalDateTime.of(2026, 11, 18, 10, 30),
                new Resumo(quantidadeExtracoes, quantidadeExtracoes, quantidadeExtracoes, 0, 0,
                        new BigDecimal("100.0"), 3L * quantidadeExtracoes, 6L * quantidadeExtracoes, 0,
                        new BigDecimal("3.00"), 4200L, 4200L, quantidadeExtracoes, 0, quantidadeExtracoes, 200),
                List.of(new SerieDiaria(LocalDate.of(2026, 11, 3), quantidadeExtracoes, quantidadeExtracoes, 0)),
                List.of(new PorUsuario(1L, "Ana & Cia", quantidadeExtracoes, quantidadeExtracoes, 0)),
                List.of(new PorPeriodoLetivo(2026, 2, quantidadeExtracoes)),
                List.of(),
                extracoes);
        return montador.extracaoGrades(dto, "Coordenador <Teste>");
    }

    @Test
    @DisplayName("PDF: gera arquivo válido com várias páginas")
    void gerarPdf() {
        byte[] pdf = new RelatorioPdfRenderer().renderizar(documentoExemplo(120));
        assertTrue(pdf.length > 1000);
        assertEquals("%PDF-", new String(pdf, 0, 5, StandardCharsets.US_ASCII));
    }

    @Test
    @DisplayName("DOCX: contém título, tabelas, rodapé com numeração e página A4")
    void gerarDocx() throws Exception {
        byte[] docx = new RelatorioDocxRenderer().renderizar(documentoExemplo(5));
        assertEquals('P', (char) docx[0]);
        assertEquals('K', (char) docx[1]);

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(docx));
             XWPFWordExtractor extrator = new XWPFWordExtractor(doc)) {
            String texto = extrator.getText();
            assertTrue(texto.contains("Relatório mensal de extração de grades"));
            assertTrue(texto.contains("Grades processadas"));
            assertTrue(texto.contains("Kátia Gonçalves"));
            assertFalse(texto.contains("—"), "o documento não deve usar travessão como valor vazio");
            assertFalse(texto.contains("Evolução diária"));
            assertTrue(doc.getTables().size() >= 4);

            String rodape = doc.getFooterList().get(0).getParagraphs().get(0).getCTP().xmlText();
            assertTrue(rodape.contains("PAGE"));
            assertTrue(rodape.contains("NUMPAGES"));
            assertTrue(rodape.indexOf("Página") < rodape.indexOf("NUMPAGES"));

            String sectPr = doc.getDocument().getBody().getSectPr().xmlText();
            assertTrue(sectPr.contains("11906"));
        }
    }

    @Test
    @DisplayName("Tabelas vazias exibem mensagem em vez de quebrar a geração")
    void tabelaVazia() {
        DocumentoRelatorio doc = documentoExemplo(0);
        assertDoesNotThrow(() -> new RelatorioPdfRenderer().renderizar(doc));
        assertDoesNotThrow(() -> new RelatorioDocxRenderer().renderizar(doc));
    }
}
