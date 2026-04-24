package com.ferraz.forumbackend.user;

import com.ferraz.forumbackend.infra.exception.ForbiddenException;
import com.ferraz.forumbackend.infra.service.AuthorizationService;
import com.ferraz.forumbackend.user.dto.NewUserDTO;
import com.ferraz.forumbackend.user.dto.UpdateUserDTO;
import com.ferraz.forumbackend.user.exception.UsernameNotFoundException;
import com.ferraz.forumbackend.user.validator.InsertUserValidator;
import com.ferraz.forumbackend.user.validator.UpdateUserValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final List<InsertUserValidator> insertUserValidators;
    private final List<UpdateUserValidator> updateUserValidators;
    private final PasswordEncoder passwordEncoder;
    private final AuthorizationService authorizationService;

    @Transactional
    public UserEntity insert(NewUserDTO newUserDTO) {
        insertUserValidators.forEach(validator -> validator.validate(newUserDTO));
        UserEntity userEntity = UserMapper.toEntity(newUserDTO, passwordEncoder);
        return userRepository.save(userEntity);
    }

    public UserEntity findByUsername(String username) {
        return userRepository.findFirstByUsername(username.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    @Transactional
    public UserEntity update(String username, UpdateUserDTO updateUserDTO) {
        UserEntity userEntity = this.findByUsername(username);
        updateUserValidators.forEach(validator -> validator.validate(username, updateUserDTO));
        if (hasText(updateUserDTO.username())) {
            userEntity.setUsername(updateUserDTO.username());
        }
        if (hasText(updateUserDTO.email())) {
            userEntity.setEmail(updateUserDTO.email());
        }
        if (hasText(updateUserDTO.password())) {
            userEntity.setPassword(passwordEncoder.encode(updateUserDTO.password()));
        }
        userEntity.setUpdatedAt(LocalDateTime.now());
        userRepository.save(userEntity);
        return userEntity;
    }

    public UserEntity activate(UserEntity user) {
        if (!authorizationService.can(user, "read:activation_token")) {
            throw new ForbiddenException();
        }
        return this.setFeatures(user, new String[]{"create:session", "read:session"});
    }

    @Transactional
    public UserEntity setFeatures(UserEntity user, String[] features) {
        user.setFeatures(features);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

}
