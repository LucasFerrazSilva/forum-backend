package com.ferraz.forumbackend.integration.status;

import com.ferraz.forumbackend.infra.exception.ErrorResponse;
import com.ferraz.forumbackend.integration.AbstractIntegrationTest;
import com.ferraz.forumbackend.status.entity.StatusDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class StatusControllerIntegrationTest extends AbstractIntegrationTest {

    public String getEndpoint() {
        return "/api/v1/status";
    }

    @Test
    @DisplayName("Deve retornar 200 quando fizer um GET no endpoint '/api/v1/status'")
    void shouldReturn200WhenStatusEndpointIsCalled() throws Exception {
        MockHttpServletResponse response = GET().send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        StatusDTO statusDTO = extractObject(response, StatusDTO.class);

        assertThat(statusDTO).isNotNull();
        assertThat(statusDTO.updatedAt()).isBefore(LocalDateTime.now());
        assertThat(statusDTO.database()).isNotNull();
        assertThat(statusDTO.database().version()).isNotBlank();
        assertThat(statusDTO.database().maxConnections()).isNotNull();
        assertThat(statusDTO.database().openedConnections()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar 405 quando fizer um POST no endpoint '/api/v1/status'")
    void shouldReturn405WhenPostStatusEndpointIsCalled() throws Exception {
        MockHttpServletResponse response = POST().send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());

        ErrorResponse errorResponse = extractObject(response, ErrorResponse.class);

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
        assertThat(errorResponse.getName()).isNotBlank();
        assertThat(errorResponse.getMessage()).isNotBlank();
        assertThat(errorResponse.getAction()).isNotBlank();
    }

}