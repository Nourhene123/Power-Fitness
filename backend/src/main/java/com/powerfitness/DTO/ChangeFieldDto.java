package com.powerfitness.DTO;

import jakarta.validation.constraints.NotBlank;

public record ChangeFieldDto(@NotBlank String field, @NotBlank String label) {}
