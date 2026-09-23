package com.powerfitness.service;

import com.powerfitness.dto.HabitToggleResult;
import com.powerfitness.dto.LogWeighInRequest;
import com.powerfitness.dto.LogWorkoutRequest;
import com.powerfitness.dto.ProgressLogDto;
import com.powerfitness.dto.TimelineItemDto;
import com.powerfitness.dto.WorkoutSessionDto;
import java.util.List;

/**
 * Client progress logging: workouts, weigh-ins, daily habits, and the merged timeline.
 * Port of {@code log_workout.php}, {@code log_weight.php}, {@code toggle_habit.php} and
 * {@code progress_history.php}.
 */
public interface ProgressService {

    WorkoutSessionDto logWorkout(Long userId, LogWorkoutRequest body);

    List<WorkoutSessionDto> recentWorkouts(Long userId);

    ProgressLogDto logWeighIn(Long userId, LogWeighInRequest body);

    List<ProgressLogDto> recentWeighIns(Long userId);

    HabitToggleResult toggleHabit(Long userId, String habit);

    List<TimelineItemDto> timeline(Long userId);
}
