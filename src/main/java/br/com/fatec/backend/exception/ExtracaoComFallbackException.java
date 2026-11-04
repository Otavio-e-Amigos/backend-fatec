package br.com.fatec.backend.exception;

/** Usado internamente: a extração com a chave do usuário falhou por cota, e será tentada com a chave padrão. */
public class ExtracaoComFallbackException extends RuntimeException {
    public ExtracaoComFallbackException(String mensagem) {
        super(mensagem);
    }
}
