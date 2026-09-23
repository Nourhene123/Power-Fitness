package com.powerfitness.dto;

import java.time.Instant;

public record NotificationDto(
        Long id,
        String type,
        String title,
        String body,
        String link,
        boolean read,
        Instant createdAt) {}
