package com.powerfitness.DTO;

import java.time.LocalDate;

public record LogWorkoutRequest(
        Integer dayIndex,
        LocalDate sessionDate,
        Integer durationMin,
        Integer rpe,
        String note) {}
