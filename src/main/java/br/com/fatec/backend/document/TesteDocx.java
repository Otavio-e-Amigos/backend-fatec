package br.com.fatec.backend.document;

/**
 * Classe utilizada somente para executar
 * o teste do Apache POI.
 */
public class TesteDocx {

    public static void main(String[] args) {

        try {

            /*
             * Cria o serviço responsável pelo teste.
             */
            TesteDocxService service = new TesteDocxService();

            /*
             * Executa a geração do documento.
             */
            service.gerarDocumentoTeste();

        } catch (Exception erro) {

            /*
             * Mostra o erro caso alguma coisa dê errado.
             */
            System.out.println(
                    "Erro ao gerar documento:"
            );

            erro.printStackTrace();
        }
    }
}