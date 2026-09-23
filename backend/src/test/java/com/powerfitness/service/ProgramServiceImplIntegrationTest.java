package com.powerfitness.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.powerfitness.dto.CoachVersionDto;
import com.powerfitness.dto.ProgramVersionDetailDto;
import com.powerfitness.dto.RequestChangesBody;
import com.powerfitness.dto.SaveDraftRequest;
import com.powerfitness.entity.Assessment;
import com.powerfitness.entity.Program;
import com.powerfitness.entity.ProgramStatus;
import com.powerfitness.entity.ProgramVersion;
import com.powerfitness.entity.Role;
import com.powerfitness.entity.User;
import com.powerfitness.exception.BusinessRuleException;
import com.powerfitness.repository.AssessmentRepository;
import com.powerfitness.repository.ProgramChangeRequestRepository;
import com.powerfitness.repository.ProgramRepository;
import com.powerfitness.repository.ProgramVersionRepository;
import com.powerfitness.repository.UserRepository;
import java.math.BigDecimal;
import com.powerfitness.service.ProgramService;
import com.powerfitness.common.domain.PlanContent;
import com.powerfitness.common.domain.PlanContent.Guidelines;
import com.powerfitness.common.domain.PlanContent.Meal;
import com.powerfitness.common.domain.PlanContent.Metrics;
import com.powerfitness.common.domain.PlanContent.Phase;
import com.powerfitness.common.domain.PlanContent.PlannedExercise;
import com.powerfitness.common.domain.PlanContent.TrainingDay;
import com.powerfitness.support.AbstractIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


@Transactional
class ProgramServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProgramService programService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private ProgramVersionRepository programVersionRepository;

    @Autowired
    private ProgramChangeRequestRepository changeRequestRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    private User newUser(String email, Role role) {
        return userRepository.save(User.builder()
                .name("Test " + role)
                .email(email)
                .password("irrelevant-for-this-test")
                .role(role)
                .build());
    }

    private ProgramService.CreateResult createProgram(User client) {
        Assessment assessment = assessmentRepository.save(Assessment.builder()
                .user(client)
                .age(30)
                .heightCm(new BigDecimal("180.00"))
                .weightKg(new BigDecimal("85.00"))
                .targetWeightKg(new BigDecimal("78.00"))
                .primaryGoal("fat_loss")
                .build());
        return programService.createFromGeneration(client, assessment, null, samplePlan(2000), "fat_loss");
    }

    @Test
    void createFromGenerationStartsAProgramInReviewWithVersionOne() {
        User client = newUser("client-create@example.com", Role.USER);

        ProgramService.CreateResult result = createProgram(client);

        Program program = programRepository.findById(result.programId()).orElseThrow();
        ProgramVersion version = programVersionRepository.findById(result.versionId()).orElseThrow();
        assertThat(program.getStatus()).isEqualTo(ProgramStatus.IN_REVIEW);
        assertThat(version.getStatus()).isEqualTo(ProgramStatus.IN_REVIEW);
        assertThat(version.getVersionNo()).isEqualTo(1);
    }

    @Test
    void aSecondAssessmentSupersedesThePriorLiveProgram() {
        User client = newUser("client-resubmit@example.com", Role.USER);
        ProgramService.CreateResult first = createProgram(client);

        createProgram(client);

        Program firstProgram = programRepository.findById(first.programId()).orElseThrow();
        assertThat(firstProgram.getStatus()).isEqualTo(ProgramStatus.SUPERSEDED);
    }

    @Test
    void approvingForTheFirstTimeActivatesTheVersionAndSetsTheProgramStartDate() {
        User client = newUser("client-approve@example.com", Role.USER);
        User coach = newUser("coach-approve@example.com", Role.COACH);
        ProgramService.CreateResult created = createProgram(client);

        ProgramVersionDetailDto detail = programService.approve(created.versionId(), coach.getId(), "Looks good");

        assertThat(detail.status()).isEqualTo("ACTIVE");
        Program program = programRepository.findById(created.programId()).orElseThrow();
        assertThat(program.getStatus()).isEqualTo(ProgramStatus.ACTIVE);
        assertThat(program.getActiveVersionId()).isEqualTo(created.versionId());
        assertThat(program.getStartDate()).isNotNull();
    }

    @Test
    void approvingASecondVersionSupersedesThePreviouslyActiveOne() {
        User client = newUser("client-update@example.com", Role.USER);
        User coach = newUser("coach-update@example.com", Role.COACH);
        ProgramService.CreateResult created = createProgram(client);
        programService.approve(created.versionId(), coach.getId(), "v1 approved");

        CoachVersionDto v2 = programService.newVersionFromActive(created.programId(), coach.getId());
        ProgramVersionDetailDto approvedV2 = programService.approve(v2.versionId(), coach.getId(), "v2 approved");

        assertThat(approvedV2.versionNo()).isEqualTo(2);
        ProgramVersion v1 = programVersionRepository.findById(created.versionId()).orElseThrow();
        assertThat(v1.getStatus()).isEqualTo(ProgramStatus.SUPERSEDED);
        Program program = programRepository.findById(created.programId()).orElseThrow();
        assertThat(program.getActiveVersionId()).isEqualTo(v2.versionId());
    }

    @Test
    void requestChangesRejectsABlankNote() {
        User client = newUser("client-blank-note@example.com", Role.USER);
        User coach = newUser("coach-blank-note@example.com", Role.COACH);
        ProgramService.CreateResult created = createProgram(client);

        assertThatThrownBy(() -> programService.requestChanges(
                created.versionId(), coach.getId(), new RequestChangesBody("  ", List.of())))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void requestChangesMovesVersionAndProgramToChangesRequestedAndOpensAChangeRequest() {
        User client = newUser("client-changes@example.com", Role.USER);
        User coach = newUser("coach-changes@example.com", Role.COACH);
        ProgramService.CreateResult created = createProgram(client);

        programService.requestChanges(created.versionId(), coach.getId(),
                new RequestChangesBody("Please clarify your injury history", List.of()));

        ProgramVersion version = programVersionRepository.findById(created.versionId()).orElseThrow();
        Program program = programRepository.findById(created.programId()).orElseThrow();
        assertThat(version.getStatus()).isEqualTo(ProgramStatus.CHANGES_REQUESTED);
        assertThat(program.getStatus()).isEqualTo(ProgramStatus.CHANGES_REQUESTED);
        assertThat(changeRequestRepository.findFirstByProgramAndResolvedFalseOrderByIdDesc(program)).isPresent();
    }

    @Test
    void resolveChangeRequestReturnsTheVersionToInReviewButLeavesTheRequestOpenUntilApproval() {
        User client = newUser("client-resolve@example.com", Role.USER);
        User coach = newUser("coach-resolve@example.com", Role.COACH);
        ProgramService.CreateResult created = createProgram(client);
        programService.requestChanges(created.versionId(), coach.getId(),
                new RequestChangesBody("Clarify your goal weight", List.of()));

        programService.resolveChangeRequest(client.getId(), java.util.Map.of(), "Here is my answer");

        ProgramVersion version = programVersionRepository.findById(created.versionId()).orElseThrow();
        Program program = programRepository.findById(created.programId()).orElseThrow();
        assertThat(version.getStatus()).isEqualTo(ProgramStatus.IN_REVIEW);
        assertThat(program.getStatus()).isEqualTo(ProgramStatus.IN_REVIEW);
        // by design, the change request itself is only marked resolved on final approval
        assertThat(changeRequestRepository.findFirstByProgramAndResolvedFalseOrderByIdDesc(program)).isPresent();
    }

    @Test
    void resolveChangeRequestThrowsWhenNothingIsPending() {
        User client = newUser("client-nothing-pending@example.com", Role.USER);
        createProgram(client);

        assertThatThrownBy(() -> programService.resolveChangeRequest(client.getId(), java.util.Map.of(), "note"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void saveDraftIsRejectedOnceTheVersionIsLocked() {
        User client = newUser("client-locked@example.com", Role.USER);
        User coach = newUser("coach-locked@example.com", Role.COACH);
        ProgramService.CreateResult created = createProgram(client);
        programService.approve(created.versionId(), coach.getId(), "approved");

        assertThatThrownBy(() -> programService.saveDraft(created.versionId(), coach.getId(),
                new SaveDraftRequest(samplePlan(2100), "trying to edit a locked version")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void newVersionFromActiveClonesTheActiveVersionIntoAnIncrementedDraft() {
        User client = newUser("client-clone@example.com", Role.USER);
        User coach = newUser("coach-clone@example.com", Role.COACH);
        ProgramService.CreateResult created = createProgram(client);
        programService.approve(created.versionId(), coach.getId(), "approved");

        CoachVersionDto draft = programService.newVersionFromActive(created.programId(), coach.getId());

        assertThat(draft.versionNo()).isEqualTo(2);
        assertThat(draft.status()).isEqualTo("DRAFT");
        assertThat(draft.locked()).isFalse();
    }

    @Test
    void newVersionFromActiveReusesAnAlreadyOpenDraftInsteadOfDuplicating() {
        User client = newUser("client-reuse-draft@example.com", Role.USER);
        User coach = newUser("coach-reuse-draft@example.com", Role.COACH);
        ProgramService.CreateResult created = createProgram(client);
        programService.approve(created.versionId(), coach.getId(), "approved");
        CoachVersionDto firstDraft = programService.newVersionFromActive(created.programId(), coach.getId());

        CoachVersionDto secondCall = programService.newVersionFromActive(created.programId(), coach.getId());

        assertThat(secondCall.versionId()).isEqualTo(firstDraft.versionId());
        assertThat(programVersionRepository.findByProgramOrderByVersionNoAsc(
                programRepository.findById(created.programId()).orElseThrow())).hasSize(2);
    }

    // --- test data ----------------------------------------------------

    private static PlanContent samplePlan(int calories) {
        Metrics metrics = new Metrics(24.0, "Healthy weight", 1700, 2200, calories, 160, 200, 60,
                2.8, "12-Week Test Roadmap");
        Phase phase = new Phase(1, "Phase 1", "Weeks 1-3", "Foundation", "65-70% 1RM", List.of("Show up"));
        TrainingDay day = new TrainingDay("Day 1: Monday", "Full Body", "60 mins",
                List.of(new PlannedExercise("Squat", "3 sets", "10 reps", "90s", "")));
        Meal meal = new Meal(1, "Breakfast", 500, 40, 50, 15, "Oats and eggs");
        Guidelines guidelines = new Guidelines("Drink water", "Sleep well", List.of("Creatine"), "Log daily");
        return new PlanContent(metrics, List.of(phase), List.of(day), List.of(meal), guidelines,
                List.of(), null, List.of());
    }
}
