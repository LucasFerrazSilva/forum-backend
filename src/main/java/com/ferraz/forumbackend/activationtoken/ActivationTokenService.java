package com.ferraz.forumbackend.activationtoken;

import com.ferraz.forumbackend.activationtoken.exception.InvalidActivationTokenException;
import com.ferraz.forumbackend.infra.service.EmailService;
import com.ferraz.forumbackend.user.UserEntity;
import com.ferraz.forumbackend.user.UserService;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivationTokenService {

    private final ActivationTokenRepository activationTokenRepository;
    private final UserService userService;
    private final EmailService emailService;

    @Value("${server.activation-token.base-url}")
    private String baseUrl;

    @Value("${server.activation-token.expiration-days}")
    @Getter
    private int activationTokenExpirationDays;

    @Transactional
    public ActivationTokenEntity create(UserEntity userEntity) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(activationTokenExpirationDays);
        ActivationTokenEntity activationTokenEntity = ActivationTokenEntity.create(userEntity, expiresAt);
        return activationTokenRepository.save(activationTokenEntity);
    }

    public void sendActivationEmail(UserEntity userEntity, ActivationTokenEntity activationTokenEntity) {
        String activationUrl = baseUrl + "/api/v1/activation-token/activate/" + activationTokenEntity.getId();

        emailService.send(
                userEntity.getEmail(),
                "Ative seu cadastro",
                """
                %s, clique no link abaixo para ativar sua conta:
            
                %s
                
                Atenciosamente,
                Equipe do fórum
                """.formatted(userEntity.getUsername(), activationUrl)
        );
    }

    @Transactional
    public UserEntity activate(UUID id) {
        ActivationTokenEntity activationTokenEntity =
                activationTokenRepository.findById(id).orElseThrow(() -> new InvalidActivationTokenException(id));
        if (activationTokenEntity.getUsedAt() != null) {
            throw new InvalidActivationTokenException(id);
        }
        if (activationTokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidActivationTokenException(id);
        }
        activationTokenEntity.setUsedAt(LocalDateTime.now());
        activationTokenEntity.setUpdatedAt(LocalDateTime.now());
        activationTokenRepository.save(activationTokenEntity);
        return userService.setFeatures(activationTokenEntity.getUser(), new String[]{"create:session"});
    }
}
