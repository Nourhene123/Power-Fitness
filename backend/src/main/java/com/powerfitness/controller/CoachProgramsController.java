package com.powerfitness.controller;

import com.powerfitness.dto.CoachVersionDto;
import com.powerfitness.security.AppUserPrincipal;
import com.powerfitness.service.ProgramService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coach/programs")
@PreAuthorize("hasAnyRole('COACH','ADMIN')")
public class CoachProgramsController {

    private final ProgramService programService;

    public CoachProgramsController(ProgramService programService) {
        this.programService = programService;
    }

    @PostMapping("/{id}/new-version")
    public CoachVersionDto newVersion(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        return programService.newVersionFromActive(id, principal.id());
    }
}
