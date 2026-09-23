package com.powerfitness.controller;

import com.powerfitness.dto.DashboardDto;
import com.powerfitness.security.AppUserPrincipal;
import com.powerfitness.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/dashboard")
@PreAuthorize("hasRole('USER')")
public class MemberDashboardController {

    private final DashboardService dashboardService;

    public MemberDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardDto dashboard(@AuthenticationPrincipal AppUserPrincipal principal) {
        return dashboardService.build(principal.id());
    }
}
