package com.powerfitness.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PlaceOrderRequest(
        @NotEmpty List<@Valid OrderItemLineRequest> items,
        @NotBlank String recipientName,
        @NotBlank String phone,
        @NotBlank String addressLine,
        @NotBlank String city,
        String note) {}
