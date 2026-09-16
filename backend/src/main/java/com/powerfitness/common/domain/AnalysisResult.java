package com.powerfitness.common.domain;

import java.util.List;

public record AnalysisResult(
        String generatedAt,
        Metabolic metabolic,
        Feasibility feasibility,
        List<Strength> strengths,
        List<Limiter> limiters,
        Contraindications contraindications,
        SuggestedStart suggestedStart,
        List<DataQualityFlag> dataQualityFlags) {

    public record Metabolic(
            double bmi,
            String bmiCategory,
            int bmr,
            int tdee,
            double activityMultiplier,
            int targetCalories,
            int proteinG,
            int carbsG,
            int fatsG,
            String bodyfatBand,
            String roadmapTitle,
            double waterLiters) {}

    public record Feasibility(
            String verdict,        // realistic | aggressive | unrealistic | maintenance
            String direction,      // loss | gain | maintain
            double deltaKg,
            List<Double> safeWeeklyPctRange,
            Double weeklyRatePct,
            Integer weeksAvailable,
            int projectedWeeks,
            String message) {}

    public record Strength(String label, String detail) {}

    public record Limiter(String severity, String label, String action) {}

    public record Contraindications(
            List<String> excludedPatterns,
            String equipmentMode,      // gym | home | bands | bodyweight
            int maxExercisesPerSession,
            boolean noHighImpact,
            List<String> notes) {}

    public record SuggestedStart(
            String split,
            int weeklySetsPerMuscle,
            int calorieTarget,
            int proteinG,
            int sessionCap,
            List<String> focusHabits) {}

    public record DataQualityFlag(String type, String message) {}
}
