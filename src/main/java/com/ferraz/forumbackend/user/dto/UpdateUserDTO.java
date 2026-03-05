package com.ferraz.forumbackend.user.dto;

public record UpdateUserDTO(
        String username,
        String email,
        String password
) {
        @Override
        public String username() {
                return username != null ? username.toLowerCase() : null;
        }
        @Override
        public String email() {
                return email != null ? email.toLowerCase() : null;
        }
}
