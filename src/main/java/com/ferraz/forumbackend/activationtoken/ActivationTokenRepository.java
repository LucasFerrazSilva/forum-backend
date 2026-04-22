package com.ferraz.forumbackend.activationtoken;

import com.ferraz.forumbackend.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ActivationTokenRepository extends JpaRepository<ActivationTokenEntity, UUID> {
    Optional<ActivationTokenEntity> findFirstByUser(UserEntity user);
}
