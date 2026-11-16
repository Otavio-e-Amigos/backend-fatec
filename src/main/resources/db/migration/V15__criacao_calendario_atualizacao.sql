-- Confirmação anual de que o calendário acadêmico do ano foi revisado/atualizado.
-- Os eventos do calendário continuam em memória (decisão D-03 ainda pendente); esta tabela guarda
-- apenas o marco persistente usado pelo lembrete anual de fevereiro.
CREATE TABLE calendario_atualizacao (
    ano             INTEGER      PRIMARY KEY,
    usuario_id      BIGINT,
    observacao      VARCHAR(500),
    atualizado_em   TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT fk_calendario_atualizacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE SET NULL,
    CONSTRAINT ck_calendario_atualizacao_ano CHECK (ano BETWEEN 2000 AND 2050)
);
