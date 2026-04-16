package com.ferraz.forumbackend.session;

import com.ferraz.forumbackend.session.dto.LoginDTO;
import com.ferraz.forumbackend.session.dto.SessionDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @Value("${server.cookie.secure}")
    private boolean cookieSecure;

    @PostMapping
    public ResponseEntity<SessionDTO> getSession(@Valid @RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        SessionEntity sessionEntity = sessionService.getSession(loginDTO);
        SessionDTO sessionDTO = new SessionDTO(sessionEntity.getToken());

        Cookie cookie = new Cookie("session_id", sessionEntity.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");

        long maxAge = Duration.between(LocalDateTime.now(), sessionEntity.getExpiresAt()).getSeconds();
        cookie.setMaxAge((int) maxAge);

        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.CREATED.value()).body(sessionDTO);
    }

}
