package com.powerfitness.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank String name,
        @NotBlank String category,
        String description,
        @NotNull @DecimalMin(value = "0.001") BigDecimal price,
        String imageUrl,
        @NotNull @Min(0) Integer stockQty) {}
