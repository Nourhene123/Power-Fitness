package com.powerfitness.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrderStatusRequest(@NotBlank String status) {}
