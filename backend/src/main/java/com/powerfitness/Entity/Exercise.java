package com.powerfitness.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise extends BaseEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    @Builder.Default
    private ExerciseCategory category = ExerciseCategory.FULL_BODY;

    @Column(name = "primary_muscle", length = 40)
    private String primaryMuscle;

    @Column(nullable = false, length = 40)
    @Builder.Default
    private String equipment = "bodyweight";

    @Column(length = 255)
    private String patterns;

    @Column(name = "is_compound", nullable = false)
    @Builder.Default
    private boolean compound = false;
}
