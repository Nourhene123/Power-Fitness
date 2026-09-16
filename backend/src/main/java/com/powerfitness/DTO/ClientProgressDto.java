package com.powerfitness.DTO;

import java.util.List;

public record ClientProgressDto(
        Long userId,
        String name,
        String email,
        Stats stats,
        List<WeightLog> weightLogs,
        List<WorkoutLog> workoutLogs,
        List<HabitDay> habitMatrix,
        boolean atRisk,
        List<String> riskReasons,
        Integer daysSinceLastWeighIn,
        Integer daysSinceLastWorkout,
        int currentStreak) {

    public record Stats(
            int workoutPct,
            int nutritionPct,
            int waterPct,
            double avgSleepHours,
            double weightChangeKg,
            int workoutsCompleted,
            int totalDays) {}

    public record WeightLog(
            String date,
            Double weightKg,
            Double waistCm,
            Double hipCm,
            Double chestCm,
            Integer energy,
            String note) {}

    public record WorkoutLog(String date, String dayLabel, Integer durationMin, Integer rpe, String note) {}

    public record HabitDay(String date, boolean water, boolean workout, boolean nutrition, boolean sleep) {}
}
