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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Value("${server.activation-token.base-url}")
    private String activationTokenBaseUrl;

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

        // Validar envio do email com link de ativação
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);
        MimeMessage email = messages[0];
        assertThat(email.getAllRecipients()).hasSize(1);
        assertThat(email.getAllRecipients()[0]).hasToString(newUserDTO.email());
        assertThat(email.getFrom()).hasSize(1);
        assertThat(email.getFrom()[0]).hasToString(from);
        assertThat(email.getSubject()).isEqualTo("Ative seu cadastro");

        String emailContent = (String) email.getContent();
        assertThat(emailContent).contains(newUserDTO.username());
        String regex = "%s/api/v1/activation-token/activate/([\\w-]+)".formatted(activationTokenBaseUrl);
        Matcher matcher = Pattern.compile(regex).matcher(emailContent);
        assertThat(matcher.find()).isTrue();
        UUID tokenId = UUID.fromString(matcher.group(1));

        // Validar criação do token de ativação
        Optional<ActivationTokenEntity> optionalActivationToken = activationTokenRepository.findById(tokenId);
        assertThat(optionalActivationToken).isPresent();
        ActivationTokenEntity activationTokenEntity = optionalActivationToken.get();
        assertThat(activationTokenEntity.getUser().getId()).isEqualTo(userDTO.id());
        assertThat(activationTokenEntity.getUsedAt()).isNull();
        assertThat(activationTokenEntity.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(activationTokenEntity.getCreatedAt()).isNotNull();
        assertThat(activationTokenEntity.getUpdatedAt()).isNotNull();

        // Ativação do usuário
        response =
                GET().withEndpoint("/api/v1/activation-token/activate/" + activationTokenEntity.getId())
                        .withRequestBody(newUserDTO).send();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        userDTO = extractObject(response, UserDTO.class);

        // Validar se usuário foi ativado corretamente
        UserEntity userEntity = userRepository.findById(userDTO.id()).get();
        assertThat(userEntity.getFeatures()).contains("create:session");

        // Fazer login com sucesso
        LoginDTO loginDTO = new LoginDTO(newUserDTO.email(), newUserDTO.password());
        response = POST().withEndpoint("/api/v1/sessions").withRequestBody(loginDTO).send();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        List<Cookie> cookies = List.of(response.getCookies());
        Cookie sessionCookie = cookies.getFirst();

        // Consultar dados do usuário
        response = GET().withEndpoint("/api/v1/users").withSessionCookie(sessionCookie).send();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

}
