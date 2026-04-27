package com.ferraz.forumbackend.activationtoken;

import com.ferraz.forumbackend.user.UserEntity;
import com.ferraz.forumbackend.user.UserMapper;
import com.ferraz.forumbackend.user.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/activation-token")
@RequiredArgsConstructor
public class ActivationTokenController {

    private final ActivationTokenService activationTokenService;

    @GetMapping("activate/{id}")
    public ResponseEntity<UserDTO> activate(@PathVariable UUID id) {
        UserEntity userEntity = activationTokenService.activate(id);
        UserDTO userDTO = UserMapper.toDTO(userEntity);
        return ResponseEntity.ok(userDTO);
    }

}
