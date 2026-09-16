package com.powerfitness.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "coach_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachRequest extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    /** {@code beginner} | {@code intermediate} | {@code advanced}. */
    @Column(nullable = false, length = 15)
    @Builder.Default
    private String experience = "beginner";

    @Column(nullable = false, columnDefinition = "text")
    private String goals;

    /** {@code pending} | {@code contacted} | {@code completed}. */
    @Column(nullable = false, length = 12)
    @Builder.Default
    private String status = "pending";
}
