package com.ferraz.forumbackend.integration.fixture;

import com.ferraz.forumbackend.user.UserEntity;
import com.ferraz.forumbackend.user.UserService;
import com.ferraz.forumbackend.user.dto.NewUserDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class UserFixture {

    private final UserService userService;

    @Transactional
    public UserEntity user() {
        return user(b -> {});
    }

    @Transactional
    public UserEntity admin() {
        return user(b -> {});
    }

    @Transactional
    public UserEntity user(Consumer<NewUserDTOBuilder> customizer) {
        NewUserDTOBuilder newUserDTOBuilder = new NewUserDTOBuilder();
        customizer.accept(newUserDTOBuilder);
        NewUserDTO user = newUserDTOBuilder.build();
        return userService.insert(user);
    }

    public NewUserDTO newUserDTO() {
        return newUserDTO(b -> {});
    }

    public NewUserDTO newUserDTO(Consumer<NewUserDTOBuilder> customizer) {
        NewUserDTOBuilder builder = new NewUserDTOBuilder();
        customizer.accept(builder);
        return builder.build();
    }


    public class NewUserDTOBuilder {

        private final Integer rand = new Random().nextInt(100000);
        private String username = rand + "username";
        private String email = rand + "mail@domain.com";
        private String password = "password";

        public NewUserDTOBuilder username(String username) {
            this.username = username;
            return this;
        }

        public NewUserDTOBuilder email(String email) {
            this.email = email;
            return this;
        }

        public NewUserDTOBuilder password(String password) {
            this.password = password;
            return this;
        }

        public NewUserDTO build() {
            return new NewUserDTO(username, email, password);
        }

    }

}


