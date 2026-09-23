package com.powerfitness.dto;

public record UserDto(
        Long id,
        String name,
        String email,
        String role,
        String avatar,
        boolean hasAssessment) {}
