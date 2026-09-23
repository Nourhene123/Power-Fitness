package com.powerfitness.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "roadmaps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Roadmap extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "assessment_id")
    private Long assessmentId;

    @Column(name = "analysis_id")
    private Long analysisId;

    @Column(name = "program_version_id")
    private Long programVersionId;

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

    @Column(name = "water_liters", nullable = false, precision = 3, scale = 1)
    private BigDecimal waterLiters;

    @Column(name = "roadmap_title", nullable = false, length = 150)
    private String roadmapTitle;

    /** Plan payload mirrored from the active version's content (phases, split, meals, guidelines). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "roadmap_data", nullable = false, columnDefinition = "jsonb")
    private String roadmapData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private RoadmapStatus status = RoadmapStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "coach_status", nullable = false, length = 16)
    @Builder.Default
    private CoachStatus coachStatus = CoachStatus.PENDING_REVIEW;

    @Column(name = "coach_id")
    private Long coachId;

    @Column(name = "coach_notes", columnDefinition = "text")
    private String coachNotes;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;
}
