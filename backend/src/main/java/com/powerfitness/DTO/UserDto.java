package com.powerfitness.DTO;

public record UserDto(
        Long id,
        String name,
        String email,
        String role,
        String avatar,
        boolean hasAssessment) {}
