package com.ferraz.forumbackend.status.entity;

import java.time.LocalDateTime;
import java.util.List;

public record StatusDTO(
        LocalDateTime updatedAt,
        DatabaseInfo database
) {
}
