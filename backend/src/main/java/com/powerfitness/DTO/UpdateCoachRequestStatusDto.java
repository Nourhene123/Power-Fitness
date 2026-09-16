package com.powerfitness.DTO;

import jakarta.validation.constraints.Pattern;

public record UpdateCoachRequestStatusDto(
        @Pattern(regexp = "pending|contacted|completed") String status) {}
