package com.powerfitness.DTO;


public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserDto user) {}
