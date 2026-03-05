package com.ferraz.forumbackend.user.validator;

import com.ferraz.forumbackend.user.UserEntity;
import com.ferraz.forumbackend.user.UserRepository;
import com.ferraz.forumbackend.user.dto.NewUserDTO;
import com.ferraz.forumbackend.user.dto.UpdateUserDTO;
import com.ferraz.forumbackend.user.exception.NonUniqueEmailException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;

@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements InsertUserValidator, UpdateUserValidator {

    private final UserRepository userRepository;

    @Override
    public void validate(NewUserDTO newUserDTO) {
        Optional<UserEntity> user = userRepository.findFirstByEmail(newUserDTO.email());

        if (user.isPresent()) {
            throw new NonUniqueEmailException(newUserDTO.email());
        }
    }

    @Override
    public void validate(String username, UpdateUserDTO updateUserDTO) {
        if (!hasText(updateUserDTO.email())) return;

        Optional<UserEntity> user = userRepository.findFirstByEmail(updateUserDTO.email());

        if (user.isPresent() && !username.equals(user.get().getUsername())) {
            throw new NonUniqueEmailException(updateUserDTO.email());
        }
    }
}
