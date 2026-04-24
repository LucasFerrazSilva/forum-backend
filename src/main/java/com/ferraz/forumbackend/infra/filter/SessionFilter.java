package com.ferraz.forumbackend.infra.filter;

import com.ferraz.forumbackend.infra.service.CookieService;
import com.ferraz.forumbackend.infra.service.UserContext;
import com.ferraz.forumbackend.session.SessionEntity;
import com.ferraz.forumbackend.session.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SessionFilter extends OncePerRequestFilter {

    private final CookieService cookieService;
    private final SessionService sessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            Cookie sessionCookie = cookieService.extractSessionCookie(request);
            SessionEntity session = sessionCookie != null ? sessionService.getSession(sessionCookie.getValue()) : null;
            UserContext.setSession(session);
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
