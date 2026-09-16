package com.powerfitness.DTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDto(
        Long id,
        String status,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal total,
        String recipientName,
        String phone,
        String addressLine,
        String city,
        String note,
        Instant createdAt,
        List<OrderItemDto> items) {}
