package com.powerfitness.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCoachRequestDto(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 20) String phone,
        @Pattern(regexp = "beginner|intermediate|advanced") String experience,
        @NotBlank @Size(max = 5000) String goals) {}
