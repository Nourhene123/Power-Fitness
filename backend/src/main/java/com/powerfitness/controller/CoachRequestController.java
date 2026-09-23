package com.powerfitness.controller;

import com.powerfitness.dto.CoachRequestDto;
import com.powerfitness.dto.CreateCoachRequestDto;
import com.powerfitness.service.CoachRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coach-requests")
public class CoachRequestController {

    private final CoachRequestService service;

    public CoachRequestController(CoachRequestService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CoachRequestDto submit(@Valid @RequestBody CreateCoachRequestDto request) {
        return service.submit(request);
    }
}
