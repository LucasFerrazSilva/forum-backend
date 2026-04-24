package com.ferraz.forumbackend.infra.service;

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
        int maxAge = (int) Duration.between(LocalDateTime.now(), sessionEntity.getExpiresAt()).getSeconds();
        return createSessionCookie(sessionEntity.getToken(), maxAge);
    }

    public Cookie createExpiredSessionCookie(String token) {
        return createSessionCookie(token, 0);
    }

    private Cookie createSessionCookie(String token, int maxAge) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    public Cookie extractSessionCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        return Stream.of(cookies)
                        .filter(cookie -> cookieName.equals(cookie.getName()))
                        .findAny().orElse(null);
    }

}
