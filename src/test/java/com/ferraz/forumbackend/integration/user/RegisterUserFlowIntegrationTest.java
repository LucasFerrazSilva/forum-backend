package com.ferraz.forumbackend.integration.user;

import com.ferraz.forumbackend.activationtoken.ActivationTokenEntity;
import com.ferraz.forumbackend.activationtoken.ActivationTokenRepository;
import com.ferraz.forumbackend.integration.AbstractIntegrationTest;
import com.ferraz.forumbackend.session.SessionRepository;
import com.ferraz.forumbackend.session.dto.LoginDTO;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterUserFlowIntegrationTest extends AbstractIntegrationTest {

    @Override
    public String getEndpoint() {
        return "/api/v1/users";
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Value("${spring.mail.username}")
    private String from;

    @AfterEach
    void afterEach() {
        activationTokenRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    @DisplayName("Deve retornar validar o fluxo completo de registro e ativação de usuário")
    void shouldValidateFullUserRegistrationFlow() throws Exception {
        // Registrar usuário
        NewUserDTO newUserDTO = userFixture.newUserDTO();
        MockHttpServletResponse response =
                POST().withEndpoint("/api/v1/users").withRequestBody(newUserDTO).send();

        // Validar se usuário foi criado corretamente
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        UserDTO userDTO = extractObject(response, UserDTO.class);
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.id()).isNotNull();
        assertThat(userDTO.username()).isEqualTo(newUserDTO.username());
        assertThat(userDTO.email()).isEqualTo(newUserDTO.email());
        assertThat(userDTO.createdAt()).isNotNull();
        assertThat(userDTO.updatedAt()).isNotNull();
        assertThat(userDTO.features()).contains("read:activation_token");

        // Validar criação do token de ativação
        UserEntity userEntity = userRepository.findById(userDTO.id()).get();
        Optional<ActivationTokenEntity> optionalActivationToken = activationTokenRepository.findFirstByUser(userEntity);
        assertThat(optionalActivationToken).isPresent();
        ActivationTokenEntity activationTokenEntity = optionalActivationToken.get();
        assertThat(activationTokenEntity.getUsedAt()).isNull();
        assertThat(activationTokenEntity.getCreatedAt()).isNotNull();
        assertThat(activationTokenEntity.getUpdatedAt()).isNotNull();

        // Validar envio do email com link de ativação
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages[0].getAllRecipients()).hasSize(1);
        assertThat(messages[0].getAllRecipients()[0]).hasToString(newUserDTO.email());
        assertThat(messages[0].getFrom()).hasSize(1);
        assertThat(messages[0].getFrom()[0]).hasToString(from);
        assertThat(messages[0].getSubject()).isEqualTo("Ative seu cadastro");
        assertThat(messages[0].getContent()).asString().contains(newUserDTO.username());
        assertThat(messages[0].getContent()).asString().containsAnyOf("http://", "https://");

        // Ativação do usuário
        response =
                GET().withEndpoint("/api/v1/activation-token/activate/" + activationTokenEntity.getId())
                        .withRequestBody(newUserDTO).send();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        userDTO = extractObject(response, UserDTO.class);
        assertThat(userDTO.features()).contains("usuario-ativado");

        // Validar se usuário foi ativado corretamente
        userEntity = userRepository.findById(userDTO.id()).get();
        assertThat(userEntity.getFeatures()).contains("usuario-ativado");

        // Fazer login com sucesso
        LoginDTO loginDTO = new LoginDTO(newUserDTO.email(), newUserDTO.password());
        response = POST().withEndpoint("/api/v1/sessions").withRequestBody(loginDTO).send();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        List<Cookie> cookies = List.of(response.getCookies());
        Cookie sessionCookie = cookies.getFirst();

        // Consultar dados do usuário
        response = GET().withEndpoint("/api/v1/users").withSessionCookie(sessionCookie).send();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isNotBlank();
    }

}
