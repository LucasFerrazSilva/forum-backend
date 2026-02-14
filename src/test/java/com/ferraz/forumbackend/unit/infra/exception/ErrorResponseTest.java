package com.ferraz.forumbackend.unit.infra.exception;

import com.ferraz.forumbackend.infra.exception.DatabaseException;
import com.ferraz.forumbackend.infra.exception.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ErrorResponseTest {

    @Test
    @DisplayName("Deve definir as variáveis internas corretamente com AllArgsConstructor")
    void testAllArgsConstructor() {
        ErrorResponse errorResponse =
                new ErrorResponse(
                        405,
                        "HttpRequestMethodNotSupportedException",
                        "Método não permitido",
                        "Utilize um método permitido");

        assertThat(errorResponse.getStatusCode()).isEqualTo(405);
        assertThat(errorResponse.getName()).isEqualTo("HttpRequestMethodNotSupportedException");
        assertThat(errorResponse.getMessage()).isEqualTo("Método não permitido");
        assertThat(errorResponse.getAction()).isEqualTo("Utilize um método permitido");
    }

    @Test
    @DisplayName("Deve definir as variáveis internas corretamente com construtor de exception")
    void testExceptionConstructor() {
        DatabaseException databaseException = new DatabaseException(new RuntimeException("Teste"));

        ErrorResponse errorResponse = new ErrorResponse(databaseException);

        assertThat(errorResponse.getStatusCode()).isEqualTo(databaseException.getStatusCode());
        assertThat(errorResponse.getName()).isEqualTo(databaseException.getName());
        assertThat(errorResponse.getMessage()).isEqualTo(databaseException.getMessage());
        assertThat(errorResponse.getAction()).isEqualTo(databaseException.getAction());
    }

}