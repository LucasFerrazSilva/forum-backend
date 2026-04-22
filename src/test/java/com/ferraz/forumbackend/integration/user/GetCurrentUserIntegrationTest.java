package com.ferraz.forumbackend.integration.user;

import com.ferraz.forumbackend.infra.exception.ErrorResponse;
import com.ferraz.forumbackend.integration.AbstractIntegrationTest;
import com.ferraz.forumbackend.session.SessionEntity;
import com.ferraz.forumbackend.session.SessionRepository;
import com.ferraz.forumbackend.session.dto.LoginDTO;
import com.ferraz.forumbackend.user.UserEntity;
import com.ferraz.forumbackend.user.UserRepository;
import com.ferraz.forumbackend.user.dto.UserDTO;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetCurrentUserIntegrationTest extends AbstractIntegrationTest {

    @Override
    public String getEndpoint() {
        return "/api/v1/users";
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
    @DisplayName("Deve retornar 401 (Unauthorized) quando fizer um GET no endpoint '/api/v1/users/' sem enviar o cookie de sessão")
    void shouldReturn401WhenUsersEndpointIsCalledWithGetAndNoSessionCookie() throws Exception {
        MockHttpServletResponse response = GET().send();

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
    @DisplayName("Deve retornar 401 (Unauthorized) quando fizer um GET no endpoint '/api/v1/users/' enviando cookie de sessão expirado")
    void shouldReturn401WhenUsersEndpointIsCalledWithGetAndExpiredSessionCookie() throws Exception {
        String senhaValida = "SenhaValida";
        UserEntity user = userFixture.user(b -> b.password(senhaValida));
        LoginDTO loginDTO = new LoginDTO(user.getEmail(), senhaValida);

        Cookie sessionCookie = sessionFixture.cookie(loginDTO);

        SessionEntity sessionEntity = sessionRepository.findFirstByUser(user).get();
        sessionEntity.setExpiresAt(LocalDateTime.now().minusDays(1));
        sessionRepository.save(sessionEntity);

        MockHttpServletResponse response = GET().withSessionCookie(sessionCookie).send();

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
    @DisplayName("Deve retornar 401 (Unauthorized) quando fizer um GET no endpoint '/api/v1/users/' enviando cookie de sessão invalido")
    void shouldReturn401WhenUsersEndpointIsCalledWithGetAndInvalidSessionCookie() throws Exception {
        Cookie sessionCookie = sessionFixture.cookie();
        sessionCookie.setValue("Invalid value");

        MockHttpServletResponse response = GET().withSessionCookie(sessionCookie).send();

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
    @DisplayName("Deve retornar 401 (Unauthorized) quando fizer um GET no endpoint '/api/v1/users/' enviando cookie inativado")
    void shouldReturn401WhenUsersEndpointIsCalledWithGetAndInvalidatedSessionCookie() throws Exception {
        String senhaValida = "SenhaValida";
        UserEntity user = userFixture.user(b -> b.password(senhaValida));
        LoginDTO loginDTO = new LoginDTO(user.getEmail(), senhaValida);

        Cookie sessionCookie = sessionFixture.cookie(loginDTO);

        DELETE().withEndpoint("/api/v1/sessions").withSessionCookie(sessionCookie).send();
        MockHttpServletResponse response = GET().withSessionCookie(sessionCookie).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = extractObject(response, ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(errorResponse.getName()).isEqualTo("UnauthorizedException");
        assertThat(errorResponse.getMessage()).isEqualTo("Cookie de sessão não enviado ou inválido");
        assertThat(errorResponse.getAction()).isEqualTo("Verifique se um cookie válido de sessão está sendo enviado no cabeçalho da requisição");
        assertThat(errorResponse.getInvalidFields()).isNull();

        List<Cookie> cookies = List.of(response.getCookies());
        assertThat(cookies).hasSize(1);
        Cookie newSessionCookie = cookies.getFirst();
        assertThat(newSessionCookie.getName()).isEqualTo(cookieName);
        assertThat(newSessionCookie.getMaxAge()).isZero();
    }

    @Test
    @DisplayName("Deve retornar 200 (Ok) quando fizer um GET no endpoint '/api/v1/users' com um cookie de sessão válido")
    void shouldReturn200WhenUsersEndpointIsCalledWithGetAndValidSessionCookie() throws Exception {
        String senhaValida = "SenhaValida";
        UserEntity user = userFixture.user(b -> b.password(senhaValida));
        LoginDTO loginDTO = new LoginDTO(user.getEmail(), senhaValida);

        Cookie sessionCookie = sessionFixture.cookie(loginDTO);
        sessionCookie.setMaxAge(sessionCookie.getMaxAge() - 1);

        SessionEntity previousSessionEntity = sessionRepository.findFirstByUser(user).get();

        MockHttpServletResponse response = GET().withSessionCookie(sessionCookie).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isNotBlank();

        UserDTO userDTO = extractObject(response, UserDTO.class);
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.id()).isEqualTo(user.getId());
        assertThat(userDTO.username()).isEqualTo(user.getUsername());
        assertThat(userDTO.email()).isEqualTo(user.getEmail());
        assertThat(userDTO.createdAt()).isNotNull();
        assertThat(userDTO.updatedAt()).isNotNull();

        SessionEntity sessionEntity = sessionRepository.findFirstByUser(user).get();
        assertThat(sessionEntity.getToken()).isEqualTo(previousSessionEntity.getToken());
        assertThat(sessionEntity.getExpiresAt()).isAfter(previousSessionEntity.getExpiresAt());
        assertThat(sessionEntity.getUpdatedAt()).isAfter(previousSessionEntity.getUpdatedAt());

        List<Cookie> cookies = List.of(response.getCookies());
        assertThat(cookies).hasSize(1);
        Cookie newSessionCookie = cookies.getFirst();
        assertThat(newSessionCookie.getValue()).isEqualTo(sessionCookie.getValue());
        assertThat(newSessionCookie.getName()).isEqualTo(cookieName);
        assertThat(newSessionCookie.getMaxAge()).isGreaterThan(sessionCookie.getMaxAge());
    }

}

