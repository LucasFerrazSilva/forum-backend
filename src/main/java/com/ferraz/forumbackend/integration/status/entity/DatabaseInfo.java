package com.ferraz.forumbackend.integration.status.entity;

public record DatabaseInfo(
        String version,
        Integer maxConnections,
        Integer openedConnections
) {}
