package com.ferraz.forumbackend.integration.user;

import com.ferraz.forumbackend.infra.exception.ErrorResponse;
import com.ferraz.forumbackend.integration.AbstractIntegrationTest;
import com.ferraz.forumbackend.user.UserEntity;
import com.ferraz.forumbackend.user.UserRepository;
import com.ferraz.forumbackend.user.dto.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class GetUserByUsernameIntegrationTest extends AbstractIntegrationTest {

    @Override
    public String getEndpoint() {
        return "/api/v1/users";
    }

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve retornar 200 (Ok) quando fizer um GET no endpoint '/api/v1/users/{username}' com username válido")
    void shouldReturn200WhenUsersEndpointIsCalledWithGetAndValidUsername() throws Exception {
        UserEntity user = userFixture.user();

        MockHttpServletResponse response = GET().withEndpoint(getEndpoint() + "/" + user.getUsername()).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isNotBlank();

        UserDTO userDTO = extractObject(response, UserDTO.class);
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

        MockHttpServletResponse response = GET().withEndpoint(getEndpoint() + "/" + invalidUsername).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = extractObject(response, ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(errorResponse.getMessage()).isEqualTo("Nenhum usuário encontrado para o username " + invalidUsername);
    }

}

