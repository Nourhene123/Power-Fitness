package com.powerfitness.RestController;

import com.powerfitness.DTO.CoachDashboardDto;
import com.powerfitness.Services.Interface.CoachDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coach/dashboard")
@PreAuthorize("hasAnyRole('COACH','ADMIN')")
public class CoachDashboardController {

    private final CoachDashboardService coachDashboardService;

    public CoachDashboardController(CoachDashboardService coachDashboardService) {
        this.coachDashboardService = coachDashboardService;
    }

    @GetMapping
    public CoachDashboardDto dashboard() {
        return coachDashboardService.dashboard();
    }
}
