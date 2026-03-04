package com.ferraz.forumbackend.user;

import com.ferraz.forumbackend.infra.exception.NotFoundException;
import com.ferraz.forumbackend.user.dto.NewUserDTO;
import com.ferraz.forumbackend.user.validator.InsertUserValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final List<InsertUserValidator> validators;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserEntity insert(NewUserDTO newUserDTO) {
        validators.forEach(validator -> validator.validate(newUserDTO));
        UserEntity userEntity = UserMapper.toEntity(newUserDTO, passwordEncoder);
        return userRepository.save(userEntity);
    }

    public UserEntity findByUsername(String username) {
        return userRepository.findFirstByUsername(username.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Nenhum usuário encontrado para o username " + username));
    }
}
