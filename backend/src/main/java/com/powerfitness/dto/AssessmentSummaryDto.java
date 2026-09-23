package com.powerfitness.dto;

import java.util.List;

public record AssessmentSummaryDto(
        Integer age,
        String gender,
        String sexAtBirth,
        Double heightCm,
        Double weightKg,
        Double targetWeightKg,
        String primaryGoal,
        String experienceLevel,
        Integer workoutFrequency,
        Integer sessionDurationMin,
        String workoutLocation,
        String occupationType,
        Integer sleepHours,
        Integer sleepQuality,
        Integer stressLevel,
        Integer energyLevel,
        List<String> equipment,
        String dietPreference,
        String allergies,
        String dislikedFoods,
        String lovedExercises,
        String hatedExercises,
        String coachingTonePref,
        List<InjuryDto> injuries,
        String medicalConditions) {

    public record InjuryDto(String bodyPart, boolean stillPainful, String avoid) {}
}
