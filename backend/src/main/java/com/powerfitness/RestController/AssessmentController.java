package com.powerfitness.RestController;

import com.powerfitness.DTO.AssessmentSubmittedDto;
import com.powerfitness.DTO.SubmitAssessmentRequest;
import com.powerfitness.Security.AppUserPrincipal;
import com.powerfitness.Services.Interface.AssessmentService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assessments")
@PreAuthorize("hasRole('USER')")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping("/me")
    public Map<String, Boolean> mine(@AuthenticationPrincipal AppUserPrincipal principal) {
        return Map.of("hasAssessment", assessmentService.hasAssessment(principal.id()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssessmentSubmittedDto submit(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @Valid @RequestBody SubmitAssessmentRequest request) {
        return assessmentService.submit(principal.id(), request);
    }
}
