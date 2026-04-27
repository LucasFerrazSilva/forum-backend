package com.ferraz.forumbackend.integration.status;

import com.ferraz.forumbackend.infra.exception.ErrorResponse;
import com.ferraz.forumbackend.integration.AbstractIntegrationTest;
import com.ferraz.forumbackend.session.dto.LoginDTO;
import com.ferraz.forumbackend.status.entity.DatabaseInfo;
import com.ferraz.forumbackend.status.entity.StatusDTO;
import com.ferraz.forumbackend.user.UserEntity;
import jakarta.servlet.http.Cookie;
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
    @DisplayName("Deve retornar 200 com versão do banco nula quando anônimo acessar o endpoint")
    void shouldReturn200WithNullDbVersionWhenAnonymous() throws Exception {
        MockHttpServletResponse response = GET().send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        StatusDTO statusDTO = extractObject(response, StatusDTO.class);

        assertThat(statusDTO).isNotNull();
        assertThat(statusDTO.updatedAt()).isBefore(LocalDateTime.now());
        assertThat(statusDTO.database()).isNotNull();
        assertThat(statusDTO.database().version()).isNull();
        assertThat(statusDTO.database().maxConnections()).isNotNull();
        assertThat(statusDTO.database().openedConnections()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar 200 com versão do banco nula quando usuário autenticado sem feature acessar o endpoint")
    void shouldReturn200WithNullDbVersionWhenAuthenticatedWithoutFeature() throws Exception {
        MockHttpServletResponse response = GET().withDefaultUser().send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        StatusDTO statusDTO = extractObject(response, StatusDTO.class);

        assertThat(statusDTO).isNotNull();
        assertThat(statusDTO.database()).isNotNull();
        assertThat(statusDTO.database().version()).isNull();
        assertThat(statusDTO.database().maxConnections()).isNotNull();
        assertThat(statusDTO.database().openedConnections()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar 200 com versão do banco preenchida quando usuário com feature 'read:status:all' acessar o endpoint")
    void shouldReturn200WithDbVersionWhenAuthenticatedWithFeature() throws Exception {
        String password = "SenhaValida";
        UserEntity user = userFixture.user(b ->
                b.withPassword(password).withFeatures(new String[]{"read:status:all"})
        );
        LoginDTO loginDTO = new LoginDTO(user.getEmail(), password);
        Cookie sessionCookie = sessionFixture.cookie(loginDTO);

        MockHttpServletResponse response = GET().withSessionCookie(sessionCookie).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        StatusDTO statusDTO = extractObject(response, StatusDTO.class);

        assertThat(statusDTO).isNotNull();
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