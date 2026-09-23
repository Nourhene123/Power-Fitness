package com.powerfitness.dto;

import jakarta.validation.constraints.Pattern;

public record ToggleHabitRequest(@Pattern(regexp = "water|workout|nutrition|sleep") String habit) {}
