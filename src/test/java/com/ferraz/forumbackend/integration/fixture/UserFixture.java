package com.ferraz.forumbackend.integration.fixture;

import com.ferraz.forumbackend.session.dto.LoginDTO;
import com.ferraz.forumbackend.user.UserEntity;
import com.ferraz.forumbackend.user.UserService;
import com.ferraz.forumbackend.user.dto.NewUserDTO;
import com.ferraz.forumbackend.user.dto.UpdateUserDTO;
import jakarta.transaction.Transactional;
import lombok.Getter;
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
        NewUserDTO newUserDTO = newUserDTOBuilder.build();
        UserEntity userEntity = userService.insert(newUserDTO);
        if (newUserDTOBuilder.shouldActivate()) {
            userService.activate(userEntity);
        }
        if (newUserDTOBuilder.getFeatures() != null) {
            userService.addFeatures(userEntity, newUserDTOBuilder.getFeatures());
        }
        return userEntity;
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
        private boolean activated = true;
        @Getter
        private String[] features = null;


        public boolean shouldActivate() {
            return activated;
        }

        public NewUserDTOBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public NewUserDTOBuilder withFeatures(String[] features) {
            this.features = features;
            return this;
        }

        public NewUserDTOBuilder withLoginDTO(LoginDTO loginDTO) {
            this.email = loginDTO.email();
            this.password = loginDTO.password();
            return this;
        }

        public NewUserDTOBuilder withActivated(boolean activated) {
            this.activated = activated;
            return this;
        }

        public NewUserDTOBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public NewUserDTOBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public NewUserDTO build() {
            return new NewUserDTO(username, email, password);
        }

    }


    public UpdateUserDTO updateUserDTO() {
        return updateUserDTO(b -> {});
    }

    public UpdateUserDTO updateUserDTO(Consumer<UpdateUserDTOBuilder> customizer) {
        UpdateUserDTOBuilder builder = new UpdateUserDTOBuilder();
        customizer.accept(builder);
        return builder.build();
    }

    public class UpdateUserDTOBuilder {

        private String username = null;
        private String email = null;
        private String password = null;

        public UpdateUserDTOBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UpdateUserDTOBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UpdateUserDTOBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UpdateUserDTO build() {
            return new UpdateUserDTO(username, email, password);
        }

    }

}


