package com.ferraz.forumbackend.status.entity;

import java.time.LocalDateTime;

public record StatusDTO(
        String appVersion,
        LocalDateTime updatedAt,
        DatabaseInfo database
) {
}
