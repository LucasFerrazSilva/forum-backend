package com.ferraz.forumbackend.user;

import com.ferraz.forumbackend.infra.exception.InvalidField;
import com.ferraz.forumbackend.infra.exception.ValidationException;
import com.ferraz.forumbackend.user.dto.NewUserDTO;
import com.ferraz.forumbackend.user.validator.InsertUserValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final List<InsertUserValidator> validators;

    @Transactional
    public UserEntity insert(NewUserDTO newUserDTO) {
        validators.forEach(validator -> validator.validate(newUserDTO));
        UserEntity userEntity = UserMapper.toEntity(newUserDTO);
        return userRepository.save(userEntity);
    }

    public UserEntity findByUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new ValidationException(new InvalidField("username", "O username não pode estar vazio"));
        }

        return userRepository.findFirstByUsername(username.toLowerCase()).orElseThrow();
    }
}
