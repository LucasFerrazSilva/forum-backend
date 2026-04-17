package com.ferraz.forumbackend.integration.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferraz.forumbackend.infra.exception.ErrorResponse;
import com.ferraz.forumbackend.integration.AbstractIntegrationTest;
import com.ferraz.forumbackend.integration.fixture.UserFixture;
import com.ferraz.forumbackend.session.SessionEntity;
import com.ferraz.forumbackend.session.SessionRepository;
import com.ferraz.forumbackend.session.SessionService;
import com.ferraz.forumbackend.session.dto.LoginDTO;
import com.ferraz.forumbackend.session.dto.SessionDTO;
import com.ferraz.forumbackend.user.UserEntity;
import com.ferraz.forumbackend.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionControllerIntegrationTest extends AbstractIntegrationTest {

    public static final String ENDPOINT = "/api/v1/sessions";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionService sessionService;

    @AfterEach
    void afterEach() {
        sessionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um POST no endpoint '" + ENDPOINT + "' sem enviar email")
    void shouldReturn400WhenSessionsEndpointIsCalledWithPostWithoutEmail() throws Exception {
        badRequestTest(new LoginDTO(null, "SenhaTeste"), "email");
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um POST no endpoint '" + ENDPOINT + "' enviando email invalido")
    void shouldReturn400WhenSessionsEndpointIsCalledWithPostWithInvalidEmail() throws Exception {
        badRequestTest(new LoginDTO("emailinvalido", "SenhaTeste"), "email");
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um POST no endpoint '" + ENDPOINT + "' sem enviar a senha")
    void shouldReturn400WhenSessionsEndpointIsCalledWithPostWithoutPassword() throws Exception {
        badRequestTest(new LoginDTO("email@domain.com", null), "password");
    }

    private void badRequestTest(LoginDTO loginDTO, String invalidField) throws Exception {
        String requestBody = objectMapper.writeValueAsString(loginDTO);
        MockHttpServletResponse response = post(ENDPOINT, requestBody);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isNotBlank();
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getInvalidFields()).hasSize(1);
        assertThat(errorResponse.getInvalidFields().getFirst().field()).isEqualTo(invalidField);
    }

    @Test
    @DisplayName("Deve retornar 401 (Unauthorized) quando fizer um POST no endpoint '" + ENDPOINT + "' enviando senha valida mas email incorreto")
    void shouldReturn400WhenSessionsEndpointIsCalledWithPostWithValidPasswordAndInvalidEmail() throws Exception {
        String senhaValida = "SenhaValida";
        userFixture.user(b -> b.password(senhaValida));
        LoginDTO loginDTO = new LoginDTO("EmailIncorreto@mail.com", senhaValida);
        String requestBody = objectMapper.writeValueAsString(loginDTO);
        MockHttpServletResponse response = post(ENDPOINT, requestBody);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentAsString()).isNotBlank();
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getMessage()).isEqualTo("Erro ao validar as credenciais");
    }

    @Test
    @DisplayName("Deve retornar 401 (Unauthorized) quando fizer um POST no endpoint '" + ENDPOINT + "' enviando email valido mas senha incorreta")
    void shouldReturn400WhenSessionsEndpointIsCalledWithPostWithValidEmailAndInvalidPassword() throws Exception {
        UserEntity user = userFixture.user();
        LoginDTO loginDTO = new LoginDTO(user.getEmail(), "SenhaIncorreta");
        String requestBody = objectMapper.writeValueAsString(loginDTO);
        MockHttpServletResponse response = post(ENDPOINT, requestBody);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentAsString()).isNotBlank();
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getMessage()).isEqualTo("Erro ao validar as credenciais");
    }

    @Test
    @DisplayName("Deve retornar 201 (Created) quando fizer um POST no endpoint '" + ENDPOINT + "' enviando dados validos")
    void shouldReturn201WhenSessionsEndpointIsCalledWithPostWithValidData() throws Exception {
        String senhaValida = "SenhaValida";
        UserEntity user = userFixture.user(b -> b.password(senhaValida));
        LoginDTO loginDTO = new LoginDTO(user.getEmail(), senhaValida);
        String requestBody = objectMapper.writeValueAsString(loginDTO);

        MockHttpServletResponse response = post(ENDPOINT, requestBody);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.getContentAsString()).isNotBlank();
        SessionDTO sessionDTO = objectMapper.readValue(response.getContentAsString(), SessionDTO.class);
        assertThat(sessionDTO).isNotNull();
        assertThat(sessionDTO.sessionId()).isNotBlank();

        List<Cookie> cookies = List.of(response.getCookies());
        assertThat(cookies).hasSize(1);
        Cookie sessionCookie = cookies.getFirst();
        assertThat(sessionCookie.getName()).isEqualTo("session_id");
        assertThat(sessionCookie.getValue()).isNotBlank();
        assertThat(sessionCookie.getPath()).isEqualTo("/");
        assertThat(sessionCookie.isHttpOnly()).isTrue();

        Optional<SessionEntity> sessionOptional =
                sessionRepository.findFirstByTokenAndExpiresAtAfter(sessionDTO.sessionId(), LocalDateTime.now());
        assertThat(sessionOptional).isPresent();
        SessionEntity sessionEntity = sessionOptional.get();
        assertThat(sessionEntity.getUser()).isEqualTo(user);
        assertThat(sessionEntity.getExpiresAt().toLocalDate())
                .isEqualTo(LocalDate.now().plusDays(sessionService.getSessionExpirationDays()));
    }

}
