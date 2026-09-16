package com.powerfitness.common.domain;

import java.time.LocalDate;
import java.util.List;

public record AssessmentInput(
        int age,
        LocalDate dateOfBirth,
        String gender,            // male | female | other
        String sexAtBirth,       // male | female  (drives BMR)
        double heightCm,
        double weightKg,
        double targetWeightKg,
        LocalDate targetDate,
        int workoutFrequency,
        int sessionDurationMin,
        String workoutLocation,  // gym | home | calisthenics | outdoor
        String experienceLevel,  // beginner | intermediate | advanced
        Double yearsTraining,
        String activityLevel,    // sedentary | light | moderate | very_active
        Integer dailySteps,
        String occupationType,   // desk | on_feet | physical | shift
        String primaryGoal,      // fat_loss | muscle_gain | weight_gain | athletic_performance | general_fitness
        String focusAreas,
        int mealsPerDay,
        String dietPreference,   // balanced | high_protein | vegetarian | vegan | keto | mediterranean
        Integer sleepHours,
        Integer sleepQuality,
        Integer stressLevel,
        Integer energyLevel,
        Integer caffeineMg,
        Integer alcoholUnitsWeek,
        String cookingAbility,   // none | basic | confident
        Integer eatingOutPerWeek,
        String budgetLevel,      // tight | moderate | flexible
        boolean pregnant,
        String medicalConditions,
        Double strengthSquat,
        Double strengthBench,
        Double strengthDeadlift,
        Double waistCm,
        List<String> equipment,  // full_gym | gym | dumbbells | barbell | bands | machines | bodyweight | calisthenics
        List<Injury> injuries) {

    public record Injury(String bodyPart, boolean stillPainful, String avoid) {}
}
