package com.powerfitness.Services.Implimentation;

import com.powerfitness.Entity.HabitLog;
import com.powerfitness.Entity.User;
import com.powerfitness.Entity.WorkoutSession;
import com.powerfitness.Repository.HabitLogRepository;
import com.powerfitness.Repository.ProgressLogRepository;
import com.powerfitness.Repository.WorkoutSessionRepository;
import com.powerfitness.Services.Interface.ClientRiskService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ClientRiskServiceImpl implements ClientRiskService {

    private static final int NO_WEIGH_IN_DAYS = 10;
    private static final int NO_WORKOUT_DAYS = 7;
    private static final int ADHERENCE_WINDOW_DAYS = 7;
    private static final int LOW_ADHERENCE_PCT = 40;

    private final ProgressLogRepository progressLogs;
    private final WorkoutSessionRepository workoutSessions;
    private final HabitLogRepository habitLogs;

    public ClientRiskServiceImpl(ProgressLogRepository progressLogs, WorkoutSessionRepository workoutSessions,
                                 HabitLogRepository habitLogs) {
        this.progressLogs = progressLogs;
        this.workoutSessions = workoutSessions;
        this.habitLogs = habitLogs;
    }

    @Override
    public Assessment assess(User user) {
        List<String> reasons = new ArrayList<>();
        LocalDate today = LocalDate.now();

        var lastWeighIn = progressLogs.findFirstByUserAndWeightKgIsNotNullOrderByLoggedOnDesc(user);
        if (lastWeighIn.isEmpty() || ChronoUnit.DAYS.between(lastWeighIn.get().getLoggedOn(), today) > NO_WEIGH_IN_DAYS) {
            reasons.add("No weigh-in in over " + NO_WEIGH_IN_DAYS + " days");
        }

        List<WorkoutSession> workouts = workoutSessions.findByUserOrderBySessionDateDescIdDesc(user);
        if (workouts.isEmpty() || ChronoUnit.DAYS.between(workouts.get(0).getSessionDate(), today) > NO_WORKOUT_DAYS) {
            reasons.add("No workout logged in over " + NO_WORKOUT_DAYS + " days");
        }

        LocalDate windowStart = today.minusDays(ADHERENCE_WINDOW_DAYS);
        List<HabitLog> weekLogs = habitLogs.findByUserAndLogDateBetweenOrderByLogDateAsc(user, windowStart, today);
        if (weekLogs.isEmpty()) {
            reasons.add("No check-ins logged in the last " + ADHERENCE_WINDOW_DAYS + " days");
        } else {
            long completed = weekLogs.stream()
                    .mapToLong(l -> (l.isWorkoutCompleted() ? 1 : 0) + (l.isNutritionCompleted() ? 1 : 0)
                            + (l.isWaterCompleted() ? 1 : 0))
                    .sum();
            int possible = weekLogs.size() * 3;
            int pct = possible > 0 ? Math.round(completed * 100f / possible) : 0;
            if (pct < LOW_ADHERENCE_PCT) {
                reasons.add("Habit adherence dropped to " + pct + "% this week");
            }
        }

        return new Assessment(!reasons.isEmpty(), reasons);
    }
}
