package com.ferraz.forumbackend.activationtoken;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActivationTokenRepository extends JpaRepository<ActivationTokenEntity, UUID> {
}
