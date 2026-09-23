package com.powerfitness.controller;

import com.powerfitness.dto.ApproveVersionRequest;
import com.powerfitness.dto.CoachVersionDto;
import com.powerfitness.dto.PlanEditorDto;
import com.powerfitness.dto.ProgramVersionDetailDto;
import com.powerfitness.dto.RequestChangesBody;
import com.powerfitness.dto.SaveDraftRequest;
import com.powerfitness.security.AppUserPrincipal;
import com.powerfitness.service.CoachClientService;
import com.powerfitness.service.ProgramService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coach/program-versions")
@PreAuthorize("hasAnyRole('COACH','ADMIN')")
public class CoachProgramController {

    private final ProgramService programService;
    private final CoachClientService coachClientService;

    public CoachProgramController(ProgramService programService, CoachClientService coachClientService) {
        this.programService = programService;
        this.coachClientService = coachClientService;
    }

    @GetMapping("/{id}")
    public PlanEditorDto editor(@PathVariable Long id) {
        return toPlanEditorDto(programService.versionForEditor(id));
    }

    @PutMapping("/{id}")
    public PlanEditorDto saveDraft(@AuthenticationPrincipal AppUserPrincipal principal,
                                   @PathVariable Long id,
                                   @Valid @RequestBody SaveDraftRequest body) {
        return toPlanEditorDto(programService.saveDraft(id, principal.id(), body));
    }

    @PostMapping("/{id}/approve")
    public ProgramVersionDetailDto approve(@AuthenticationPrincipal AppUserPrincipal principal,
                                           @PathVariable Long id,
                                           @RequestBody(required = false) ApproveVersionRequest body) {
        String coachNote = body != null ? body.coachNote() : null;
        return programService.approve(id, principal.id(), coachNote);
    }

    @PostMapping("/{id}/request-changes")
    public ProgramVersionDetailDto requestChanges(@AuthenticationPrincipal AppUserPrincipal principal,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody RequestChangesBody body) {
        return programService.requestChanges(id, principal.id(), body);
    }

    private PlanEditorDto toPlanEditorDto(CoachVersionDto v) {
        return new PlanEditorDto(
                v.versionId(), v.programId(), v.versionNo(), v.status(), v.locked(), v.coachNote(),
                v.content(), v.liveChangelog(),
                coachClientService.profile(v.clientUserId()),
                coachClientService.safeExercisesFor(v.clientUserId()));
    }
}
