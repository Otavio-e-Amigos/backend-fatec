package br.com.fatec.backend.document;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Classe utilizada para testar a leitura e alteração
 * de um arquivo DOCX utilizando o Apache POI.
 */
public class TesteDocxService {

    /*
     * Dados fictícios utilizados somente para o teste.
     */
    private final Map<String, String> dados = Map.of(
            "{{SEM}}", "2º/2026",
            "{{INI}}", "01/08/2026",
            "{{FIM}}", "15/12/2026",
            "{{MAT}}", "TESTE001",
            "{{PROFESSOR}}", "TESTE",
            "{{CPF}}", "000.000.000-00",
            "{{CON}}", "Temporário"
    );

    /**
     * Abre o template, substitui os placeholders
     * e gera um novo arquivo DOCX.
     */
    public void gerarDocumentoTeste() throws Exception {

        /*
         * Procura o template dentro da pasta:
         *
         * src/main/resources/templates/
         */
        InputStream entrada = getClass()
                .getResourceAsStream("/templates/grade-modelo.docx");

        /*
         * Verifica se o arquivo foi encontrado.
         */
        if (entrada == null) {
            throw new IllegalStateException(
                    "Template grade-modelo.docx não encontrado."
            );
        }

        /*
         * Abre o arquivo DOCX utilizando o Apache POI.
         */
        XWPFDocument documento = new XWPFDocument(entrada);

        /*
         * Percorre os parágrafos normais do documento.
         */
        for (XWPFParagraph paragrafo : documento.getParagraphs()) {
            substituirTexto(paragrafo);
        }

        /*
         * Percorre todas as tabelas do documento.
         */
        for (XWPFTable tabela : documento.getTables()) {

            /*
             * Percorre as linhas da tabela.
             */
            for (XWPFTableRow linha : tabela.getRows()) {

                /*
                 * Percorre as células da linha.
                 */
                for (XWPFTableCell celula : linha.getTableCells()) {

                    /*
                     * Percorre os parágrafos dentro da célula.
                     */
                    for (XWPFParagraph paragrafo : celula.getParagraphs()) {
                        substituirTexto(paragrafo);
                    }
                }
            }
        }

        /*
         * Cria a pasta para guardar os documentos gerados.
         */
        File pasta = new File("documentos-gerados");

        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        /*
         * Define o nome do novo arquivo.
         *
         * O template original NÃO será alterado.
         */
        File arquivoSaida =
                new File(pasta, "grade-preenchida.docx");

        /*
         * Salva o novo documento.
         */
        try (FileOutputStream saida =
                     new FileOutputStream(arquivoSaida)) {

            documento.write(saida);
        }

        /*
         * Fecha o documento original carregado pelo POI.
         */
        documento.close();

        /*
         * Fecha a entrada do template.
         */
        entrada.close();

        System.out.println(
                "Documento gerado com sucesso!"
        );

        System.out.println(
                "Arquivo: " + arquivoSaida.getAbsolutePath()
        );
    }

    /**
     * Substitui um placeholder mesmo quando o Word
     * dividiu o texto em vários Runs.
     */
    private void substituirPlaceholder(
            XWPFParagraph paragrafo,
            String textoCompleto,
            String placeholder,
            String valor
    ) {

        // Procura o placeholder no texto completo.
        int inicio = textoCompleto.indexOf(placeholder);

        // Se não encontrou, não precisa fazer nada.
        if (inicio == -1) {
            return;
        }

        int fim = inicio + placeholder.length();

        /*
         * Guarda a posição de cada Run dentro do texto completo.
         */
        int posicaoAtual = 0;

        int runInicial = -1;
        int runFinal = -1;

        int inicioDentroRun = 0;
        int fimDentroRun = 0;

        /*
         * Descobre em quais Runs o placeholder está.
         */
        for (int i = 0; i < paragrafo.getRuns().size(); i++) {

            XWPFRun run = paragrafo.getRuns().get(i);

            String texto = run.getText(0);

            if (texto == null) {
                continue;
            }

            int inicioRun = posicaoAtual;
            int fimRun = posicaoAtual + texto.length();

            /*
             * Verifica se o início do placeholder
             * está neste Run.
             */
            if (runInicial == -1
                    && inicio >= inicioRun
                    && inicio < fimRun) {

                runInicial = i;
                inicioDentroRun = inicio - inicioRun;
            }

            /*
             * Verifica se o final do placeholder
             * está neste Run.
             */
            if (fim > inicioRun && fim <= fimRun) {

                runFinal = i;
                fimDentroRun = fim - inicioRun;
                break;
            }

            posicaoAtual = fimRun;
        }

        /*
         * Se não conseguimos encontrar os Runs,
         * não fazemos nenhuma alteração.
         */
        if (runInicial == -1 || runFinal == -1) {
            return;
        }

        /*
         * Caso o placeholder esteja inteiro
         * dentro do mesmo Run.
         */
        if (runInicial == runFinal) {

            XWPFRun run = paragrafo.getRuns().get(runInicial);

            String texto = run.getText(0);

            String novoTexto =
                    texto.substring(0, inicioDentroRun)
                            + valor
                            + texto.substring(fimDentroRun);

            /*
             * Altera somente o texto.
             * A formatação do Run continua.
             */
            run.setText(novoTexto, 0);

            return;
        }

        /*
         * O placeholder está dividido em vários Runs.
         */

        XWPFRun primeiroRun =
                paragrafo.getRuns().get(runInicial);

        XWPFRun ultimoRun =
                paragrafo.getRuns().get(runFinal);

        String textoPrimeiro =
                primeiroRun.getText(0);

        String textoUltimo =
                ultimoRun.getText(0);

        /*
         * Mantém o texto que estava antes do placeholder
         * e coloca o novo valor no primeiro Run.
         */
        String novoTextoPrimeiro =
                textoPrimeiro.substring(0, inicioDentroRun)
                        + valor;

        primeiroRun.setText(novoTextoPrimeiro, 0);

        /*
         * Mantém o texto que estava depois do placeholder.
         */
        String novoTextoUltimo =
                textoUltimo.substring(fimDentroRun);

        ultimoRun.setText(novoTextoUltimo, 0);

        /*
         * Limpa somente os Runs que ficavam no meio
         * do placeholder.
         *
         * A formatação deles continua existindo,
         * mas eles ficam sem texto.
         */
        for (int i = runInicial + 1; i < runFinal; i++) {

            XWPFRun run =
                    paragrafo.getRuns().get(i);

            run.setText("", 0);
        }
    }

    /**
     * Procura os placeholders em um parágrafo
     * e substitui pelos dados de teste,
     * mantendo a formatação original.
     */
    private void substituirTexto(XWPFParagraph paragrafo) {

        // Se o parágrafo não possuir texto, não faz nada.
        if (paragrafo.getRuns().isEmpty()) {
            return;
        }

        /*
         * Junta o texto de todos os Runs para conseguir
         * encontrar placeholders que foram separados pelo Word.
         */
        StringBuilder textoCompleto = new StringBuilder();

        for (XWPFRun run : paragrafo.getRuns()) {

            String texto = run.getText(0);

            if (texto != null) {
                textoCompleto.append(texto);
            }
        }

        /*
         * Faz cada substituição mantendo os Runs originais.
         */
        for (Map.Entry<String, String> item : dados.entrySet()) {

            substituirPlaceholder(
                    paragrafo,
                    textoCompleto.toString(),
                    item.getKey(),
                    item.getValue()
            );

            /*
             * Atualiza o texto completo depois da substituição.
             */
            textoCompleto.setLength(0);

            for (XWPFRun run : paragrafo.getRuns()) {

                String texto = run.getText(0);

                if (texto != null) {
                    textoCompleto.append(texto);
                }
            }
        }
    }
}