package com.powerfitness.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One day's habit check-ins for a user. Upserted per {@code (user, logDate)}. */
@Entity
@Table(name = "habit_logs",
        uniqueConstraints = @UniqueConstraint(name = "uq_habit_user_day", columnNames = {"user_id", "log_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "water_completed", nullable = false)
    @Builder.Default
    private boolean waterCompleted = false;

    @Column(name = "workout_completed", nullable = false)
    @Builder.Default
    private boolean workoutCompleted = false;

    @Column(name = "nutrition_completed", nullable = false)
    @Builder.Default
    private boolean nutritionCompleted = false;

    @Column(name = "sleep_completed", nullable = false)
    @Builder.Default
    private boolean sleepCompleted = false;

    @Column(name = "sleep_hours", nullable = false)
    @Builder.Default
    private Integer sleepHours = 0;
}
