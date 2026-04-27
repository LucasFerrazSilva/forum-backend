package com.ferraz.forumbackend.integration.user;

import com.ferraz.forumbackend.infra.exception.ErrorResponse;
import com.ferraz.forumbackend.integration.AbstractIntegrationTest;
import com.ferraz.forumbackend.session.SessionRepository;
import com.ferraz.forumbackend.session.dto.LoginDTO;
import com.ferraz.forumbackend.user.UserEntity;
import com.ferraz.forumbackend.user.UserRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateUserIntegrationTest extends AbstractIntegrationTest {

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
    @DisplayName("Deve retornar 404 (Not Found) quando fizer um PATCH no endpoint '/api/v1/users/{username}' passando um username inexistente")
    void shouldReturn404WhenUsersEndpointIsCalledWithPatchAndInvalidUsername() throws Exception {
        String invalidUsername = "invalidUsername";
        UpdateUserDTO updateUserDTO = userFixture.updateUserDTO(b -> b.username("newUsername"));

        MockHttpServletResponse response =
                PATCH().withDefaultUser()
                        .withEndpoint(getEndpoint() + "/" + invalidUsername)
                        .withRequestBody(updateUserDTO).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = extractObject(response, ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(errorResponse.getMessage()).isEqualTo("Nenhum usuário encontrado para o username " + invalidUsername);
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um PATCH no endpoint '/api/v1/users/{username}' passando um username ja utilizado")
    void shouldReturn400WhenUsersEndpointIsCalledWithPatchAndUsernameBeeingUsed() throws Exception {
        LoginDTO loginDTO = new LoginDTO("superuser@mail.com", "SuperUser");
        UserEntity existingUser = userFixture.user(b ->
                b.withLoginDTO(loginDTO).withFeatures(new String[]{"update:user:other"})
        );
        Cookie cookie = sessionFixture.cookie(loginDTO);
        UserEntity userBeeingUpdated = userFixture.user();

        UpdateUserDTO updateUserDTO = userFixture.updateUserDTO(b -> b.username(existingUser.getUsername()));

        MockHttpServletResponse response =
                PATCH().withEndpoint(getEndpoint() + "/" + userBeeingUpdated.getUsername())
                        .withSessionCookie(cookie).withRequestBody(updateUserDTO).send();

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
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um PATCH no endpoint '/api/v1/users/{username}' passando um email ja utilizado")
    void shouldReturn400WhenUsersEndpointIsCalledWithPatchAndEmailBeeingUsed() throws Exception {
        LoginDTO loginDTO = new LoginDTO("superuser@mail.com", "SuperUser");
        UserEntity existingUser = userFixture.user(b ->
                b.withLoginDTO(loginDTO).withFeatures(new String[]{"update:user:other"})
        );
        Cookie cookie = sessionFixture.cookie(loginDTO);

        UserEntity userBeeingUpdated = userFixture.user();

        UpdateUserDTO updateUserDTO = userFixture.updateUserDTO(b -> b.email(existingUser.getEmail()));

        MockHttpServletResponse response =
                PATCH().withEndpoint(getEndpoint() + "/" + userBeeingUpdated.getUsername())
                        .withSessionCookie(cookie).withRequestBody(updateUserDTO).send();

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
    @DisplayName("Deve retornar 200 (Ok) quando fizer um PATCH no endpoint '/api/v1/users/{username}' passando dados válidos")
    void shouldReturn200WhenUsersEndpointIsCalledWithPatchAndValidData() throws Exception {
        LoginDTO loginDTO = new LoginDTO("EmailAntigo@mail.com", "SenhaAntiga");
        UserEntity user = userFixture.user(b ->
                b.withLoginDTO(loginDTO).withFeatures(new String[]{"update:user:other"})
        );
        UpdateUserDTO updateUserDTO = userFixture.updateUserDTO(b -> {
            b.username("NovoUsername");
            b.email("NovoEmail@mail.com");
            b.password("NovaSenha");
        });
        Cookie cookie = sessionFixture.cookie(loginDTO);

        MockHttpServletResponse response =
                PATCH().withEndpoint(getEndpoint() + "/" + user.getUsername())
                        .withSessionCookie(cookie)
                        .withRequestBody(updateUserDTO).send();

        validateNewUser(response, user, updateUserDTO);
    }

    @Test
    @DisplayName("Deve retornar 200 (Ok) quando fizer um PATCH no endpoint '/api/v1/users/{username}' com super usuario")
    void shouldReturn200WhenUsersEndpointIsCalledWithPatchAndSuperUser() throws Exception {
        LoginDTO loginDTO = new LoginDTO("superuser@mail.com", "SuperUser");
        userFixture.user(b ->
                b.withLoginDTO(loginDTO).withFeatures(new String[]{"update:user:other"})
        );
        Cookie cookie = sessionFixture.cookie(loginDTO);

        UserEntity user = userFixture.user(b -> {
            b.withUsername("UsernameAntigo");
            b.withEmail("EmailAntigo@mail.com");
            b.withPassword("SenhaAntiga");
        });
        UpdateUserDTO updateUserDTO = userFixture.updateUserDTO(b -> {
            b.username("NovoUsername");
            b.email("NovoEmail@mail.com");
            b.password("NovaSenha");
        });

        MockHttpServletResponse response =
                PATCH().withEndpoint(getEndpoint() + "/" + user.getUsername())
                        .withSessionCookie(cookie)
                        .withRequestBody(updateUserDTO).send();

        validateNewUser(response, user, updateUserDTO);
    }

    private void validateNewUser(MockHttpServletResponse response, UserEntity user, UpdateUserDTO updateUserDTO) throws Exception {
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        assertThat(response.getContentAsString()).isNotBlank();
        UserDTO userDTO = extractObject(response, UserDTO.class);
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
    @DisplayName("Deve retornar 401 (Unauthorized) quando fizer um PATCH no endpoint '/api/v1/users/{username}' passando usuário nao ativado")
    void shouldReturn401WhenUsersEndpointIsCalledWithPatchAndNonActivatedUser() throws Exception {
        UserEntity nonActivatedUser = userFixture.user(b -> b.withActivated(false));
        UpdateUserDTO updateUserDTO = userFixture.updateUserDTO(b -> b.email("novoemail@mail.com"));

        MockHttpServletResponse response =
                PATCH().withEndpoint(getEndpoint() + "/" + nonActivatedUser.getUsername()).withRequestBody(updateUserDTO).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentAsString()).isNotBlank();
        ErrorResponse errorResponse = extractObject(response, ErrorResponse.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("Deve retornar 403 (Forbidden) quando fizer um PATCH no endpoint '/api/v1/users/{username}' passando usuário diferente")
    void shouldReturn403WhenUsersEndpointIsCalledWithPatchAndDiferentUser() throws Exception {
        UserEntity user1 = userFixture.user();
        String user2Password = "User2Password";
        UserEntity user2 = userFixture.user(b -> b.withPassword(user2Password));
        Cookie user2SessionCookie = sessionFixture.cookie(new LoginDTO(user2.getEmail(), user2Password));
        UpdateUserDTO updateUserDTO = userFixture.updateUserDTO(b -> b.email("novoemail@mail.com"));

        MockHttpServletResponse response =
                PATCH().withEndpoint(getEndpoint() + "/" + user1.getUsername())
                        .withSessionCookie(user2SessionCookie)
                        .withRequestBody(updateUserDTO).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentAsString()).isNotBlank();
        ErrorResponse errorResponse = extractObject(response, ErrorResponse.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(errorResponse.getMessage()).isEqualTo("Você não tem autorização para atualizar outro usuário");
    }

}

