package com.ferraz.forumbackend.infra.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
                        405,
                        "HttpRequestMethodNotSupportedException",
                        "Método não permitido",
                        "Utilize um método permitido");

        return handleException(ex, errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ErrorResponse errorResponse =
                new ErrorResponse(
                        500,
                        "Exception",
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
