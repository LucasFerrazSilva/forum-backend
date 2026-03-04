package com.ferraz.forumbackend.user;

import com.ferraz.forumbackend.user.dto.NewUserDTO;
import com.ferraz.forumbackend.user.dto.UserDTO;

import java.time.LocalDateTime;

public class UserMapper {

    public static UserEntity toEntity(NewUserDTO newUserDTO) {
        UserEntity userEntity = new UserEntity();

        userEntity.setUsername(newUserDTO.username());
        userEntity.setEmail(newUserDTO.email());
        userEntity.setPassword(newUserDTO.password());
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setUpdatedAt(LocalDateTime.now());

        return userEntity;
    }

    public static UserDTO toDTO(UserEntity userEntity) {
        return new UserDTO(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                userEntity.getCreatedAt(),
                userEntity.getUpdatedAt()
        );
    }

}
