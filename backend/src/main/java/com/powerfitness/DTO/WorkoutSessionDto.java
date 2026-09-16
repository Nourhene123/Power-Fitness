package com.powerfitness.DTO;

import java.time.LocalDate;

public record WorkoutSessionDto(
        Long id,
        Integer dayIndex,
        String dayLabel,
        LocalDate sessionDate,
        Integer durationMin,
        Integer rpe,
        String note) {}
