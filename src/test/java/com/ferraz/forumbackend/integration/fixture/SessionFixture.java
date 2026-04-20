package com.ferraz.forumbackend.integration.fixture;

import com.ferraz.forumbackend.infra.CookieService;
import com.ferraz.forumbackend.session.SessionController;
import com.ferraz.forumbackend.session.SessionEntity;
import com.ferraz.forumbackend.session.SessionService;
import com.ferraz.forumbackend.session.dto.LoginDTO;
import com.ferraz.forumbackend.user.UserEntity;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SessionFixture {

    private final SessionService sessionService;
    private final UserFixture userFixture;
    private final CookieService cookieService;

    public SessionEntity session() {
        return session(null);
    }

    public SessionEntity session(LoginDTO loginDTO) {
        if (loginDTO == null) {
            String senhaValida = "SenhaValida";
            UserEntity user = userFixture.user(b -> b.password(senhaValida));
            loginDTO = new LoginDTO(user.getEmail(), senhaValida);
        }

        return sessionService.getSession(loginDTO);
    }

    public Cookie cookie() {
        return cookie(null);
    }

    public Cookie cookie(LoginDTO loginDTO) {
        SessionEntity sessionEntity = session(loginDTO);
        return cookieService.createSessionCookie(sessionEntity);
    }

}
