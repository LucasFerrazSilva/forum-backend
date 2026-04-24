package com.ferraz.forumbackend.session;

import com.ferraz.forumbackend.infra.exception.UnauthorizedException;
import com.ferraz.forumbackend.infra.service.CookieService;
import com.ferraz.forumbackend.infra.service.UserContext;
import com.ferraz.forumbackend.session.dto.LoginDTO;
import com.ferraz.forumbackend.session.dto.SessionDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final CookieService cookieService;


    @PostMapping
    public ResponseEntity<SessionDTO> getSession(@Valid @RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        SessionEntity sessionEntity = sessionService.getSession(loginDTO);

        Cookie cookie = cookieService.createSessionCookie(sessionEntity);
        response.addCookie(cookie);

        SessionDTO sessionDTO = new SessionDTO(sessionEntity.getToken());
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(sessionDTO);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteSession(HttpServletRequest request, HttpServletResponse response) {
        if (UserContext.isAnonymousSession()) {
            throw new UnauthorizedException();
        }
        sessionService.inactivate(UserContext.getSession());
        Cookie sessionCookie = cookieService.extractSessionCookie(request);
        response.addCookie(cookieService.createExpiredSessionCookie(sessionCookie.getValue()));
        return ResponseEntity.noContent().build();
    }

}
