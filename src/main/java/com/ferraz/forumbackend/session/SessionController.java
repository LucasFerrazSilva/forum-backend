package com.ferraz.forumbackend.session;

import com.ferraz.forumbackend.infra.CookieService;
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
        SessionDTO sessionDTO = new SessionDTO(sessionEntity.getToken());

        Cookie cookie = cookieService.createSessionCookie(sessionEntity);
        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.CREATED.value()).body(sessionDTO);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteSession(HttpServletRequest request, HttpServletResponse response) {
        Cookie sessionCookie = cookieService.getSessionCookie(request);

        SessionEntity sessionEntity = sessionService.inactivate(sessionCookie.getValue());

        Cookie cookie = cookieService.createSessionCookie(sessionEntity);
        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }

}
