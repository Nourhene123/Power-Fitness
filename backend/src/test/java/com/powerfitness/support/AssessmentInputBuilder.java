package com.powerfitness.support;

import com.powerfitness.common.domain.AssessmentInput;
import com.powerfitness.common.domain.AssessmentInput.Injury;
import java.time.LocalDate;
import java.util.List;

/** Minimal fluent builder over the 30-field {@link AssessmentInput} record, for test readability. */
public final class AssessmentInputBuilder {

    private int age = 28;
    private LocalDate dateOfBirth = null;
    private String gender = "male";
    private String sexAtBirth = "male";
    private double heightCm = 175;
    private double weightKg = 75;
    private double targetWeightKg = 75;
    private LocalDate targetDate = null;
    private int workoutFrequency = 4;
    private int sessionDurationMin = 60;
    private String workoutLocation = "gym";
    private String experienceLevel = "beginner";
    private Double yearsTraining = null;
    private String activityLevel = "moderate";
    private Integer dailySteps = null;
    private String occupationType = "desk";
    private String primaryGoal = "general_fitness";
    private String focusAreas = null;
    private int mealsPerDay = 3;
    private String dietPreference = "balanced";
    private Integer sleepHours = 7;
    private Integer sleepQuality = 4;
    private Integer stressLevel = 4;
    private Integer energyLevel = 6;
    private Integer caffeineMg = null;
    private Integer alcoholUnitsWeek = null;
    private String cookingAbility = "basic";
    private Integer eatingOutPerWeek = 2;
    private String budgetLevel = "moderate";
    private boolean pregnant = false;
    private String medicalConditions = null;
    private Double strengthSquat = null;
    private Double strengthBench = null;
    private Double strengthDeadlift = null;
    private Double waistCm = null;
    private List<String> equipment = List.of("full_gym");
    private List<Injury> injuries = List.of();

    public static AssessmentInputBuilder builder() {
        return new AssessmentInputBuilder();
    }

    public AssessmentInputBuilder age(int v) { this.age = v; return this; }
    public AssessmentInputBuilder sexAtBirth(String v) { this.sexAtBirth = v; return this; }
    public AssessmentInputBuilder heightCm(double v) { this.heightCm = v; return this; }
    public AssessmentInputBuilder weightKg(double v) { this.weightKg = v; return this; }
    public AssessmentInputBuilder targetWeightKg(double v) { this.targetWeightKg = v; return this; }
    public AssessmentInputBuilder targetDate(LocalDate v) { this.targetDate = v; return this; }
    public AssessmentInputBuilder workoutFrequency(int v) { this.workoutFrequency = v; return this; }
    public AssessmentInputBuilder sessionDurationMin(int v) { this.sessionDurationMin = v; return this; }
    public AssessmentInputBuilder workoutLocation(String v) { this.workoutLocation = v; return this; }
    public AssessmentInputBuilder experienceLevel(String v) { this.experienceLevel = v; return this; }
    public AssessmentInputBuilder activityLevel(String v) { this.activityLevel = v; return this; }
    public AssessmentInputBuilder dailySteps(Integer v) { this.dailySteps = v; return this; }
    public AssessmentInputBuilder occupationType(String v) { this.occupationType = v; return this; }
    public AssessmentInputBuilder primaryGoal(String v) { this.primaryGoal = v; return this; }
    public AssessmentInputBuilder mealsPerDay(int v) { this.mealsPerDay = v; return this; }
    public AssessmentInputBuilder dietPreference(String v) { this.dietPreference = v; return this; }
    public AssessmentInputBuilder sleepHours(Integer v) { this.sleepHours = v; return this; }
    public AssessmentInputBuilder eatingOutPerWeek(Integer v) { this.eatingOutPerWeek = v; return this; }
    public AssessmentInputBuilder pregnant(boolean v) { this.pregnant = v; return this; }
    public AssessmentInputBuilder equipment(List<String> v) { this.equipment = v; return this; }
    public AssessmentInputBuilder injuries(List<Injury> v) { this.injuries = v; return this; }

    public AssessmentInput build() {
        return new AssessmentInput(
                age, dateOfBirth, gender, sexAtBirth, heightCm, weightKg, targetWeightKg, targetDate,
                workoutFrequency, sessionDurationMin, workoutLocation, experienceLevel, yearsTraining,
                activityLevel, dailySteps, occupationType, primaryGoal, focusAreas, mealsPerDay,
                dietPreference, sleepHours, sleepQuality, stressLevel, energyLevel, caffeineMg,
                alcoholUnitsWeek, cookingAbility, eatingOutPerWeek, budgetLevel, pregnant,
                medicalConditions, strengthSquat, strengthBench, strengthDeadlift, waistCm,
                equipment, injuries);
    }
}
