package com.powerfitness.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * The structured "coach analysis" produced from an {@link Assessment}: metabolic numbers,
 * goal feasibility, ranked limiters, hard contraindications, suggested starting point.
 * Filterable scalars are columns; the full structure is {@link #analysis} (jsonb).
 */
@Entity
@Table(name = "assessment_analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentAnalysis extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal bmi;

    @Column(name = "bmi_category", nullable = false, length = 50)
    private String bmiCategory;

    @Column(nullable = false)
    private Integer bmr;

    @Column(nullable = false)
    private Integer tdee;

    @Column(name = "target_calories", nullable = false)
    private Integer targetCalories;

    @Column(name = "protein_g", nullable = false)
    private Integer proteinG;

    @Column(name = "carbs_g", nullable = false)
    private Integer carbsG;

    @Column(name = "fats_g", nullable = false)
    private Integer fatsG;

    @Column(name = "bodyfat_band", length = 30)
    private String bodyfatBand;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_feasibility", nullable = false, length = 14)
    @Builder.Default
    private GoalFeasibility goalFeasibility = GoalFeasibility.REALISTIC;

    @Column(name = "weekly_rate_pct", precision = 4, scale = 2)
    private BigDecimal weeklyRatePct;

    @Column(name = "projected_weeks")
    private Integer projectedWeeks;

    @Column(name = "risk_count", nullable = false)
    @Builder.Default
    private Integer riskCount = 0;

    @Column(name = "data_quality_flag_count", nullable = false)
    @Builder.Default
    private Integer dataQualityFlagCount = 0;

    /** Full analyzer output JSON: metabolic{}, feasibility{}, strengths[], limiters[], contraindications{}, ... */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String analysis;
}
