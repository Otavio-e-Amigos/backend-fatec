package br.com.fatec.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

/**
 * Intercepta as exceções e padroniza a resposta em formato JSON para o Frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RegraNegocioException.class)
    // Map<String, String> = chave e valor em texto.
    // Exemplo: {"erro": "O login já está em uso."}
    public ResponseEntity<Map<String, String>> tratarRegraNegocio(RegraNegocioException ex) {
        // Retorna status 422 (Unprocessable Entity) com a mensagem de erro da regra de negócio
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("erro", ex.getMessage()));
    }
}