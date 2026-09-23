package com.powerfitness.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        String size,
        BigDecimal lineTotal) {}
