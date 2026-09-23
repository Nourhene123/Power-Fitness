package com.powerfitness.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;


public record SubmitAssessmentRequest(        String fullName,
        @NotNull LocalDate dateOfBirth,
        @Pattern(regexp = "male|female") String sexAtBirth,
        @Pattern(regexp = "male|female|other") String gender,
        String country,
        String timezone,
        @Pattern(regexp = "metric|imperial") String units,
        @Pattern(regexp = "desk|on_feet|physical|shift") String occupationType,
        String motivation,

        // --- body & biometrics ---
        @DecimalMin("100") @DecimalMax("250") double heightCm,
        @DecimalMin("30") @DecimalMax("300") double weightKg,
        @DecimalMin("30") @DecimalMax("300") double targetWeightKg,
        Double waistCm, Double hipCm, Double chestCm, Double armCm, Double thighCm, Double neckCm,
        Double bodyfatPct,
        String weightTrend,

        // --- training history & capacity ---
        @Pattern(regexp = "beginner|intermediate|advanced") String experienceLevel,
        Double yearsTraining,
        String currentProgram,
        Double strengthSquat, Double strengthBench, Double strengthDeadlift, Integer strengthPullups,
        int workoutFrequency,
        int sessionDurationMin,
        List<String> trainingDays,
        List<String> equipment,
        String cardioBaseline,
        Integer dailySteps,
        @Pattern(regexp = "sedentary|light|moderate|very_active") String activityLevel,

        // --- goals & focus ---
        @Pattern(regexp = "fat_loss|muscle_gain|weight_gain|athletic_performance|general_fitness") String primaryGoal,
        String secondaryGoal,
        LocalDate targetDate,
        String targetEvent,
        List<String> priorityMuscles,
        String priorityLifts,
        String successDefinition,

        // --- nutrition & lifestyle ---
        int mealsPerDay,
        Integer eatingOutPerWeek,
        @Pattern(regexp = "balanced|high_protein|vegetarian|vegan|keto|mediterranean") String dietPreference,
        @Pattern(regexp = "none|basic|confident") String cookingAbility,
        @Pattern(regexp = "tight|moderate|flexible") String budgetLevel,
        String allergies,
        String dislikedFoods,
        Double waterIntakeLiters,
        Integer alcoholUnitsWeek,
        Integer caffeineMg,
        String supplements,

        // --- recovery & health ---
        Integer sleepHours,
        Integer sleepQuality,
        @Pattern(regexp = "groggy|ok|fresh") String wakeFeeling,
        Integer stressLevel,
        Integer energyLevel,
        boolean hasInjuries,
        List<InjuryDto> injuries,
        String medicalConditions,
        String medications,
        boolean isPregnant,
        boolean medicalAck,

        // --- preferences ---
        String lovedExercises,
        String hatedExercises,
        @Pattern(regexp = "strength|bodybuilding|athletic|minimal") String trainingStylePref,
        @Pattern(regexp = "gentle|direct|tough") String coachingTonePref,
        @Pattern(regexp = "weekly|biweekly|monthly") String checkinFrequency,
        @Pattern(regexp = "email|whatsapp|in_app") String contactChannel) {

    public record InjuryDto(String bodyPart, String description, boolean stillPainful, String avoid) {}

    @AssertTrue(message = "You must acknowledge the medical disclaimer to continue")
    public boolean isMedicalAckAccepted() {
        return medicalAck;
    }
}
