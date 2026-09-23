package com.powerfitness.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemLineRequest(
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity,
        String size) {}
