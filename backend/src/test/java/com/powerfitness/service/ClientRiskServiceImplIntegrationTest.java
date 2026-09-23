package com.powerfitness.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.powerfitness.entity.HabitLog;
import com.powerfitness.entity.ProgressLog;
import com.powerfitness.entity.Role;
import com.powerfitness.entity.User;
import com.powerfitness.entity.WorkoutSession;
import com.powerfitness.repository.HabitLogRepository;
import com.powerfitness.repository.ProgressLogRepository;
import com.powerfitness.repository.UserRepository;
import com.powerfitness.repository.WorkoutSessionRepository;
import com.powerfitness.service.ClientRiskService;
import com.powerfitness.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs {@link ClientRiskServiceImpl} against a real PostgreSQL instance (Testcontainers) with
 * data going through the real repositories, not mocks — the derived query methods and entity
 * mappings only get verified by actually hitting the database.
 */
@Transactional
class ClientRiskServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ClientRiskService clientRiskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProgressLogRepository progressLogRepository;

    @Autowired
    private WorkoutSessionRepository workoutSessionRepository;

    @Autowired
    private HabitLogRepository habitLogRepository;

    private User newClient(String email) {
        return userRepository.save(User.builder()
                .name("Test Client")
                .email(email)
                .password("irrelevant-for-this-test")
                .role(Role.USER)
                .build());
    }

    @Test
    void aBrandNewClientWithNoLogsAtAllIsFlaggedForAllThreeReasons() {
        User client = newClient("brand-new@example.com");

        ClientRiskService.Assessment assessment = clientRiskService.assess(client);

        assertThat(assessment.atRisk()).isTrue();
        assertThat(assessment.reasons())
                .anySatisfy(r -> assertThat(r).contains("No weigh-in"))
                .anySatisfy(r -> assertThat(r).contains("No workout"))
                .anySatisfy(r -> assertThat(r).contains("No check-ins"));
    }

    @Test
    void aClientWithRecentActivityAndGoodAdherenceIsNotAtRisk() {
        User client = newClient("on-track@example.com");
        LocalDate today = LocalDate.now();

        progressLogRepository.save(ProgressLog.builder()
                .user(client).loggedOn(today).weightKg(new BigDecimal("80.00")).build());
        workoutSessionRepository.save(WorkoutSession.builder()
                .user(client).sessionDate(today).build());
        for (int i = 0; i < 5; i++) {
            habitLogRepository.save(HabitLog.builder()
                    .user(client).logDate(today.minusDays(i))
                    .workoutCompleted(true).nutritionCompleted(true).waterCompleted(true)
                    .build());
        }

        ClientRiskService.Assessment assessment = clientRiskService.assess(client);

        assertThat(assessment.atRisk()).isFalse();
        assertThat(assessment.reasons()).isEmpty();
    }

    @Test
    void aStaleWeighInOlderThanTenDaysIsFlaggedEvenWithRecentWorkoutsAndHabits() {
        User client = newClient("stale-weighin@example.com");
        LocalDate today = LocalDate.now();

        progressLogRepository.save(ProgressLog.builder()
                .user(client).loggedOn(today.minusDays(15)).weightKg(new BigDecimal("80.00")).build());
        workoutSessionRepository.save(WorkoutSession.builder()
                .user(client).sessionDate(today).build());
        habitLogRepository.save(HabitLog.builder()
                .user(client).logDate(today)
                .workoutCompleted(true).nutritionCompleted(true).waterCompleted(true)
                .build());

        ClientRiskService.Assessment assessment = clientRiskService.assess(client);

        assertThat(assessment.atRisk()).isTrue();
        assertThat(assessment.reasons()).containsExactly("No weigh-in in over 10 days");
    }

    @Test
    void lowHabitCompletionThisWeekIsFlaggedWithTheComputedPercentage() {
        User client = newClient("low-adherence@example.com");
        LocalDate today = LocalDate.now();

        progressLogRepository.save(ProgressLog.builder()
                .user(client).loggedOn(today).weightKg(new BigDecimal("80.00")).build());
        workoutSessionRepository.save(WorkoutSession.builder()
                .user(client).sessionDate(today).build());
        // 1 of 3 habits completed on each of 3 days this week -> 33%, below the 40% threshold
        for (int i = 0; i < 3; i++) {
            habitLogRepository.save(HabitLog.builder()
                    .user(client).logDate(today.minusDays(i))
                    .workoutCompleted(true).nutritionCompleted(false).waterCompleted(false)
                    .build());
        }

        ClientRiskService.Assessment assessment = clientRiskService.assess(client);

        assertThat(assessment.atRisk()).isTrue();
        assertThat(assessment.reasons()).containsExactly("Habit adherence dropped to 33% this week");
    }
}
