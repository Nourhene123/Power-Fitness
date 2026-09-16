package com.powerfitness.RestController;

import com.powerfitness.DTO.HabitToggleResult;
import com.powerfitness.DTO.LogWeighInRequest;
import com.powerfitness.DTO.LogWorkoutRequest;
import com.powerfitness.DTO.ProgressLogDto;
import com.powerfitness.DTO.TimelineItemDto;
import com.powerfitness.DTO.ToggleHabitRequest;
import com.powerfitness.DTO.WorkoutSessionDto;
import com.powerfitness.Security.AppUserPrincipal;
import com.powerfitness.Services.Interface.ProgressService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/me")
@PreAuthorize("hasRole('USER')")
public class MemberProgressController {

    private final ProgressService progressService;

    public MemberProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PostMapping("/workout-sessions")
    public WorkoutSessionDto logWorkout(@AuthenticationPrincipal AppUserPrincipal principal,
                                        @RequestBody LogWorkoutRequest body) {
        return progressService.logWorkout(principal.id(), body);
    }

    @GetMapping("/workout-sessions")
    public List<WorkoutSessionDto> recentWorkouts(@AuthenticationPrincipal AppUserPrincipal principal) {
        return progressService.recentWorkouts(principal.id());
    }

    @PostMapping("/weigh-ins")
    public ProgressLogDto logWeighIn(@AuthenticationPrincipal AppUserPrincipal principal,
                                      @RequestBody LogWeighInRequest body) {
        return progressService.logWeighIn(principal.id(), body);
    }

    @GetMapping("/weigh-ins")
    public List<ProgressLogDto> recentWeighIns(@AuthenticationPrincipal AppUserPrincipal principal) {
        return progressService.recentWeighIns(principal.id());
    }

    @PostMapping("/habits/toggle")
    public HabitToggleResult toggleHabit(@AuthenticationPrincipal AppUserPrincipal principal,
                                          @Valid @RequestBody ToggleHabitRequest body) {
        return progressService.toggleHabit(principal.id(), body.habit());
    }

    @GetMapping("/timeline")
    public List<TimelineItemDto> timeline(@AuthenticationPrincipal AppUserPrincipal principal) {
        return progressService.timeline(principal.id());
    }
}
