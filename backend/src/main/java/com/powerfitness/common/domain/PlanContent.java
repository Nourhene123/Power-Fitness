package com.powerfitness.common.domain;

import java.util.List;


public record PlanContent(
        Metrics metrics,
        List<Phase> phases,
        List<TrainingDay> weeklySplit,
        List<Meal> mealPlan,
        Guidelines guidelines,
        List<String> coachConstraints,
        AnalysisSummary analysisSummary,
        List<String> changelog) {

    public record Metrics(
            double bmi,
            String bmiCategory,
            int bmr,
            int tdee,
            int targetCalories,
            int proteinG,
            int carbsG,
            int fatsG,
            double waterLiters,
            String roadmapTitle) {}

    public record Phase(
            int phase,
            String title,
            String weeks,
            String focus,
            String intensity,
            List<String> targets) {}

    public record TrainingDay(
            String day,
            String title,
            String duration,
            List<PlannedExercise> exercises) {}

    public record PlannedExercise(String name, String sets, String reps, String rest, String notes) {}

    public record Meal(
            int number,
            String name,
            int calories,
            int proteinG,
            int carbsG,
            int fatsG,
            String suggestion) {}

    public record Guidelines(
            String hydration,
            String sleep,
            List<String> supplements,
            String consistencyRule) {}

    public record AnalysisSummary(
            String feasibility,
            List<String> topLimiters,
            List<String> focusHabits) {}
}
