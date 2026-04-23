package com.ferraz.forumbackend.infra;

import com.ferraz.forumbackend.session.SessionEntity;
import com.ferraz.forumbackend.session.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SessionFilter extends OncePerRequestFilter {

    private final CookieService cookieService;
    private final SessionService sessionService;
    private final SessionHolder sessionHolder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Cookie sessionCookie = cookieService.extractSessionCookie(request);
        SessionEntity session = sessionCookie != null ? sessionService.getSession(sessionCookie.getValue()) : null;
        sessionHolder.setSession(session);
        filterChain.doFilter(request, response);
    }
}
