package com.powerfitness.dto;

import java.math.BigDecimal;

public record ProductDto(
        Long id,
        String name,
        String category,
        String description,
        BigDecimal price,
        String imageUrl,
        int stockQty,
        boolean active) {}
