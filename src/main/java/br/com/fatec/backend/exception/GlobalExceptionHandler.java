package br.com.fatec.backend.exception;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Trata erros de regras de negócio (ex: usuário não encontrado, login duplicado)
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleRegraNegocioException(RegraNegocioException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RespostaPadraoDTO.erro(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    // Trata erros de validação do @Valid (@NotBlank, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Erro de validação nos dados enviados.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RespostaPadraoDTO.erro(mensagem, HttpStatus.BAD_REQUEST.value()));
    }

    // Trata erros genéricos/inesperados do servidor (Erro 500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RespostaPadraoDTO.erro("Ocorreu um erro interno no servidor: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}