package com.ferraz.forumbackend.user.validator;

import com.ferraz.forumbackend.user.UserEntity;
import com.ferraz.forumbackend.user.UserRepository;
import com.ferraz.forumbackend.user.dto.NewUserDTO;
import com.ferraz.forumbackend.user.exception.NonUniqueEmailException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements InsertUserValidator {

    private final UserRepository userRepository;

    @Override
    public void validate(NewUserDTO newUserDTO) {
        Optional<UserEntity> users = userRepository.findByEmail(newUserDTO.email());

        if (users.isPresent()) {
            throw new NonUniqueEmailException(newUserDTO.email());
        }
    }
}
