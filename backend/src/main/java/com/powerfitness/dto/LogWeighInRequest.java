package com.powerfitness.dto;

import java.time.LocalDate;

public record LogWeighInRequest(
        LocalDate loggedOn,
        Double weightKg,
        Double waistCm,
        Double hipCm,
        Double chestCm,
        Double armCm,
        Double thighCm,
        Double neckCm,
        Integer energy,
        String note) {}
