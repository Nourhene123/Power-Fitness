package com.powerfitness.controller;

import com.powerfitness.dto.ClientProfileDto;
import com.powerfitness.dto.ClientProgressDto;
import com.powerfitness.service.CoachClientService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coach/clients")
@PreAuthorize("hasAnyRole('COACH','ADMIN')")
public class CoachClientController {

    private final CoachClientService coachClientService;

    public CoachClientController(CoachClientService coachClientService) {
        this.coachClientService = coachClientService;
    }

    @GetMapping("/{userId}/profile")
    public ClientProfileDto profile(@PathVariable Long userId) {
        return coachClientService.profile(userId);
    }

    @GetMapping("/{userId}/progress")
    public ClientProgressDto progress(@PathVariable Long userId) {
        return coachClientService.progress(userId);
    }
}
