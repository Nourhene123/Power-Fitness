package com.powerfitness.controller;

import com.powerfitness.dto.ChangeRequestDto;
import com.powerfitness.dto.MyProgramDto;
import com.powerfitness.dto.ProgramVersionDetailDto;
import com.powerfitness.dto.ProgramVersionSummaryDto;
import com.powerfitness.dto.ResolveChangeRequestBody;
import com.powerfitness.security.AppUserPrincipal;
import com.powerfitness.service.AssessmentService;
import com.powerfitness.service.ProgramService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/program")
@PreAuthorize("hasRole('USER')")
public class MemberProgramController {

    private final AssessmentService assessmentService;
    private final ProgramService programService;

    public MemberProgramController(AssessmentService assessmentService, ProgramService programService) {
        this.assessmentService = assessmentService;
        this.programService = programService;
    }

    @GetMapping
    public MyProgramDto program(@AuthenticationPrincipal AppUserPrincipal principal) {
        return assessmentService.currentProgram(principal.id());
    }

    @GetMapping("/versions")
    public List<ProgramVersionSummaryDto> versions(@AuthenticationPrincipal AppUserPrincipal principal) {
        return programService.versionSummaries(principal.id());
    }

    @GetMapping("/versions/{id}")
    public ProgramVersionDetailDto version(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        return programService.versionDetail(principal.id(), id);
    }

    @GetMapping("/change-request")
    public ChangeRequestDto changeRequest(@AuthenticationPrincipal AppUserPrincipal principal) {
        return programService.changeRequestDto(principal.id(), assessmentService.latestAssessmentValues(principal.id()));
    }

    @PostMapping("/change-request/resolve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resolveChangeRequest(@AuthenticationPrincipal AppUserPrincipal principal,
                                     @RequestBody ResolveChangeRequestBody body) {
        programService.resolveChangeRequest(principal.id(),
                body.answers() == null ? Map.of() : body.answers(), body.message());
    }
}
