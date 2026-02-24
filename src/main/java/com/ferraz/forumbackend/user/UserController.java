package com.ferraz.forumbackend.user;

import com.ferraz.forumbackend.user.dto.NewUserDTO;
import com.ferraz.forumbackend.user.dto.UserDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDTO> insert(@Valid @RequestBody NewUserDTO newUserDTO) {
        UserEntity userEntity = userService.insert(newUserDTO);
        UserDTO userDTO = UserMapper.toDTO(userEntity);
        return  ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

}
