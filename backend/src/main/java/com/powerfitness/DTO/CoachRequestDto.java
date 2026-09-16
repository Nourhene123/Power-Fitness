package com.powerfitness.DTO;

import java.time.Instant;

public record CoachRequestDto(
        Long id,
        String name,
        String email,
        String phone,
        String experience,
        String goals,
        String status,
        Instant createdAt) {}
