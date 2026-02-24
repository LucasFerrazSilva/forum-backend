package com.ferraz.forumbackend.integration.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferraz.forumbackend.infra.exception.ErrorResponse;
import com.ferraz.forumbackend.infra.exception.InvalidField;
import com.ferraz.forumbackend.integration.AbstractIntegrationTest;
import com.ferraz.forumbackend.user.UserRepository;
import com.ferraz.forumbackend.user.dto.NewUserDTO;
import com.ferraz.forumbackend.user.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.ferraz.forumbackend.integration.util.MvcUtil.post;
import static org.assertj.core.api.Assertions.assertThat;

class UserControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String ENDPOINT = "/api/v1/users";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve retornar 201 (Created) quando fizer um POST no endpoint '/api/v1/users'")
    void shouldReturn201WhenUsersEndpointIsCalledWithPost() throws Exception {
        NewUserDTO newUserDTO = new NewUserDTO(
                "username",
                "email@domain.com",
                "senha123"
        );
        String requestBody = objectMapper.writeValueAsString(newUserDTO);

        MockHttpServletResponse response = post(mvc, ENDPOINT, requestBody);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        assertThat(response.getContentAsString()).isNotBlank();

        UserDTO userDTO = objectMapper.readValue(response.getContentAsString(), UserDTO.class );
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.id()).isNotNull();
        assertThat(userDTO.username()).isEqualTo(newUserDTO.username());
        assertThat(userDTO.email()).isEqualTo(newUserDTO.email());
        assertThat(userDTO.createdAt()).isNotNull();
        assertThat(userDTO.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um POST no endpoint '/api/v1/users' sem enviar o body")
    void shouldReturn400WhenUsersEndpointIsCalledWithPostWithoutBody() throws Exception {
        MockHttpServletResponse response = post(mvc, ENDPOINT, "");
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isNotBlank();
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um POST no endpoint '/api/v1/users' sem enviar os campos necessários")
    void shouldReturn400WhenUsersEndpointIsCalledWithPostWithoutRequiredFields() throws Exception {
        NewUserDTO newUserDTO = new NewUserDTO(
                "",
                "  ",
                null
        );
        String requestBody = objectMapper.writeValueAsString(newUserDTO);

        MockHttpServletResponse response = post(mvc, ENDPOINT, requestBody);

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
        NewUserDTO newUserDTO1 = new NewUserDTO(
                "username1",
                "email@domain.com",
                "senha123"
        );
        String requestBody1 = objectMapper.writeValueAsString(newUserDTO1);

        NewUserDTO newUserDTO2 = new NewUserDTO(
                "username2",
                "Email@domain.com",
                "senha123"
        );
        String requestBody2 = objectMapper.writeValueAsString(newUserDTO2);

        MockHttpServletResponse response = post(mvc, ENDPOINT, requestBody1);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        response = post(mvc, ENDPOINT, requestBody2);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getInvalidFields()).isNotEmpty();
        assertThat(errorResponse.getInvalidFields().getFirst().field()).isEqualTo("email");
    }

    @Test
    @DisplayName("Deve retornar 400 (Bad Request) quando fizer um POST no endpoint '/api/v1/users' usando um username ja cadastrado")
    void shouldReturn400WhenUsersEndpointIsCalledWithPostWithNonUniqueUsername() throws Exception {
        NewUserDTO newUserDTO1 = new NewUserDTO(
                "NonUniqueUsername",
                "email1@domain.com",
                "senha123"
        );
        String requestBody1 = objectMapper.writeValueAsString(newUserDTO1);

        NewUserDTO newUserDTO2 = new NewUserDTO(
                "nonuniqueusername",
                "Email2@domain.com",
                "senha123"
        );
        String requestBody2 = objectMapper.writeValueAsString(newUserDTO2);

        MockHttpServletResponse response = post(mvc, ENDPOINT, requestBody1);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        response = post(mvc, ENDPOINT, requestBody2);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isNotBlank();

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getInvalidFields()).isNotEmpty();
        assertThat(errorResponse.getInvalidFields().getFirst().field()).isEqualTo("username");
    }

}
