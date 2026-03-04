package com.ferraz.forumbackend.status.entity;

public record DatabaseInfo(
        String version,
        Integer maxConnections,
        Integer openedConnections
) {}
