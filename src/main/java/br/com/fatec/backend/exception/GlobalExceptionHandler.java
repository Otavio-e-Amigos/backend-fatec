package br.com.fatec.backend.exception;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Trata especificamente o login de Usuário Desativado (Code 403)
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleDisabledException(DisabledException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(RespostaPadraoDTO.erro(ex.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    // Trata erro de Login Inválido (Usuário inexistente ou Senha Incorreta)
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class, InternalAuthenticationServiceException.class})
    public ResponseEntity<RespostaPadraoDTO<Void>> handleAuthenticationException() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(RespostaPadraoDTO.erro("Usuário inexistente ou senha inválida.", HttpStatus.UNAUTHORIZED.value()));
    }

    // Trata erros de regras de negócio da aplicação
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleRegraNegocioException(RegraNegocioException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RespostaPadraoDTO.erro(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    // Trata erros de validação do @Valid
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

    // Trata erros genéricos / inesperados do servidor (Erro 500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RespostaPadraoDTO.erro("Ocorreu um erro interno no servidor: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}