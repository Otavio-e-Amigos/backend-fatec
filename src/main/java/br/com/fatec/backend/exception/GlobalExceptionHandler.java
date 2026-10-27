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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {


    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleDisabledException(DisabledException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(RespostaPadraoDTO.erro(
                        ex.getMessage(),
                        HttpStatus.FORBIDDEN.value()
                ));
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class,
            InternalAuthenticationServiceException.class
    })


    public ResponseEntity<RespostaPadraoDTO<Void>> handleAuthenticationException() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(RespostaPadraoDTO.erro(
                        "Usuário inexistente ou senha inválida.",
                        HttpStatus.UNAUTHORIZED.value()
                ));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleRegraNegocioException(
            RegraNegocioException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RespostaPadraoDTO.erro(
                        ex.getMessage(),
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleConflitoException(
            ConflitoException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(RespostaPadraoDTO.erro(
                        ex.getMessage(),
                        HttpStatus.CONFLICT.value()
                ));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleRecursoNaoEncontradoException(
            RecursoNaoEncontradoException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(RespostaPadraoDTO.erro(
                        ex.getMessage(),
                        HttpStatus.NOT_FOUND.value()
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleAcessoNegado() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(RespostaPadraoDTO.erro(
                        "Acesso negado: você não tem permissão para esta operação.",
                        HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleIntegridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade no banco: {}", ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(RespostaPadraoDTO.erro(
                        "Os dados enviados conflitam com um registro existente.",
                        HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleValidationException(
            MethodArgumentNotValidException ex) {

        String mensagem = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Erro de validação nos dados enviados.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RespostaPadraoDTO.erro(
                        mensagem,
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleCorpoInvalido() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(RespostaPadraoDTO.erro(
                        "Corpo da requisição inválido ou com valor não reconhecido.",
                        HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleGenericException(Exception ex) {
        log.error("Erro inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RespostaPadraoDTO.erro(
                        "Ocorreu um erro interno no servidor.",
                        HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(ExtracaoIndisponivelException.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleExtracaoIndisponivel(ExtracaoIndisponivelException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(RespostaPadraoDTO.erro(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE.value()));
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleArquivoGrande() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(RespostaPadraoDTO.erro("O arquivo excede o tamanho máximo de 10 MB.",
                        HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(org.springframework.web.multipart.support.MissingServletRequestPartException.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleArquivoAusente() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(RespostaPadraoDTO.erro("Envie o arquivo PDF no campo 'arquivo'.",
                        HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<RespostaPadraoDTO<Void>> handleRecursoEstaticoAusente() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(RespostaPadraoDTO.erro("Recurso não encontrado.", HttpStatus.NOT_FOUND.value()));
    }
}