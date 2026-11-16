package br.com.fatec.backend.document.relatorio;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo neutro de um relatório do Appointer. É montado uma única vez a partir dos dados do relatório
 * e renderizado em PDF ou DOCX pelos renderizadores, garantindo o mesmo conteúdo nos dois formatos.
 */
public record DocumentoRelatorio(
        String tipoRelatorio,
        String titulo,
        String subtitulo,
        String periodoDescricao,
        LocalDateTime geradoEm,
        String geradoPor,
        List<Indicador> indicadores,
        List<Secao> secoes,
        String observacaoFinal
) {

    public DocumentoRelatorio {
        indicadores = indicadores == null ? List.of() : List.copyOf(indicadores);
        secoes = secoes == null ? List.of() : List.copyOf(secoes);
    }

    /** Indicador numérico de destaque (cartão no topo do relatório). */
    public record Indicador(String rotulo, String valor) {
    }

    public record Secao(String titulo, String descricao, List<Bloco> blocos) {
        public Secao {
            blocos = blocos == null ? List.of() : List.copyOf(blocos);
        }
    }

    /** Conteúdo de uma seção. */
    public sealed interface Bloco permits Paragrafo, Subtitulo, Tabela, FichaChaveValor {
    }

    public record Paragrafo(String texto) implements Bloco {
    }

    public record Subtitulo(String texto) implements Bloco {
    }

    /**
     * Tabela de dados. {@code largurasRelativas} define a proporção das colunas (mesmo tamanho dos cabeçalhos);
     * {@code alinharDireita} marca colunas numéricas.
     */
    public record Tabela(List<String> cabecalhos, List<List<String>> linhas, float[] largurasRelativas,
                         boolean[] alinharDireita, String mensagemVazia) implements Bloco {
        public Tabela {
            cabecalhos = List.copyOf(cabecalhos);
            linhas = linhas == null ? List.of() : linhas.stream().map(l -> l.stream().map(v -> v == null ? "" : v).toList()).toList();
        }
    }

    /** Ficha de identificação em duas colunas (rótulo: valor). */
    public record FichaChaveValor(List<Indicador> itens) implements Bloco {
        public FichaChaveValor {
            itens = List.copyOf(itens);
        }
    }
}
