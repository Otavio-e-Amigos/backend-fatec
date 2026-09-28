package br.com.fatec.backend.exception;

/**
 * Exceção customizada para violações de regras de negócio do sistema FATEC.
 */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}