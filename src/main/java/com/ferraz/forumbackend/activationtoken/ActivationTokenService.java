package com.ferraz.forumbackend.activationtoken;

import com.ferraz.forumbackend.activationtoken.exception.InvalidActivationTokenException;
import com.ferraz.forumbackend.infra.EmailService;
import com.ferraz.forumbackend.user.UserEntity;
import com.ferraz.forumbackend.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivationTokenService {

    private final ActivationTokenRepository activationTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${server.activation-token.base-url}")
    private String baseUrl;

    @Transactional
    public ActivationTokenEntity create(UserEntity userEntity) {
        ActivationTokenEntity activationTokenEntity = ActivationTokenEntity.create(userEntity);
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
        activationTokenEntity.setUsedAt(LocalDateTime.now());
        activationTokenEntity.setUpdatedAt(LocalDateTime.now());
        activationTokenRepository.save(activationTokenEntity);
        UserEntity user = activationTokenEntity.getUser();
        user.setFeatures(new String[]{"usuario-ativado"});
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }
}
