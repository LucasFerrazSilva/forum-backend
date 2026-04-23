package com.ferraz.forumbackend.user;

import com.ferraz.forumbackend.activationtoken.ActivationTokenEntity;
import com.ferraz.forumbackend.activationtoken.ActivationTokenService;
import com.ferraz.forumbackend.infra.CookieService;
import com.ferraz.forumbackend.infra.SessionHolder;
import com.ferraz.forumbackend.infra.exception.UnauthorizedException;
import com.ferraz.forumbackend.session.SessionEntity;
import com.ferraz.forumbackend.user.dto.NewUserDTO;
import com.ferraz.forumbackend.user.dto.UpdateUserDTO;
import com.ferraz.forumbackend.user.dto.UserDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final CookieService cookieService;
    private final UserService userService;
    private final ActivationTokenService activationTokenService;
    private final SessionHolder sessionHolder;

    @PostMapping
    public ResponseEntity<UserDTO> insert(@Valid @RequestBody NewUserDTO newUserDTO) {
        UserEntity userEntity = userService.insert(newUserDTO);
        ActivationTokenEntity activationTokenEntity = activationTokenService.create(userEntity);
        activationTokenService.sendActivationEmail(userEntity, activationTokenEntity);

        UserDTO userDTO = UserMapper.toDTO(userEntity);
        return  ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDTO> findByUsername(@PathVariable String username) {
        UserEntity userEntity = userService.findByUsername(username);
        UserDTO userDTO = UserMapper.toDTO(userEntity);
        return  ResponseEntity.status(HttpStatus.OK).body(userDTO);
    }

    @PatchMapping("/{username}")
    public ResponseEntity<UserDTO> update(@PathVariable String username, @RequestBody UpdateUserDTO updateUserDTO) {
        UserEntity userEntity = userService.update(username, updateUserDTO);
        UserDTO userDTO = UserMapper.toDTO(userEntity);
        return  ResponseEntity.status(HttpStatus.OK).body(userDTO);
    }

    @GetMapping
    public ResponseEntity<UserDTO> findBySessionId(HttpServletResponse response) {
        if (sessionHolder.isAnonymousSession()) {
            throw new UnauthorizedException();
        }

        SessionEntity session = sessionHolder.getSession();
        Cookie sessionCookie = cookieService.createSessionCookie(session);
        response.addCookie(sessionCookie);

        UserDTO userDTO = UserMapper.toDTO(sessionHolder.getUser());

        return  ResponseEntity.status(HttpStatus.OK).body(userDTO);
    }

}
