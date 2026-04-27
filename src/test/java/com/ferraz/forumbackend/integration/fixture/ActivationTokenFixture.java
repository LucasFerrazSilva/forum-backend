package com.ferraz.forumbackend.integration.fixture;

import com.ferraz.forumbackend.activationtoken.ActivationTokenEntity;
import com.ferraz.forumbackend.activationtoken.ActivationTokenRepository;
import com.ferraz.forumbackend.activationtoken.ActivationTokenService;
import com.ferraz.forumbackend.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class ActivationTokenFixture {

    private final ActivationTokenRepository activationTokenRepository;
    private final ActivationTokenService activationTokenService;

    public ActivationTokenEntity token(UserEntity user) {
        return token(user, b -> {});
    }

    public ActivationTokenEntity token(UserEntity user, Consumer<ActivationTokenEntityBuilder> customizer) {
        ActivationTokenEntityBuilder builder = new ActivationTokenEntityBuilder(user);
        customizer.accept(builder);
        return activationTokenRepository.save(builder.build());
    }

    public class ActivationTokenEntityBuilder {

        private final UserEntity user;
        private LocalDateTime expiresAt;
        private LocalDateTime usedAt = null;

        public ActivationTokenEntityBuilder(UserEntity user) {
            this.user = user;
            this.expiresAt = LocalDateTime.now().plusDays(activationTokenService.getActivationTokenExpirationDays());
        }

        public ActivationTokenEntityBuilder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public ActivationTokenEntityBuilder usedAt(LocalDateTime usedAt) {
            this.usedAt = usedAt;
            return this;
        }

        public ActivationTokenEntity build() {
            ActivationTokenEntity entity = ActivationTokenEntity.create(user, expiresAt);
            entity.setUsedAt(usedAt);
            return entity;
        }

    }

}

