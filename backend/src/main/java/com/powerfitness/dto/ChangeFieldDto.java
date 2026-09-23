package com.powerfitness.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeFieldDto(@NotBlank String field, @NotBlank String label) {}
