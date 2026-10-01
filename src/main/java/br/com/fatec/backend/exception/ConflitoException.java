package br.com.fatec.backend.exception;

public class ConflitoException extends RuntimeException {

    public ConflitoException(String message) {
        super(message);
    }
}