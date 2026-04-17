package com.ferraz.forumbackend.session;

import com.ferraz.forumbackend.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {

    Optional<SessionEntity> findFirstByTokenAndExpiresAtAfter(String token, LocalDateTime now);
    Optional<SessionEntity> findFirstByUser(UserEntity user);

}

