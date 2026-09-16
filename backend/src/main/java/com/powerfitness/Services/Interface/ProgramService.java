package com.powerfitness.Services.Interface;

import com.powerfitness.DTO.ChangeRequestDto;
import com.powerfitness.DTO.CoachVersionDto;
import com.powerfitness.DTO.ProgramVersionDetailDto;
import com.powerfitness.DTO.ProgramVersionSummaryDto;
import com.powerfitness.DTO.RequestChangesBody;
import com.powerfitness.DTO.SaveDraftRequest;
import com.powerfitness.Entity.Assessment;
import com.powerfitness.Entity.AssessmentAnalysis;
import com.powerfitness.Entity.Program;
import com.powerfitness.Entity.ProgramChangeRequest;
import com.powerfitness.Entity.ProgramVersion;
import com.powerfitness.Entity.User;
import com.powerfitness.common.domain.PlanContent;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Program state machine (port of {@code config/program_service.php}).
 * draft → in_review → changes_requested → approved → active → superseded → completed → archived.
 * Golden rule: an approved version's content is never edited and a version is never deleted —
 * post-approval edits create a new version (Phase 7).
 *
 * <p>The entity-returning methods below are for other services to compose with; the DTO-returning
 * ones are what {@code RestController}s call (the architecture forbids controllers from touching
 * {@code Entity} classes at all).
 */
public interface ProgramService {

    record CreateResult(Long programId, Long versionId) {}

    /**
     * Supersedes any prior engagement, then creates a program in {@code IN_REVIEW} with
     * version 1 holding the generated {@link PlanContent}, records a {@code SUBMITTED} event,
     * mirrors to {@code roadmaps}, and notifies the coach.
     */
    CreateResult createFromGeneration(User user, Assessment assessment, AssessmentAnalysis analysis,
                                      PlanContent content, String primaryGoal);

    Optional<Program> currentProgram(User user);

    Optional<ProgramVersion> currentVersion(Program program);

    ProgramVersion getVersion(Long versionId);

    List<ProgramVersion> versionsOf(Program program);

    Optional<ProgramChangeRequest> openChangeRequest(Program program);

    /* ---- controller-facing (DTOs only) ---- */

    List<ProgramVersionSummaryDto> versionSummaries(Long userId);

    ProgramVersionDetailDto versionDetail(Long userId, Long versionId);

    /** {@code assessmentValues} prefills the requested fields — see {@code AssessmentService.latestAssessmentValues}. */
    ChangeRequestDto changeRequestDto(Long userId, Map<String, Object> assessmentValues);

    /** The client answers the coach's questions; the open version goes back to {@code IN_REVIEW}. */
    void resolveChangeRequest(Long userId, Map<String, String> answers, String note);

    /** The coach approves a version: it goes {@code ACTIVE}, any prior active version is superseded. */
    ProgramVersionDetailDto approve(Long versionId, Long coachId, String coachNote);

    /** The coach asks the client to clarify specific fields before approving. */
    ProgramVersionDetailDto requestChanges(Long versionId, Long coachId, RequestChangesBody body);

    /** The plan editor's version-side data (the controller merges it with the client's profile). */
    CoachVersionDto versionForEditor(Long versionId);

    /** Saves the coach's in-progress edits. Rejected once the version is locked (active/superseded/...). */
    CoachVersionDto saveDraft(Long versionId, Long coachId, SaveDraftRequest body);

    /**
     * Clones the active version into a new {@code DRAFT} so a locked plan can be edited again —
     * returns the existing open draft instead of duplicating one if the coach already started one.
     */
    CoachVersionDto newVersionFromActive(Long programId, Long coachId);
}
