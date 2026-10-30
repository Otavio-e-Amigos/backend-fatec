package br.com.fatec.backend.document;

import br.com.fatec.backend.entity.*;
import br.com.fatec.backend.enums.RegimeContrato;
import br.com.fatec.backend.enums.RegimeJuridico;
import br.com.fatec.backend.enums.StatusDisciplinaGrade;
import br.com.fatec.backend.enums.Titulacao;
import br.com.fatec.backend.enums.Turno;
import br.com.fatec.backend.service.CalendarioAcademicoService;
import org.apache.poi.xwpf.usermodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FolhaPontoDocxServiceTest {

    private FolhaPontoDocxService docxService;

    @BeforeEach
    void setUp() {
        // Usa o CalendarioAcademicoService real (em memória, sem banco de dados)
        docxService = new FolhaPontoDocxService(new CalendarioAcademicoService());
    }

    @Test
    @DisplayName("Deve gerar o documento DOCX da Folha de Ponto com dados preenchidos e formatação correta")
    void deveGerarDocumentoComSucesso() throws Exception {
        Professor professor = new Professor(
                "Katia Barbosa Martines",
                "265",
                "12345678901",
                "123456",
                RegimeContrato.INDETERMINADO,
                RegimeJuridico.CLT,
                Titulacao.MESTRE
        );

        PeriodoLetivo periodo = new PeriodoLetivo(2, 2026, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 12, 15));
        Grade grade = new Grade(professor, periodo, new BigDecimal("20.00"), new BigDecimal("80.00"), new BigDecimal("400.00"));

        Curso cursoAds = new Curso("Análise e Desenvolvimento de Sistemas", Turno.MANHA, "ADS");
        Curso cursoDsm = new Curso("Desenvolvimento de Software Multiplataforma", Turno.TARDE, "DSM");

        Disciplina discBd = new Disciplina("BD001", "BD", "Banco de Dados");
        Disciplina discDw = new Disciplina("DW002", "DW", "Desenvolvimento Web");

        DisciplinaDaGrade dg1 = new DisciplinaDaGrade(grade, discBd, cursoAds, (short) 3, "MANHA", (short) 4, 1, StatusDisciplinaGrade.MINISTRANDO);
        DisciplinaDaGrade dg2 = new DisciplinaDaGrade(grade, discDw, cursoDsm, (short) 2, "TARDE", (short) 6, 1, StatusDisciplinaGrade.MINISTRANDO);

        List<AulaDaGrade> aulas = new ArrayList<>();
        // Segunda-feira (1): 08:00 às 08:50 (Slot 2 Manhã)
        aulas.add(new AulaDaGrade(dg1, (short) 1, LocalTime.of(8, 0), LocalTime.of(8, 50)));
        // Terça-feira (2): 13:00 às 18:20 (Bloco contínuo tarde inteira)
        aulas.add(new AulaDaGrade(dg2, (short) 2, LocalTime.of(13, 0), LocalTime.of(18, 20)));

        byte[] bytesDocx = docxService.gerarDocumento(professor, grade, aulas, 9, 2026);

        assertNotNull(bytesDocx);
        assertTrue(bytesDocx.length > 10000, "Arquivo DOCX deve ter tamanho razoável");
        // Assinatura de arquivo ZIP / DOCX: 'P', 'K', 0x03, 0x04
        assertEquals((byte) 'P', bytesDocx[0]);
        assertEquals((byte) 'K', bytesDocx[1]);

        // Validação do conteúdo gerado
        try (XWPFDocument docGerado = new XWPFDocument(new ByteArrayInputStream(bytesDocx))) {
            // Verificar título
            boolean tituloEncontrado = false;
            for (XWPFParagraph p : docGerado.getParagraphs()) {
                if (p.getText().contains("09/2026") || p.getText().contains("09 / 2026")) {
                    tituloEncontrado = true;
                    break;
                }
            }
            assertTrue(tituloEncontrado, "Título com mês/ano 09/2026 deve estar presente");

            // Verificar substituição dos placeholders no cabeçalho
            XWPFTable t0 = docGerado.getTables().get(0);
            String textoT0 = "";
            for (XWPFTableRow r : t0.getRows()) {
                for (XWPFTableCell c : r.getTableCells()) {
                    textoT0 += c.getText() + " ";
                }
            }
            assertFalse(textoT0.contains("{{PROFESSOR}}"), "Placeholder {{PROFESSOR}} deve ser substituído");
            assertFalse(textoT0.contains("{{COD}}"), "Placeholder {{COD}} deve ser substituído");
            assertFalse(textoT0.contains("{{MAT}}"), "Placeholder {{MAT}} deve ser substituído");
            assertFalse(textoT0.contains("{{RJ}}"), "Placeholder {{RJ}} deve ser substituído");
            assertTrue(textoT0.contains("Katia Barbosa Martines"), "Nome do professor deve estar presente no cabeçalho");
            assertTrue(textoT0.contains("265"), "Código do professor deve estar presente");
            assertTrue(textoT0.contains("123456"), "Matrícula deve estar presente");

            // Verificar Grade Horária (Tabela 1)
            XWPFTable t1 = docGerado.getTables().get(1);
            // Linha 3 = Segunda: deve conter sigla pontuada B.D.
            XWPFTableRow rSegunda = t1.getRow(3);
            boolean achouBd = false;
            for (XWPFTableCell c : rSegunda.getTableCells()) {
                if (c.getText().contains("B.D.")) {
                    achouBd = true;
                    break;
                }
            }
            assertTrue(achouBd, "Sigla B.D. deve estar preenchida na segunda-feira com pontos");

            // Linha 4 = Terça: deve conter sigla pontuada D.W.
            XWPFTableRow rTerca = t1.getRow(4);
            boolean achouDw = false;
            for (XWPFTableCell c : rTerca.getTableCells()) {
                if (c.getText().contains("D.W.")) {
                    achouDw = true;
                    break;
                }
            }
            assertTrue(achouDw, "Sigla D.W. deve estar preenchida na terça-feira com pontos");

            // Verificar Folha de Frequência (Tabela 2)
            XWPFTable t2 = docGerado.getTables().get(2);
            // 01/09/2026 foi uma Terça-feira
            XWPFTableRow rDia1 = t2.getRow(2);
            assertEquals("01", rDia1.getCell(0).getText().trim());
            assertEquals("Terça", rDia1.getCell(1).getText().trim());

            // 06/09/2026 foi Domingo (Linha index 7)
            XWPFTableRow rDia6 = t2.getRow(7);
            assertEquals("06", rDia6.getCell(0).getText().trim());
            assertEquals("Domingo", rDia6.getCell(1).getText().trim());
            // Coluna Dia (0) e Semana (1) devem ser BRANCAS no domingo
            assertEquals("FFFFFF", rDia6.getCell(0).getColor(), "Coluna Dia nao pode ser cinza no domingo");
            assertEquals("FFFFFF", rDia6.getCell(1).getColor(), "Coluna Semana nao pode ser cinza no domingo");
            // Apenas os quadradinhos internos (celula 2+) devem ser CINZA no domingo
            assertEquals("808080", rDia6.getCell(2).getColor(), "Quadrado interno deve ser cinza escuro (#808080) no domingo");

            // Setembro tem 30 dias: o dia 31 (Linha index 32) deve estar limpo
            XWPFTableRow rDia31 = t2.getRow(32);
            assertEquals("", rDia31.getCell(0).getText().trim(), "Dia 31 deve estar vazio em setembro");
            assertEquals("FFFFFF", rDia31.getCell(0).getColor());
        }
    }

    @Test
    @DisplayName("Deve limpar dias 29, 30 e 31 em fevereiro de ano não bissexto")
    void deveLimparDiasExcedentesFevereiro() throws Exception {
        Professor professor = new Professor("Prof Teste", "1", "11122233344", "999",
                RegimeContrato.TEMPORARIO, RegimeJuridico.PSS, Titulacao.ESPECIALISTA);
        PeriodoLetivo periodo = new PeriodoLetivo(1, 2027, LocalDate.of(2027, 2, 1), LocalDate.of(2027, 6, 30));
        Grade grade = new Grade(professor, periodo, null, null, null);

        byte[] bytesDocx = docxService.gerarDocumento(professor, grade, List.of(), 2, 2027);

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytesDocx))) {
            XWPFTable t2 = doc.getTables().get(2);

            // Dia 28 (Fevereiro 2027 tem 28 dias) -> Linha 29
            XWPFTableRow rDia28 = t2.getRow(29);
            assertEquals("28", rDia28.getCell(0).getText().trim());
            assertEquals("Domingo", rDia28.getCell(1).getText().trim());

            // Dias 29, 30 e 31 devem estar limpos
            assertEquals("", t2.getRow(30).getCell(0).getText().trim());
            assertEquals("", t2.getRow(31).getCell(0).getText().trim());
            assertEquals("", t2.getRow(32).getCell(0).getText().trim());
        }
    }
}
