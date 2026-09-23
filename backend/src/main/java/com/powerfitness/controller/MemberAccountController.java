package com.powerfitness.controller;

import com.powerfitness.dto.AccountSummaryDto;
import com.powerfitness.security.AppUserPrincipal;
import com.powerfitness.service.AssessmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/account")
@PreAuthorize("hasRole('USER')")
public class MemberAccountController {

    private final AssessmentService assessmentService;

    public MemberAccountController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping
    public AccountSummaryDto account(@AuthenticationPrincipal AppUserPrincipal principal) {
        return new AccountSummaryDto(assessmentService.assessmentCount(principal.id()));
    }
}
