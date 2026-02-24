package com.ferraz.forumbackend.infra.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class ExceptionsHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionsHandler.class);

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        return handleException(ex);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        ErrorResponse errorResponse =
                new ErrorResponse(
                        HttpStatus.METHOD_NOT_ALLOWED.value(),
                        "HttpRequestMethodNotSupportedException",
                        "Método não permitido",
                        "Utilize um método permitido");

        return handleException(ex, errorResponse);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        ErrorResponse errorResponse =
                new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "NoResourceFoundException",
                        "Endpoint não encontrado",
                        "Verifique se o path está correto");

        return handleException(ex, errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleError400(MethodArgumentNotValidException exception) {
        List<FieldError> errors = exception.getFieldErrors();
        List<InvalidField> invalidFields = errors.stream().map(InvalidField::new).toList();
        ValidationException validationException = new ValidationException(invalidFields);
        return handleException(validationException);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBodyExpected(HttpMessageNotReadableException exception) {
        ErrorResponse errorResponse =
                new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "HttpMessageNotReadableException",
                        "Corpo da requisição não encontrado",
                        "Envie o corpo da requisição com os campos necessários");
        return handleException(exception, errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ErrorResponse errorResponse =
                new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex.getClass().getSimpleName(),
                        "Erro interno",
                        "Verifique os logs do sistema para obter mais informações");

        return handleException(ex, errorResponse);
    }

    private ResponseEntity<ErrorResponse> handleException(BaseException ex) {
        return handleException(ex, new ErrorResponse(ex));
    }

    private ResponseEntity<ErrorResponse> handleException(Exception ex, ErrorResponse errorResponse) {
        logger.error(ex.getMessage(), ex);
        return ResponseEntity.status(errorResponse.getStatusCode()).body(errorResponse);
    }

}
