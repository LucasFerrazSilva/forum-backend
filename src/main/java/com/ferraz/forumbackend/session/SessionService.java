package com.ferraz.forumbackend.session;

import com.ferraz.forumbackend.infra.exception.ForbiddenException;
import com.ferraz.forumbackend.infra.service.AuthorizationService;
import com.ferraz.forumbackend.session.dto.LoginDTO;
import com.ferraz.forumbackend.session.exception.InvalidCredentialsException;
import com.ferraz.forumbackend.user.UserEntity;
import com.ferraz.forumbackend.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorizationService authorizationService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${server.cookie.expiration-days}")
    @Getter
    private int sessionExpirationDays;

    public SessionEntity getSession(LoginDTO loginDTO) {
        UserEntity user = userRepository.findFirstByEmail(loginDTO.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!authorizationService.can(user, "create:session")) {
            throw new ForbiddenException();
        }

        if (!passwordEncoder.matches(loginDTO.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(getSessionExpirationDays());

        SessionEntity sessionEntity = SessionEntity.create(user, token, expiresAt);
        return sessionRepository.save(sessionEntity);
    }

    @Transactional
    public SessionEntity getSession(String token) {
        Optional<SessionEntity> optionalSession =
                sessionRepository.findFirstByTokenAndExpiresAtAfter(token, LocalDateTime.now());

        optionalSession.ifPresent(sessionEntity -> {
            sessionEntity.setExpiresAt(LocalDateTime.now().plusDays(getSessionExpirationDays()));
            sessionEntity.setUpdatedAt(LocalDateTime.now());
            sessionRepository.save(sessionEntity);
        });

        return optionalSession.orElse(null);
    }

    @Transactional
    public SessionEntity inactivate(SessionEntity sessionEntity) {
        sessionEntity.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        sessionEntity.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(sessionEntity);
        return sessionEntity;
    }
}
