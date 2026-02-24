package com.ferraz.forumbackend.user.validator;

import com.ferraz.forumbackend.user.UserEntity;
import com.ferraz.forumbackend.user.UserRepository;
import com.ferraz.forumbackend.user.dto.NewUserDTO;
import com.ferraz.forumbackend.user.exception.NonUniqueUsernameException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UniqueUsernameValidator implements InsertUserValidator {

    private final UserRepository userRepository;

    @Override
    public void validate(NewUserDTO newUserDTO) {
        Optional<UserEntity> users = userRepository.findByUsername(newUserDTO.username());

        if (users.isPresent()) {
            throw new NonUniqueUsernameException(newUserDTO.email());
        }
    }
}
