package com.ferraz.forumbackend.integration.session;

import com.ferraz.forumbackend.infra.exception.ErrorResponse;
import com.ferraz.forumbackend.integration.AbstractIntegrationTest;
import com.ferraz.forumbackend.session.SessionEntity;
import com.ferraz.forumbackend.session.SessionRepository;
import com.ferraz.forumbackend.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DeleteSessionIntegrationTest extends AbstractIntegrationTest {

    @Override
    public String getEndpoint() {
        return "/api/v1/sessions";
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @AfterEach
    void afterEach() {
        sessionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve retornar 401 (Unauthorized) quando fizer um DELETE no endpoint '/api/v1/sessions' sem enviar o cookie de sessao")
    void shouldReturn401WhenSessionsEndpointIsCalledWithDeleteWithoutSessionCookie() throws Exception {
        MockHttpServletResponse response = DELETE().send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = extractObject(response, ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(errorResponse.getName()).isEqualTo("UnauthorizedException");
        assertThat(errorResponse.getMessage()).isEqualTo("Cookie de sessão não enviado ou inválido");
        assertThat(errorResponse.getAction()).isEqualTo("Verifique se um cookie válido de sessão está sendo enviado no cabeçalho da requisição");
        assertThat(errorResponse.getInvalidFields()).isNull();
    }

    @Test
    @DisplayName("Deve retornar 204 (No Content) quando fizer um DELETE no endpoint '/api/v1/sessions' enviando um cookie de sessao valido")
    void shouldReturn204WhenSessionsEndpointIsCalledWithDeleteWithValidSessionCookie() throws Exception {
        Cookie sessionCookie = sessionFixture.cookie();

        MockHttpServletResponse response = DELETE().withSessionCookie(sessionCookie).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(response.getContentAsString()).isBlank();

        List<Cookie> cookies = List.of(response.getCookies());
        assertThat(cookies).hasSize(1);
        Cookie newSessionCookie = cookies.getFirst();
        assertThat(newSessionCookie.getName()).isEqualTo(cookieName);
        assertThat(newSessionCookie.getMaxAge()).isZero();

        Optional<SessionEntity> sessionOptional =
                sessionRepository.findFirstByTokenAndExpiresAtAfter(sessionCookie.getValue(), LocalDateTime.now());
        assertThat(sessionOptional).isEmpty();
    }

}

