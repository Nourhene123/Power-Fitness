package com.powerfitness.dto;


public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserDto user) {}
