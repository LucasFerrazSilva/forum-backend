package com.ferraz.forumbackend.integration.user;

import com.ferraz.forumbackend.infra.exception.ErrorResponse;
import com.ferraz.forumbackend.infra.exception.InvalidField;
import com.ferraz.forumbackend.integration.AbstractIntegrationTest;
import com.ferraz.forumbackend.integration.session.SessionControllerIntegrationTest;
import com.ferraz.forumbackend.session.SessionEntity;
import com.ferraz.forumbackend.session.SessionRepository;
import com.ferraz.forumbackend.session.dto.LoginDTO;
import com.ferraz.forumbackend.user.UserEntity;
import com.ferraz.forumbackend.user.UserRepository;
import com.ferraz.forumbackend.user.dto.NewUserDTO;
import com.ferraz.forumbackend.user.dto.UpdateUserDTO;
import com.ferraz.forumbackend.user.dto.UserDTO;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String ENDPOINT = "/api/v1/users";

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
        String requestBody = objectMapper.writeValueAsString(newUserDTO);

        MockHttpServletResponse response = post(ENDPOINT, requestBody);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        assertThat(response.getContentAsString()).isNotBlank();

        UserDTO userDTO = objectMapper.readValue(response.getContentAsString(), UserDTO.class );
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.id()).isNotNull();
        assertThat(userDTO.username()).isEqualTo(newUserDTO.username());
        assertThat(userDTO.email()).isEqualTo(newUserDTO.email());
        assertThat(userDTO.createdAt()).isNotNull();
        assertThat(userDTO.updatedAt()).isNotNull();

        Optional<UserEntity> userEntityOptional = userRepository.findById(userDTO.id());
        assertThat(userEntityOptional).isPresent();
        UserEntity userEntity = userEntityOptional.get();
        assertThat(passwordEncoder.matches(newUserDTO.password(), userEntity.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("SenhaInvalida", userEntity.getPassword())).isFalse();
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um POST no endpoint '/api/v1/users' sem enviar o body")
    void shouldReturn400WhenUsersEndpointIsCalledWithPostWithoutBody() throws Exception {
        MockHttpServletResponse response = post(ENDPOINT, "");
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isNotBlank();
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um POST no endpoint '/api/v1/users' sem enviar os campos necessários")
    void shouldReturn400WhenUsersEndpointIsCalledWithPostWithoutRequiredFields() throws Exception {
        NewUserDTO newUserDTO =
                userFixture.newUserDTO(u -> u.username("").email(" ").password(null));

        String requestBody = objectMapper.writeValueAsString(newUserDTO);

        MockHttpServletResponse response = post(ENDPOINT, requestBody);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        List<String> invalidFieldNames =
                errorResponse.getInvalidFields().stream().map(InvalidField::field).toList();
        assertThat(invalidFieldNames).containsAll(List.of("username", "email", "password"));
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um POST no endpoint '/api/v1/users' usando um email ja cadastrado")
    void shouldReturn400WhenUsersEndpointIsCalledWithPostWithNonUniqueEmail() throws Exception {
        UserEntity existingUser =
                userFixture.user(u -> u.email("Email@domain.com"));

        NewUserDTO newUserDTO =
                userFixture.newUserDTO(u -> u.email(existingUser.getEmail().toLowerCase()));

        String requestBody = objectMapper.writeValueAsString(newUserDTO);

        MockHttpServletResponse response = post(ENDPOINT, requestBody);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
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
                userFixture.user(u -> u.username("NonUniqueUsername"));

        NewUserDTO newUserDTO =
                userFixture.newUserDTO(u -> u.username(existingUser.getUsername().toLowerCase()));

        String requestBody = objectMapper.writeValueAsString(newUserDTO);

        MockHttpServletResponse response = post(ENDPOINT, requestBody);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getInvalidFields()).isNotEmpty();
        assertThat(errorResponse.getInvalidFields().getFirst().field()).isEqualTo("username");
        assertThat(errorResponse.getInvalidFields().getFirst().message())
                .isEqualTo("O username %s já está cadastrado".formatted(existingUser.getUsername()));
    }

    @Test
    @DisplayName("Deve retornar 200 (Ok) quando fizer um GET no endpoint '/api/v1/users/{username}' com username válido")
    void shouldReturn200WhenUsersEndpointIsCalledWithGetAndValidUsername() throws Exception {
        UserEntity user = userFixture.user();

        MockHttpServletResponse response = get(ENDPOINT + "/" + user.getUsername());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isNotBlank();

        UserDTO userDTO = objectMapper.readValue(response.getContentAsString(), UserDTO.class );
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.id()).isNotNull();
        assertThat(userDTO.username()).isEqualTo(user.getUsername());
        assertThat(userDTO.email()).isEqualTo(user.getEmail());
        assertThat(userDTO.createdAt()).isNotNull();
        assertThat(userDTO.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar 404 (Not found) quando fizer um GET no endpoint '/api/v1/users/{username}' com username inválido")
    void shouldReturn404WhenUsersEndpointIsCalledWithGetAndInvalidUsername() throws Exception {
        String invalidUsername = "invalidUsername";

        MockHttpServletResponse response = get(ENDPOINT + "/" + invalidUsername);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(errorResponse.getMessage()).isEqualTo("Nenhum usuário encontrado para o username " + invalidUsername);
    }

    @Test
    @DisplayName("Deve retornar 404 (Not Found) quando fizer um PATCH no endpoint '/api/v1/users/{username}' passando um username inexistente")
    void shouldReturn404WhenUsersEndpointIsCalledWithPatchAndInvalidUsername() throws Exception {
        UserEntity userEntity = userFixture.user();
        String requestBody = objectMapper.writeValueAsString(userEntity);

        String invalidUsername = "invalidUsername";

        MockHttpServletResponse response = patch(ENDPOINT + "/" + invalidUsername, requestBody);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(errorResponse.getMessage()).isEqualTo("Nenhum usuário encontrado para o username " + invalidUsername);
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um PATCH no endpoint '/api/v1/users/{username}' passando um username ja utilizado")
    void shouldReturn400WhenUsersEndpointIsCalledWithPatchAndUsernameBeeingUsed() throws Exception {
        UserEntity existingUser = userFixture.user();
        UserEntity userBeeingUpdated = userFixture.user();

        UpdateUserDTO updateUserDTO = userFixture.updateUserDTO(b -> b.username(existingUser.getUsername()));
        String requestBody = objectMapper.writeValueAsString(updateUserDTO);

        MockHttpServletResponse response = patch(ENDPOINT + "/" + userBeeingUpdated.getUsername(), requestBody);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getInvalidFields()).isNotEmpty();
        assertThat(errorResponse.getInvalidFields().getFirst().field()).isEqualTo("username");
        assertThat(errorResponse.getInvalidFields().getFirst().message())
                .isEqualTo("O username %s já está cadastrado".formatted(existingUser.getUsername()));
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um PATCH no endpoint '/api/v1/users/{username}' passando um email ja utilizado")
    void shouldReturn400WhenUsersEndpointIsCalledWithPatchAndEmailBeeingUsed() throws Exception {
        UserEntity existingUser = userFixture.user();
        UserEntity userBeeingUpdated = userFixture.user();

        UpdateUserDTO updateUserDTO = userFixture.updateUserDTO(b -> b.email(existingUser.getEmail()));
        String requestBody = objectMapper.writeValueAsString(updateUserDTO);

        MockHttpServletResponse response = patch(ENDPOINT + "/" + userBeeingUpdated.getUsername(), requestBody);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getInvalidFields()).isNotEmpty();
        assertThat(errorResponse.getInvalidFields().getFirst().field()).isEqualTo("email");
        assertThat(errorResponse.getInvalidFields().getFirst().message())
                .isEqualTo("O email %s já está cadastrado".formatted(existingUser.getEmail()));
    }

    @Test
    @DisplayName("Deve retornar 200 (Ok) quando fizer um PATCH no endpoint '/api/v1/users/{username}' passando dados válidos")
    void shouldReturn200WhenUsersEndpointIsCalledWithPatchAndValidData() throws Exception {
        UserEntity user = userFixture.user(b -> {
            b.username("UsernameAntigo");
            b.email("EmailAntigo@mail.com");
            b.password("SenhaAntiga");
        });
        UpdateUserDTO updateUserDTO = userFixture.updateUserDTO(b -> {
            b.username("NovoUsername");
            b.email("NovoEmail@mail.com");
            b.password("NovaSenha");
        });
        String requestBody = objectMapper.writeValueAsString(updateUserDTO);

        MockHttpServletResponse response = patch(ENDPOINT + "/" + user.getUsername(), requestBody);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        assertThat(response.getContentAsString()).isNotBlank();

        UserDTO userDTO = objectMapper.readValue(response.getContentAsString(), UserDTO.class );
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.id()).isEqualTo(user.getId());
        assertThat(userDTO.username()).isEqualTo(updateUserDTO.username());
        assertThat(userDTO.email()).isEqualTo(updateUserDTO.email());
        assertThat(userDTO.createdAt()).isNotNull();
        assertThat(userDTO.updatedAt()).isNotNull();
        assertThat(userDTO.updatedAt()).isAfter(userDTO.createdAt());

        Optional<UserEntity> userEntityOptional = userRepository.findById(userDTO.id());
        assertThat(userEntityOptional).isPresent();
        UserEntity userEntity = userEntityOptional.get();
        assertThat(passwordEncoder.matches(updateUserDTO.password(), userEntity.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("SenhaAntiga", userEntity.getPassword())).isFalse();
    }

    @Test
    @DisplayName("Deve retornar 401 (Unauthorized) quando fizer um GET no endpoint '/api/v1/users/' sem enviar o cookie de sessão")
    void shouldReturn401WhenUsersEndpointIsCalledWithGetAndNoSessionCookie() throws Exception {
        MockHttpServletResponse response = get(ENDPOINT);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
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
        // criar usuario, pegar loginDTO
        String senhaValida = "SenhaValida";
        UserEntity user = userFixture.user(b -> b.password(senhaValida));
        LoginDTO loginDTO = new LoginDTO(user.getEmail(), senhaValida);

        // buscar session
        String requestBody = objectMapper.writeValueAsString(loginDTO);
        MockHttpServletResponse response = post(SessionControllerIntegrationTest.ENDPOINT, requestBody);
        List<Cookie> cookies = List.of(response.getCookies());
        Cookie sessionCookie = cookies.getFirst();

        // atualizar expiracao da session do usuario
        SessionEntity sessionEntity = sessionRepository.findFirstByUser(user).get();
        sessionEntity.setExpiresAt(LocalDateTime.now().minusDays(1));
        sessionRepository.save(sessionEntity);

        response = get(ENDPOINT, sessionCookie);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
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
        // criar usuario, pegar loginDTO
        String senhaValida = "SenhaValida";
        UserEntity user = userFixture.user(b -> b.password(senhaValida));
        LoginDTO loginDTO = new LoginDTO(user.getEmail(), senhaValida);

        // buscar session
        String requestBody = objectMapper.writeValueAsString(loginDTO);
        MockHttpServletResponse response = post(SessionControllerIntegrationTest.ENDPOINT, requestBody);
        List<Cookie> cookies = List.of(response.getCookies());
        Cookie sessionCookie = cookies.getFirst();
        sessionCookie.setValue("Invalid value");

        // atualizar expiracao da session do usuario
        SessionEntity sessionEntity = sessionRepository.findFirstByUser(user).get();
        sessionEntity.setExpiresAt(LocalDateTime.now().minusDays(1));
        sessionRepository.save(sessionEntity);

        response = get(ENDPOINT, sessionCookie);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(errorResponse.getName()).isEqualTo("UnauthorizedException");
        assertThat(errorResponse.getMessage()).isEqualTo("Cookie de sessão não enviado ou inválido");
        assertThat(errorResponse.getAction()).isEqualTo("Verifique se um cookie válido de sessão está sendo enviado no cabeçalho da requisição");
        assertThat(errorResponse.getInvalidFields()).isNull();
    }

    @Test
    @DisplayName("Deve retornar 200 (Ok) quando fizer um GET no endpoint '/api/v1/users' com um cookie de sessão válido")
    void shouldReturn200WhenUsersEndpointIsCalledWithGetAndValidSessionCookie() throws Exception {
        // criar usuario, pegar loginDTO
        String senhaValida = "SenhaValida";
        UserEntity user = userFixture.user(b -> b.password(senhaValida));
        LoginDTO loginDTO = new LoginDTO(user.getEmail(), senhaValida);

        // buscar session
        String requestBody = objectMapper.writeValueAsString(loginDTO);
        MockHttpServletResponse response = post(SessionControllerIntegrationTest.ENDPOINT, requestBody);
        List<Cookie> cookies = List.of(response.getCookies());
        Cookie sessionCookie = cookies.getFirst();

        // Buscar sessão
        SessionEntity sessionEntity = sessionRepository.findFirstByUser(user).get();
        LocalDateTime previousExpiresAt = sessionEntity.getExpiresAt();

        response = get(ENDPOINT, sessionCookie);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isNotBlank();

        UserDTO userDTO = objectMapper.readValue(response.getContentAsString(), UserDTO.class );
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.id()).isEqualTo(user.getId());
        assertThat(userDTO.username()).isEqualTo(user.getUsername());
        assertThat(userDTO.email()).isEqualTo(user.getEmail());
        assertThat(userDTO.createdAt()).isNotNull();
        assertThat(userDTO.updatedAt()).isNotNull();

        sessionEntity = sessionRepository.findFirstByUser(user).get();
        LocalDateTime actualExpiresAt = sessionEntity.getExpiresAt();
        assertThat(actualExpiresAt).isAfter(previousExpiresAt);
    }

}
