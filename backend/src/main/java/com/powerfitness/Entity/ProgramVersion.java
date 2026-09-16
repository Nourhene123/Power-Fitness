package com.powerfitness.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One immutable version of a {@link Program}'s plan. Golden rule: the {@code content} of an
 * approved version is never edited and a version is never deleted — post-approval edits
 * create a new version.
 */
@Entity
@Table(name = "program_versions",
        uniqueConstraints = @UniqueConstraint(name = "uq_pv_program_version",
                columnNames = {"program_id", "version_no"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(name = "version_no", nullable = false)
    @Builder.Default
    private Integer versionNo = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProgramStatus status = ProgramStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "created_by", nullable = false, length = 10)
    @Builder.Default
    private VersionAuthor createdBy = VersionAuthor.GENERATOR;

    @Column(name = "based_on_version_id")
    private Long basedOnVersionId;

    /** Raw generator output — the immutable baseline for changelog diffs. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "generator_snapshot", columnDefinition = "jsonb")
    private String generatorSnapshot;

    /** Normalised plan: metrics{}, phases[], weekly_split[], meal_plan[], guidelines{}, ... */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String content;

    @Column(name = "coach_note", columnDefinition = "text")
    private String coachNote;

    @Column(name = "focus_note", columnDefinition = "text")
    private String focusNote;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "activated_at")
    private Instant activatedAt;

    /** Human-readable diff vs the baseline, as a JSON array of strings. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private String changelog = "[]";

    public boolean isLocked() {
        return switch (status) {
            case ACTIVE, SUPERSEDED, COMPLETED, ARCHIVED -> true;
            default -> false;
        };
    }
}
