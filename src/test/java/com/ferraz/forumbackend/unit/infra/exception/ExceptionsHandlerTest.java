package com.ferraz.forumbackend.unit.infra.exception;

import com.ferraz.forumbackend.infra.service.CookieService;
import com.ferraz.forumbackend.infra.exception.DatabaseException;
import com.ferraz.forumbackend.infra.exception.ErrorResponse;
import com.ferraz.forumbackend.infra.exception.ExceptionsHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionsHandlerTest {

    final ExceptionsHandler exceptionsHandler = new ExceptionsHandler(new CookieService());

    @Test
    void shouldBuildErrorResponseCorrectlyWhenHandlingDatabaseException() {
        DatabaseException databaseException = new DatabaseException(new RuntimeException("Teste"));

        ResponseEntity<ErrorResponse> response = exceptionsHandler.handleBaseException(databaseException);
        ErrorResponse errorResponse = response.getBody();

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatusCode()).isEqualTo(databaseException.getStatusCode());
        assertThat(errorResponse.getName()).isEqualTo(databaseException.getName());
        assertThat(errorResponse.getMessage()).isEqualTo(databaseException.getMessage());
        assertThat(errorResponse.getAction()).isEqualTo(databaseException.getAction());
    }

    @Test
    void shouldBuildErrorResponseCorrectlyWhenHandlingHttpRequestMethodNotSupportedException() {
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("Teste");
        ResponseEntity<ErrorResponse> response = exceptionsHandler.handleHttpRequestMethodNotSupportedException(exception);
        ErrorResponse errorResponse = response.getBody();

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
        assertThat(errorResponse.getName()).isEqualTo("HttpRequestMethodNotSupportedException");
        assertThat(errorResponse.getMessage()).isEqualTo("Método não permitido");
        assertThat(errorResponse.getAction()).isEqualTo("Utilize um método permitido");
    }

    @Test
    void shouldBuildErrorResponseCorrectlyWhenHandlingNoResourceFoundException() {
        NoResourceFoundException exception = new NoResourceFoundException(HttpMethod.GET, "Teste", "Teste");
        ResponseEntity<ErrorResponse> response = exceptionsHandler.handleNoResourceFoundException(exception);
        ErrorResponse errorResponse = response.getBody();

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(errorResponse.getName()).isEqualTo("NoResourceFoundException");
        assertThat(errorResponse.getMessage()).isEqualTo("Endpoint não encontrado");
        assertThat(errorResponse.getAction()).isEqualTo("Verifique se o path está correto");
    }

    @Test
    void shouldBuildErrorResponseCorrectlyWhenHandlingGenericException() {
        RuntimeException exception = new RuntimeException("Teste");
        ResponseEntity<ErrorResponse> response = exceptionsHandler.handleException(exception);
        ErrorResponse errorResponse = response.getBody();

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(errorResponse.getName()).isEqualTo(exception.getClass().getSimpleName());
        assertThat(errorResponse.getMessage()).isEqualTo("Erro interno");
        assertThat(errorResponse.getAction()).isEqualTo("Verifique os logs do sistema para obter mais informações");
    }
}