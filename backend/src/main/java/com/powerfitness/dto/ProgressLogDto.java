package com.powerfitness.dto;

import java.time.LocalDate;

public record ProgressLogDto(
        Long id,
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
