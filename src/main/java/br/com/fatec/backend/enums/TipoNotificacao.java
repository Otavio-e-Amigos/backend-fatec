package br.com.fatec.backend.enums;

/** Evento específico que gerou a notificação. Cada tipo pertence a uma categoria. */
public enum TipoNotificacao {
    EXTRACAO_GRADE_CONCLUIDA(CategoriaNotificacao.EXTRACAO_GRADE),
    EXTRACAO_GRADE_COM_AVISOS(CategoriaNotificacao.EXTRACAO_GRADE),
    EXTRACAO_GRADE_FALHA(CategoriaNotificacao.EXTRACAO_GRADE),
    FOLHA_PONTO_GERADA(CategoriaNotificacao.FOLHA_PONTO),
    FOLHA_PONTO_GERADA_COM_AVISOS(CategoriaNotificacao.FOLHA_PONTO),
    RELATORIO_GERADO(CategoriaNotificacao.RELATORIO),
    CALENDARIO_ATUALIZACAO_PENDENTE(CategoriaNotificacao.CALENDARIO);

    private final CategoriaNotificacao categoria;

    TipoNotificacao(CategoriaNotificacao categoria) {
        this.categoria = categoria;
    }

    public CategoriaNotificacao getCategoria() {
        return categoria;
    }
}
