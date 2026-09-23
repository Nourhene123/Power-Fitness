package com.powerfitness.controller;

import com.powerfitness.dto.CoachDashboardDto;
import com.powerfitness.service.CoachDashboardService;
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
