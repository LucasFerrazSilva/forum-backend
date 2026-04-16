package com.ferraz.forumbackend.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {

    Optional<SessionEntity> findFirstByToken(String token);

}

