package com.ferraz.forumbackend.integration.status.entity;

import java.time.LocalDateTime;

public record StatusDTO(
        String appVersion,
        LocalDateTime updatedAt,
        DatabaseInfo database
) {
}
