package com.ferraz.forumbackend.integration.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferraz.forumbackend.infra.exception.ErrorResponse;
import com.ferraz.forumbackend.integration.AbstractIntegrationTest;
import com.ferraz.forumbackend.integration.status.entity.StatusDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class StatusControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar 200 quando fizer um GET no endpoint '/api/v1/status'")
    void shouldReturn200WhenStatusEndpointIsCalled() throws Exception {
        MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.get("/api/v1/status")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        StatusDTO statusDTO = objectMapper.readValue(response.getContentAsString(), StatusDTO.class);

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
        MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.post("/api/v1/status")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
        assertThat(errorResponse.getName()).isNotBlank();
        assertThat(errorResponse.getMessage()).isNotBlank();
        assertThat(errorResponse.getAction()).isNotBlank();
    }

}