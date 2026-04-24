package com.ferraz.forumbackend.integration.activationtoken;

import com.ferraz.forumbackend.activationtoken.ActivationTokenEntity;
import com.ferraz.forumbackend.activationtoken.ActivationTokenRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ActivateAccountIntegrationTest extends AbstractIntegrationTest {

    @Override
    public String getEndpoint() {
        return "/api/v1/activation-token/activate/";
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @AfterEach
    void afterEach() {
        activationTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve retornar 200 (OK) com o usuário ativado quando o token for válido")
    void shouldReturn200AndActivateUserWhenTokenIsValid() throws Exception {
        UserEntity user = userFixture.user();
        ActivationTokenEntity token = activationTokenFixture.token(user);

        MockHttpServletResponse response = GET().withEndpoint(getEndpoint() + token.getId()).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isNotBlank();

        UserDTO userDTO = extractObject(response, UserDTO.class);
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.id()).isEqualTo(user.getId());
        assertThat(userDTO.features()).contains("create:session");

        Optional<ActivationTokenEntity> updatedToken = activationTokenRepository.findById(token.getId());
        assertThat(updatedToken).isPresent();
        assertThat(updatedToken.get().getUsedAt()).isNotNull();

        Optional<UserEntity> updatedUser = userRepository.findById(user.getId());
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getFeatures()).contains("create:session");
    }

    @Test
    @DisplayName("Deve retornar 404 (Not Found) quando o token não existir")
    void shouldReturn404WhenTokenDoesNotExist() throws Exception {
        MockHttpServletResponse response = GET().withEndpoint(getEndpoint() + UUID.randomUUID()).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString()).isNotBlank();
        ErrorResponse errorResponse = extractObject(response, ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getMessage()).contains("Código de ativação inválido");
    }

    @Test
    @DisplayName("Deve retornar 404 (Not Found) quando o token já tiver sido utilizado")
    void shouldReturn404WhenTokenIsAlreadyUsed() throws Exception {
        UserEntity user = userFixture.user();
        ActivationTokenEntity token =
                activationTokenFixture.token(user, b -> b.usedAt(LocalDateTime.now().minusMinutes(1)));

        MockHttpServletResponse response = GET().withEndpoint(getEndpoint() + token.getId()).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString()).isNotBlank();
        ErrorResponse errorResponse = extractObject(response, ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getMessage()).contains("Código de ativação inválido");
    }

    @Test
    @DisplayName("Deve retornar 404 (Not Found) quando o token estiver expirado")
    void shouldReturn404WhenTokenIsExpired() throws Exception {
        UserEntity user = userFixture.user();
        ActivationTokenEntity token = activationTokenFixture.token(user, b -> b.expiresAt(LocalDateTime.now().minusDays(1)));

        MockHttpServletResponse response = GET().withEndpoint(getEndpoint() + token.getId()).send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString()).isNotBlank();
        ErrorResponse errorResponse = extractObject(response, ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getMessage()).contains("Código de ativação inválido");
    }

    @Test
    @DisplayName("Deve retornar 404 (Not Found) quando o token já foi utilizado numa segunda tentativa de ativação")
    void shouldReturn404OnSecondActivationAttempt() throws Exception {
        UserEntity user = userFixture.user();
        ActivationTokenEntity token = activationTokenFixture.token(user);

        // Primeira ativação — deve funcionar
        MockHttpServletResponse firstResponse = GET().withEndpoint(getEndpoint() + token.getId()).send();
        assertThat(firstResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        // Segunda ativação — deve falhar
        MockHttpServletResponse secondResponse = GET().withEndpoint(getEndpoint() + token.getId()).send();
        assertThat(secondResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        ErrorResponse errorResponse = extractObject(secondResponse, ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getMessage()).contains("Código de ativação inválido");
    }

}
