package com.ferraz.forumbackend.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDTO(
        UUID id,
        String username,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String[] features
) {
}
