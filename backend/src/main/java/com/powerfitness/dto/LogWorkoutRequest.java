package com.powerfitness.dto;

import java.time.LocalDate;

public record LogWorkoutRequest(
        Integer dayIndex,
        LocalDate sessionDate,
        Integer durationMin,
        Integer rpe,
        String note) {}
