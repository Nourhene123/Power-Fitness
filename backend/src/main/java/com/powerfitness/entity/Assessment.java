package com.powerfitness.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assessment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "is_latest", nullable = false)
    @Builder.Default
    private boolean latest = true;

    // ---- identity / metabolic inputs ----
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private Integer age;

    /** {@code male} | {@code female} | {@code other}. */
    @Column(length = 10)
    private String gender;

    /** {@code male} | {@code female} — used for the BMR formula. */
    @Column(name = "sex_at_birth", length = 6)
    private String sexAtBirth;

    @Column(name = "height_cm", nullable = false, precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "target_weight_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal targetWeightKg;

    @Column(name = "target_date")
    private LocalDate targetDate;

    // ---- training capacity ----
    @Column(name = "workout_frequency", nullable = false)
    @Builder.Default
    private Integer workoutFrequency = 3;

    @Column(name = "session_duration_min", nullable = false)
    @Builder.Default
    private Integer sessionDurationMin = 60;

    /** {@code gym} | {@code home} | {@code calisthenics} | {@code outdoor}. */
    @Column(name = "workout_location", nullable = false, length = 12)
    @Builder.Default
    private String workoutLocation = "gym";

    /** {@code beginner} | {@code intermediate} | {@code advanced}. */
    @Column(name = "experience_level", nullable = false, length = 12)
    @Builder.Default
    private String experienceLevel = "beginner";

    @Column(name = "years_training", precision = 4, scale = 1)
    private BigDecimal yearsTraining;

    /** {@code sedentary} | {@code light} | {@code moderate} | {@code very_active}. */
    @Column(name = "activity_level", nullable = false, length = 12)
    @Builder.Default
    private String activityLevel = "moderate";

    @Column(name = "daily_steps")
    private Integer dailySteps;

    /** {@code desk} | {@code on_feet} | {@code physical} | {@code shift}. */
    @Column(name = "occupation_type", nullable = false, length = 10)
    @Builder.Default
    private String occupationType = "desk";

    // ---- goals / nutrition ----
    /** {@code fat_loss} | {@code muscle_gain} | {@code weight_gain} | {@code athletic_performance} | {@code general_fitness}. */
    @Column(name = "primary_goal", nullable = false, length = 24)
    private String primaryGoal;

    @Column(name = "secondary_goal", length = 50)
    private String secondaryGoal;

    @Column(name = "focus_areas", length = 255)
    @Builder.Default
    private String focusAreas = "full_body";

    @Column(name = "meals_per_day", nullable = false)
    @Builder.Default
    private Integer mealsPerDay = 3;

    /** {@code balanced} | {@code high_protein} | {@code vegetarian} | {@code vegan} | {@code keto} | {@code mediterranean}. */
    @Column(name = "diet_preference", nullable = false, length = 16)
    @Builder.Default
    private String dietPreference = "high_protein";

    @Column(name = "water_intake_liters", precision = 3, scale = 1)
    @Builder.Default
    private BigDecimal waterIntakeLiters = BigDecimal.valueOf(2.5);

    /** {@code none} | {@code basic} | {@code confident}. */
    @Column(name = "cooking_ability", length = 10)
    private String cookingAbility;

    @Column(name = "eating_out_per_week")
    private Integer eatingOutPerWeek;

    /** {@code tight} | {@code moderate} | {@code flexible}. */
    @Column(name = "budget_level", length = 10)
    private String budgetLevel;

    @Column(name = "caffeine_mg")
    private Integer caffeineMg;

    @Column(name = "alcohol_units_week")
    private Integer alcoholUnitsWeek;

    // ---- recovery / health ----
    @Column(name = "sleep_hours")
    @Builder.Default
    private Integer sleepHours = 7;

    @Column(name = "sleep_quality")
    private Integer sleepQuality;

    /** {@code groggy} | {@code ok} | {@code fresh}. */
    @Column(name = "wake_feeling", length = 8)
    private String wakeFeeling;

    @Column(name = "stress_level")
    private Integer stressLevel;

    @Column(name = "energy_level")
    private Integer energyLevel;

    @Column(name = "has_injuries", nullable = false)
    @Builder.Default
    private boolean hasInjuries = false;

    @Column(name = "is_pregnant", nullable = false)
    @Builder.Default
    private boolean pregnant = false;

    @Column(name = "medical_conditions", columnDefinition = "text")
    private String medicalConditions;

    @Column(columnDefinition = "text")
    private String medications;

    @Column(name = "medical_ack", nullable = false)
    @Builder.Default
    private boolean medicalAck = false;

    // ---- preferences ----
    /** {@code strength} | {@code bodybuilding} | {@code athletic} | {@code minimal}. */
    @Column(name = "training_style_pref", length = 14)
    private String trainingStylePref;

    /** {@code gentle} | {@code direct} | {@code tough}. */
    @Column(name = "coaching_tone_pref", length = 8)
    private String coachingTonePref;

    /** {@code weekly} | {@code biweekly} | {@code monthly}. */
    @Column(name = "checkin_frequency", length = 10)
    private String checkinFrequency;

    /** {@code email} | {@code whatsapp} | {@code in_app}. */
    @Column(name = "contact_channel", length = 10)
    private String contactChannel;

    @Column(nullable = false, length = 8)
    @Builder.Default
    private String units = "metric";

    @Column(length = 80)
    private String country;

    @Column(length = 64)
    private String timezone;

    @Column(columnDefinition = "text")
    private String motivation;

    @Column(name = "success_definition", columnDefinition = "text")
    private String successDefinition;

    /** Full questionnaire payload as JSON (measurements, equipment[], injuries[], strength{}, ...). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private String responses = "{}";
}
