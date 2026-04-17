package com.ferraz.forumbackend.session;

import com.ferraz.forumbackend.infra.exception.UnauthorizedException;
import com.ferraz.forumbackend.session.dto.LoginDTO;
import com.ferraz.forumbackend.session.dto.SessionDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;

@RestController
@RequestMapping("api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @Value("${server.cookie.secure}")
    private boolean cookieSecure;

    @Value("${server.cookie.name}")
    private String cookieName;

    @PostMapping
    public ResponseEntity<SessionDTO> getSession(@Valid @RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        SessionEntity sessionEntity = sessionService.getSession(loginDTO);
        SessionDTO sessionDTO = new SessionDTO(sessionEntity.getToken());

        long maxAge = Duration.between(LocalDateTime.now(), sessionEntity.getExpiresAt()).getSeconds();
        Cookie cookie = createCookie(cookieName, cookieSecure, sessionEntity.getToken(), (int) maxAge);
        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.CREATED.value()).body(sessionDTO);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteSession(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            throw new UnauthorizedException();
        }

        Cookie sessionCookie =
            Stream.of(cookies)
                    .filter(cookie -> cookieName.equals(cookie.getName()))
                    .findAny()
                    .orElseThrow(UnauthorizedException::new);

        SessionEntity sessionEntity = sessionService.inactivate(sessionCookie.getValue());

        Cookie cookie = createCookie(cookieName, cookieSecure, sessionEntity.getToken(), 0);
        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }

    public static Cookie createCookie(String cookieName, boolean cookieSecure, String token, int maxAge) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

}
