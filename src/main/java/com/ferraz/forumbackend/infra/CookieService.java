package com.ferraz.forumbackend.infra;

import com.ferraz.forumbackend.infra.exception.UnauthorizedException;
import com.ferraz.forumbackend.session.SessionEntity;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;

@Service
public class CookieService {

    @Value("${server.cookie.secure}")
    private boolean cookieSecure;

    @Value("${server.cookie.name}")
    private String cookieName;


    public Cookie createSessionCookie(SessionEntity sessionEntity) {
        Cookie cookie = new Cookie(cookieName, sessionEntity.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");

        if (sessionEntity.getExpiresAt().isAfter(LocalDateTime.now())) {
            long maxAge = Duration.between(LocalDateTime.now(), sessionEntity.getExpiresAt()).getSeconds();
            cookie.setMaxAge((int) maxAge);
        } else {
            cookie.setMaxAge(0);
        }

        return cookie;
    }

    public Cookie createExpiredSessionCookie() {
        Cookie cookie = new Cookie(cookieName, "invalid");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        return cookie;
    }

    public Cookie getSessionCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) {
            throw new UnauthorizedException();
        }

        return Stream.of(cookies)
                        .filter(cookie -> cookieName.equals(cookie.getName()))
                        .findAny()
                        .orElseThrow(UnauthorizedException::new);
    }

}
