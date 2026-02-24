package com.ferraz.forumbackend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NewUserDTO(
        @NotBlank
        String username,
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password
) {
        @Override
        public String username() {
                return username.toLowerCase();
        }
        @Override
        public String email() {
                return email.toLowerCase();
        }
}
