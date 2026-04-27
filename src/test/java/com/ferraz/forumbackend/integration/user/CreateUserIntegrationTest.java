package com.ferraz.forumbackend.integration.user;

import com.ferraz.forumbackend.infra.exception.ErrorResponse;
import com.ferraz.forumbackend.infra.exception.InvalidField;
import com.ferraz.forumbackend.integration.AbstractIntegrationTest;
import com.ferraz.forumbackend.session.SessionRepository;
import com.ferraz.forumbackend.user.UserEntity;
import com.ferraz.forumbackend.user.UserRepository;
import com.ferraz.forumbackend.user.dto.NewUserDTO;
import com.ferraz.forumbackend.user.dto.UserDTO;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CreateUserIntegrationTest extends AbstractIntegrationTest {

    @Override
    public String getEndpoint() {
        return "/api/v1/users";
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void afterEach() {
        sessionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve retornar 201 (Created) quando fizer um POST no endpoint '/api/v1/users'")
    void shouldReturn201WhenUsersEndpointIsCalledWithPost() throws Exception {
        NewUserDTO newUserDTO = userFixture.newUserDTO();

        MockHttpServletResponse response = POST().withRequestBody(newUserDTO).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        assertThat(response.getContentAsString()).isNotBlank();

        UserDTO userDTO = extractObject(response, UserDTO.class);
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.id()).isNotNull();
        assertThat(userDTO.username()).isEqualTo(newUserDTO.username());
        assertThat(userDTO.email()).isEqualTo(newUserDTO.email());
        assertThat(userDTO.createdAt()).isNotNull();
        assertThat(userDTO.updatedAt()).isNotNull();
        assertThat(userDTO.features()).contains("read:activation_token");

        Optional<UserEntity> userEntityOptional = userRepository.findById(userDTO.id());
        assertThat(userEntityOptional).isPresent();
        UserEntity userEntity = userEntityOptional.get();
        assertThat(passwordEncoder.matches(newUserDTO.password(), userEntity.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("SenhaInvalida", userEntity.getPassword())).isFalse();

        boolean emailReceived = greenMail.waitForIncomingEmail(5000, 1);
        assertThat(emailReceived).isTrue();
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages[0].getAllRecipients()[0]).hasToString(newUserDTO.email());
        assertThat(messages[0].getSubject()).isEqualTo("Ative seu cadastro");
        assertThat(messages[0].getContent()).asString().contains(newUserDTO.username());
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um POST no endpoint '/api/v1/users' sem enviar o body")
    void shouldReturn400WhenUsersEndpointIsCalledWithPostWithoutBody() throws Exception {
        MockHttpServletResponse response = POST().send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isNotBlank();
        ErrorResponse errorResponse = extractObject(response, ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um POST no endpoint '/api/v1/users' sem enviar os campos necessários")
    void shouldReturn400WhenUsersEndpointIsCalledWithPostWithoutRequiredFields() throws Exception {
        NewUserDTO newUserDTO =
                userFixture.newUserDTO(u -> u.withUsername("").withEmail(" ").withPassword(null));

        MockHttpServletResponse response = POST().withRequestBody(newUserDTO).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = extractObject(response, ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        List<String> invalidFieldNames =
                errorResponse.getInvalidFields().stream().map(InvalidField::field).toList();
        assertThat(invalidFieldNames).containsAll(List.of("username", "email", "password"));
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um POST no endpoint '/api/v1/users' usando um email ja cadastrado")
    void shouldReturn400WhenUsersEndpointIsCalledWithPostWithNonUniqueEmail() throws Exception {
        UserEntity existingUser =
                userFixture.user(u -> u.withEmail("Email@domain.com"));

        NewUserDTO newUserDTO =
                userFixture.newUserDTO(u -> u.withEmail(existingUser.getEmail().toLowerCase()));

        MockHttpServletResponse response = POST().withRequestBody(newUserDTO).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = extractObject(response, ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getInvalidFields()).isNotEmpty();
        assertThat(errorResponse.getInvalidFields().getFirst().field()).isEqualTo("email");
        assertThat(errorResponse.getInvalidFields().getFirst().message())
                .isEqualTo("O email %s já está cadastrado".formatted(existingUser.getEmail()));
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um POST no endpoint '/api/v1/users' usando um username ja cadastrado")
    void shouldReturn400WhenUsersEndpointIsCalledWithPostWithNonUniqueUsername() throws Exception {
        UserEntity existingUser =
                userFixture.user(u -> u.withUsername("NonUniqueUsername"));

        NewUserDTO newUserDTO =
                userFixture.newUserDTO(u -> u.withUsername(existingUser.getUsername().toLowerCase()));

        MockHttpServletResponse response = POST().withRequestBody(newUserDTO).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = extractObject(response, ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getInvalidFields()).isNotEmpty();
        assertThat(errorResponse.getInvalidFields().getFirst().field()).isEqualTo("username");
        assertThat(errorResponse.getInvalidFields().getFirst().message())
                .isEqualTo("O username %s já está cadastrado".formatted(existingUser.getUsername()));
    }

    @Test
    @DisplayName("Deve retornar 403 (Forbidden) quando fizer um POST no endpoint '/api/v1/users' enviando um cookie de sessao valido")
    void shouldReturn403WhenUsersEndpointIsCalledWithPostWithValidSessionCookie() throws Exception {
        Cookie sessionCookie = sessionFixture.cookie();
        NewUserDTO newUserDTO = userFixture.newUserDTO();

        MockHttpServletResponse response =
                POST().withSessionCookie(sessionCookie).withRequestBody(newUserDTO).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentAsString()).isNotBlank();
        ErrorResponse errorResponse = extractObject(response, ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(errorResponse.getMessage()).isEqualTo("Você não tem autorização para consumir esse endpoint");
        assertThat(errorResponse.getAction()).isEqualTo("Entre em contato com o suporte para solicitar a permissão necessária");
    }

}

