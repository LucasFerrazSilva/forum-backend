package com.ferraz.forumbackend.user.validator;

import com.ferraz.forumbackend.user.UserEntity;
import com.ferraz.forumbackend.user.UserRepository;
import com.ferraz.forumbackend.user.dto.UpdateUserDTO;
import com.ferraz.forumbackend.user.exception.UsernameNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsernameExistsValidator implements UpdateUserValidator {

    private final UserRepository userRepository;

    @Override
    public void validate(String username, UpdateUserDTO updateUserDTO) {
        Optional<UserEntity> user = userRepository.findFirstByUsername(username.toLowerCase());

        if (user.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
    }
}
