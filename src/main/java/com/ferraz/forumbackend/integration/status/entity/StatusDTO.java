package com.ferraz.forumbackend.integration.status.entity;

import java.time.LocalDateTime;

public record StatusDTO(
        LocalDateTime updatedAt,
        DatabaseInfo database
) {
}
