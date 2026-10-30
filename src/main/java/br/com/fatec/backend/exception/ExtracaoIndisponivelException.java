package br.com.fatec.backend.exception;

/** Falha ao acionar o serviço externo de extração (Gemini). */
public class ExtracaoIndisponivelException extends RuntimeException {
    public ExtracaoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}