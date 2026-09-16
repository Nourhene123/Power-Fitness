package com.powerfitness.Services.Implimentation;

import com.powerfitness.DTO.ChangeFieldDto;
import com.powerfitness.DTO.ChangeRequestDto;
import com.powerfitness.DTO.CoachVersionDto;
import com.powerfitness.DTO.ProgramVersionDetailDto;
import com.powerfitness.DTO.ProgramVersionSummaryDto;
import com.powerfitness.DTO.RequestChangesBody;
import com.powerfitness.DTO.SaveDraftRequest;
import com.powerfitness.Entity.Assessment;
import com.powerfitness.Entity.AssessmentAnalysis;
import com.powerfitness.Entity.CoachStatus;
import com.powerfitness.Entity.Program;
import com.powerfitness.Entity.ProgramChangeRequest;
import com.powerfitness.Entity.ProgramReviewEvent;
import com.powerfitness.Entity.ProgramStatus;
import com.powerfitness.Entity.ProgramVersion;
import com.powerfitness.Entity.ReviewAction;
import com.powerfitness.Entity.Roadmap;
import com.powerfitness.Entity.RoadmapStatus;
import com.powerfitness.Entity.User;
import com.powerfitness.Entity.VersionAuthor;
import com.powerfitness.Exception.BusinessRuleException;
import com.powerfitness.Exception.ResourceNotFoundException;
import com.powerfitness.Mapper.ProgramVersionMapper;
import com.powerfitness.Repository.ProgramChangeRequestRepository;
import com.powerfitness.Repository.ProgramRepository;
import com.powerfitness.Repository.ProgramReviewEventRepository;
import com.powerfitness.Repository.ProgramVersionRepository;
import com.powerfitness.Repository.RoadmapRepository;
import com.powerfitness.Repository.UserRepository;
import com.powerfitness.Services.Interface.ChangelogService;
import com.powerfitness.Services.Interface.NotificationService;
import com.powerfitness.Services.Interface.ProgramService;
import com.powerfitness.common.domain.PlanContent;
import com.powerfitness.common.util.JsonUtil;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProgramServiceImpl implements ProgramService {

    private static final List<ProgramStatus> LIVE = List.of(
            ProgramStatus.DRAFT, ProgramStatus.IN_REVIEW, ProgramStatus.CHANGES_REQUESTED,
            ProgramStatus.APPROVED, ProgramStatus.ACTIVE);

    private static final List<ProgramStatus> SUPERSEDABLE = List.of(
            ProgramStatus.DRAFT, ProgramStatus.IN_REVIEW, ProgramStatus.CHANGES_REQUESTED, ProgramStatus.APPROVED);

    private final ProgramRepository programs;
    private final ProgramVersionRepository versions;
    private final ProgramReviewEventRepository events;
    private final ProgramChangeRequestRepository changeRequests;
    private final RoadmapRepository roadmaps;
    private final UserRepository users;
    private final NotificationService notifications;
    private final ChangelogService changelog;
    private final ProgramVersionMapper mapper;
    private final JsonUtil json;

    public ProgramServiceImpl(ProgramRepository programs, ProgramVersionRepository versions,
                              ProgramReviewEventRepository events, ProgramChangeRequestRepository changeRequests,
                              RoadmapRepository roadmaps, UserRepository users,
                              NotificationService notifications, ChangelogService changelog,
                              ProgramVersionMapper mapper, JsonUtil json) {
        this.programs = programs;
        this.versions = versions;
        this.events = events;
        this.changeRequests = changeRequests;
        this.roadmaps = roadmaps;
        this.users = users;
        this.notifications = notifications;
        this.changelog = changelog;
        this.mapper = mapper;
        this.json = json;
    }

    @Override
    public CreateResult createFromGeneration(User user, Assessment assessment, AssessmentAnalysis analysis,
                                             PlanContent content, String primaryGoal) {
        // Any prior engagement becomes superseded.
        for (Program prev : programs.findByUserAndStatusNotIn(user,
                List.of(ProgramStatus.SUPERSEDED, ProgramStatus.ARCHIVED, ProgramStatus.COMPLETED))) {
            prev.setStatus(ProgramStatus.SUPERSEDED);
            versions.findByProgramAndStatusIn(prev, LIVE)
                    .forEach(v -> v.setStatus(ProgramStatus.SUPERSEDED));
            events.save(ProgramReviewEvent.builder()
                    .program(prev).versionId(null).actorId(user.getId()).actorRole("SYSTEM")
                    .action(ReviewAction.SUPERSEDED).build());
        }

        String contentJson = json.write(content);

        Program program = programs.save(Program.builder()
                .user(user)
                .assessment(assessment)
                .analysis(analysis)
                .primaryGoal(primaryGoal)
                .status(ProgramStatus.IN_REVIEW)
                .build());

        ProgramVersion version = versions.save(ProgramVersion.builder()
                .program(program)
                .versionNo(1)
                .status(ProgramStatus.IN_REVIEW)
                .createdBy(VersionAuthor.GENERATOR)
                .generatorSnapshot(contentJson)
                .content(contentJson)
                .changelog("[]")
                .build());

        events.save(ProgramReviewEvent.builder()
                .program(program).versionId(version.getId()).actorId(user.getId()).actorRole("CLIENT")
                .action(ReviewAction.SUBMITTED)
                .payload(json.write(Map.of("assessmentId", assessment.getId())))
                .build());

        mirrorToRoadmaps(program, version, content);

        notifications.coachUser().ifPresent(coach -> notifications.notify(coach, "draft_submitted",
                "New assessment to review",
                "A client just submitted their assessment. A draft roadmap is waiting for your review.",
                "/coach/plan/" + version.getId()));

        return new CreateResult(program.getId(), version.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Program> currentProgram(User user) {
        return programs.findFirstByUserOrderByIdDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProgramVersion> currentVersion(Program program) {
        if (program.getActiveVersionId() != null) {
            Optional<ProgramVersion> active = versions.findById(program.getActiveVersionId());
            if (active.isPresent()) {
                return active;
            }
        }
        return versions.findTopByProgramOrderByVersionNoDesc(program);
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramVersion getVersion(Long versionId) {
        return versions.findById(versionId)
                .orElseThrow(() -> ResourceNotFoundException.of("ProgramVersion", versionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgramVersion> versionsOf(Program program) {
        return versions.findByProgramOrderByVersionNoAsc(program);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProgramChangeRequest> openChangeRequest(Program program) {
        return changeRequests.findFirstByProgramAndResolvedFalseOrderByIdDesc(program);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgramVersionSummaryDto> versionSummaries(Long userId) {
        Program program = ownProgram(userId);
        Long currentId = program.getActiveVersionId();
        return versionsOf(program).stream()
                .sorted((a, b) -> Integer.compare(b.getVersionNo(), a.getVersionNo())) // newest first
                .map(v -> toSummaryDto(v, currentId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramVersionDetailDto versionDetail(Long userId, Long versionId) {
        ProgramVersion version = getVersion(versionId);
        if (!version.getProgram().getUser().getId().equals(userId)) {
            throw ResourceNotFoundException.of("ProgramVersion", versionId);
        }
        return toDetailDto(version);
    }

    @Override
    @Transactional(readOnly = true)
    public ChangeRequestDto changeRequestDto(Long userId, Map<String, Object> assessmentValues) {
        Program program = ownProgram(userId);
        ProgramChangeRequest cr = openChangeRequest(program)
                .orElseThrow(() -> new ResourceNotFoundException("No open change request."));

        List<ChangeFieldDto> fields = parseFields(cr.getFields());
        Map<String, String> prefill = new LinkedHashMap<>();
        for (ChangeFieldDto f : fields) {
            Object v = assessmentValues.get(f.field());
            if (v instanceof List<?> list) {
                String joined = list.stream().map(String::valueOf).collect(Collectors.joining(", "));
                if (!joined.isBlank()) prefill.put(f.field(), joined);
            } else if (v != null && !(v instanceof Map)) {
                prefill.put(f.field(), String.valueOf(v));
            }
        }
        return new ChangeRequestDto(program.getId(), cr.getNote(), fields, prefill);
    }

    @Override
    public void resolveChangeRequest(Long userId, Map<String, String> answers, String note) {
        Program program = ownProgram(userId);

        ProgramVersion version = versions.findByProgramAndStatusIn(program, List.of(ProgramStatus.CHANGES_REQUESTED))
                .stream()
                .max((a, b) -> Integer.compare(a.getVersionNo(), b.getVersionNo()))
                .orElseThrow(() -> new BusinessRuleException("No pending change request for this program."));

        version.setStatus(ProgramStatus.IN_REVIEW);
        program.setStatus(ProgramStatus.IN_REVIEW);

        events.save(ProgramReviewEvent.builder()
                .program(program).versionId(version.getId()).actorId(userId).actorRole("CLIENT")
                .action(ReviewAction.RESUBMITTED)
                .payload(json.write(Map.of("note", note == null ? "" : note,
                        "answers", answers == null ? Map.of() : answers)))
                .build());

        mirrorToRoadmaps(program, version, json.read(version.getContent(), PlanContent.class));

        notifications.coachUser().ifPresent(coach -> notifications.notify(coach, "client_resubmitted",
                "Client answered your questions",
                (note != null && !note.isBlank()) ? note : "The client resubmitted their plan for review.",
                "/coach/plan/" + version.getId()));
    }

    @Override
    public ProgramVersionDetailDto approve(Long versionId, Long coachId, String coachNote) {
        ProgramVersion version = getVersion(versionId);
        Program program = version.getProgram();
        User coach = users.findById(coachId).orElseThrow(() -> ResourceNotFoundException.of("User", coachId));

        Long prevActiveId = program.getActiveVersionId();
        boolean isUpdate = prevActiveId != null && !prevActiveId.equals(versionId);

        PlanContent baseContent;
        ProgramVersion prevActive = prevActiveId != null ? versions.findById(prevActiveId).orElse(null) : null;
        if (prevActive != null) {
            if (isUpdate) {
                prevActive.setStatus(ProgramStatus.SUPERSEDED);
            }
            baseContent = json.read(prevActive.getContent(), PlanContent.class);
        } else {
            baseContent = json.read(version.getGeneratorSnapshot(), PlanContent.class);
        }

        // every other non-final version on this program loses to this one
        versions.findByProgramAndStatusIn(program, SUPERSEDABLE).stream()
                .filter(v -> !v.getId().equals(versionId))
                .forEach(v -> v.setStatus(ProgramStatus.SUPERSEDED));

        PlanContent content = json.read(version.getContent(), PlanContent.class);
        List<String> changes = changelog.compute(baseContent, content);

        Instant now = Instant.now();
        version.setStatus(ProgramStatus.ACTIVE);
        version.setCoachNote(coachNote);
        version.setApprovedBy(coachId);
        version.setApprovedAt(now);
        version.setActivatedAt(now);
        version.setChangelog(json.write(changes));

        program.setActiveVersionId(versionId);
        program.setCoach(coach);
        program.setStatus(ProgramStatus.ACTIVE);
        if (program.getStartDate() == null) {
            program.setStartDate(LocalDate.now());
        }

        openChangeRequest(program).ifPresent(cr -> {
            cr.setResolved(true);
            cr.setResolvedAt(now);
        });

        events.save(ProgramReviewEvent.builder()
                .program(program).versionId(versionId).actorId(coachId).actorRole("COACH")
                .action(ReviewAction.APPROVED)
                .payload(json.write(Map.of("isUpdate", isUpdate, "changes", changes.size())))
                .build());

        mirrorToRoadmaps(program, version, content);

        User client = program.getUser();
        if (isUpdate) {
            notifications.notify(client, "plan_updated", "Coach updated your plan",
                    "Coach Ilyesse released v" + version.getVersionNo() + " of your roadmap. "
                            + changes.size() + " change(s) — see the changelog.",
                    "/dashboard");
        } else {
            notifications.notify(client, "plan_approved", "Your roadmap is approved 🎉",
                    "Coach Ilyesse has reviewed and approved your plan. It's now active on your dashboard.",
                    "/dashboard");
        }

        return toDetailDto(version);
    }

    @Override
    public ProgramVersionDetailDto requestChanges(Long versionId, Long coachId, RequestChangesBody body) {
        String note = body != null ? body.note() : null;
        List<ChangeFieldDto> fields = body != null && body.fields() != null ? body.fields() : List.of();
        if (note == null || note.isBlank()) {
            throw new BusinessRuleException("Explain to the client what needs to be clarified.");
        }
        ProgramVersion version = getVersion(versionId);
        Program program = version.getProgram();
        User coach = users.findById(coachId).orElseThrow(() -> ResourceNotFoundException.of("User", coachId));

        version.setStatus(ProgramStatus.CHANGES_REQUESTED);
        version.setCoachNote(note);

        program.setStatus(ProgramStatus.CHANGES_REQUESTED);
        program.setCoach(coach);

        changeRequests.save(ProgramChangeRequest.builder()
                .program(program)
                .versionId(versionId)
                .fields(json.write(fields))
                .note(note)
                .resolved(false)
                .build());

        events.save(ProgramReviewEvent.builder()
                .program(program).versionId(versionId).actorId(coachId).actorRole("COACH")
                .action(ReviewAction.CHANGES_REQUESTED)
                .payload(json.write(Map.of("fields", fields.size())))
                .build());

        mirrorToRoadmaps(program, version, json.read(version.getContent(), PlanContent.class));

        notifications.notify(program.getUser(), "changes_requested", "Coach needs a bit more info",
                note, "/program/answer-coach");

        return toDetailDto(version);
    }

    @Override
    @Transactional(readOnly = true)
    public CoachVersionDto versionForEditor(Long versionId) {
        return toCoachVersionDto(getVersion(versionId));
    }

    @Override
    public CoachVersionDto saveDraft(Long versionId, Long coachId, SaveDraftRequest body) {
        ProgramVersion version = getVersion(versionId);
        if (version.isLocked()) {
            throw new BusinessRuleException("This version is locked — create a new version to make changes.");
        }
        Program program = version.getProgram();
        PlanContent content = body.content();

        version.setContent(json.write(content));
        if (body.coachNote() != null) {
            version.setCoachNote(body.coachNote());
        }
        List<String> changes = changelog.compute(baseContentFor(version), content);
        version.setChangelog(json.write(changes));

        events.save(ProgramReviewEvent.builder()
                .program(program).versionId(versionId).actorId(coachId).actorRole("COACH")
                .action(ReviewAction.EDITED)
                .payload(json.write(Map.of("changes", changes.size())))
                .build());

        mirrorToRoadmaps(program, version, content);
        return toCoachVersionDto(version);
    }

    @Override
    public CoachVersionDto newVersionFromActive(Long programId, Long coachId) {
        Program program = programs.findById(programId)
                .orElseThrow(() -> ResourceNotFoundException.of("Program", programId));
        ProgramVersion active = currentVersion(program)
                .orElseThrow(() -> new BusinessRuleException("No active version to clone."));

        // an already-open draft is reused rather than duplicated
        Optional<ProgramVersion> existingDraft = versions
                .findByProgramAndStatusIn(program, List.of(ProgramStatus.DRAFT, ProgramStatus.IN_REVIEW)).stream()
                .max((a, b) -> Integer.compare(a.getVersionNo(), b.getVersionNo()));
        if (existingDraft.isPresent()) {
            return toCoachVersionDto(existingDraft.get());
        }

        int nextVersionNo = versions.findByProgramOrderByVersionNoAsc(program).stream()
                .mapToInt(ProgramVersion::getVersionNo).max().orElse(0) + 1;

        ProgramVersion newVersion = versions.save(ProgramVersion.builder()
                .program(program)
                .versionNo(nextVersionNo)
                .status(ProgramStatus.DRAFT)
                .createdBy(VersionAuthor.COACH)
                .basedOnVersionId(active.getId())
                .generatorSnapshot(active.getGeneratorSnapshot())
                .content(active.getContent())
                .changelog("[]")
                .build());

        events.save(ProgramReviewEvent.builder()
                .program(program).versionId(newVersion.getId()).actorId(coachId).actorRole("COACH")
                .action(ReviewAction.NEW_VERSION)
                .payload(json.write(Map.of("basedOn", active.getVersionNo())))
                .build());

        return toCoachVersionDto(newVersion);
    }


    private Program ownProgram(Long userId) {
        User user = users.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        return currentProgram(user).orElseThrow(() -> new ResourceNotFoundException("No program yet."));
    }

    private List<ChangeFieldDto> parseFields(String fieldsJson) {
        ChangeFieldDto[] parsed = json.read(fieldsJson, ChangeFieldDto[].class);
        return parsed != null ? List.of(parsed) : List.of();
    }

    private List<String> parseChangelog(String changelogJson) {
        String[] parsed = json.read(changelogJson, String[].class);
        return parsed != null ? List.of(parsed) : List.of();
    }

    private ProgramVersionSummaryDto toSummaryDto(ProgramVersion v, Long currentId) {
        return mapper.toSummaryDto(v, parseChangelog(v.getChangelog()), currentId);
    }

    private ProgramVersionDetailDto toDetailDto(ProgramVersion v) {
        PlanContent content = json.read(v.getContent(), PlanContent.class);
        return mapper.toDetailDto(v, content, parseChangelog(v.getChangelog()));
    }

    private PlanContent baseContentFor(ProgramVersion version) {
        if (version.getBasedOnVersionId() != null) {
            Optional<ProgramVersion> base = versions.findById(version.getBasedOnVersionId());
            if (base.isPresent()) {
                return json.read(base.get().getContent(), PlanContent.class);
            }
        }
        return json.read(version.getGeneratorSnapshot(), PlanContent.class);
    }

    private CoachVersionDto toCoachVersionDto(ProgramVersion v) {
        PlanContent content = json.read(v.getContent(), PlanContent.class);
        List<String> liveChangelog = changelog.compute(baseContentFor(v), content);
        return mapper.toCoachVersionDto(v, content, liveChangelog);
    }


    private void mirrorToRoadmaps(Program program, ProgramVersion version, PlanContent content) {
        PlanContent.Metrics m = content.metrics();

        RoadmapStatus rmStatus = switch (version.getStatus()) {
            case ACTIVE -> RoadmapStatus.ACTIVE;
            case SUPERSEDED -> RoadmapStatus.ARCHIVED;
            case COMPLETED -> RoadmapStatus.COMPLETED;
            case DRAFT -> RoadmapStatus.ARCHIVED;
            default -> RoadmapStatus.ACTIVE;
        };
        CoachStatus coachStatus = switch (version.getStatus()) {
            case ACTIVE, SUPERSEDED, COMPLETED -> CoachStatus.APPROVED;
            case CHANGES_REQUESTED -> CoachStatus.NEEDS_REVISION;
            default -> CoachStatus.PENDING_REVIEW;
        };

        String roadmapData = json.write(Map.of(
                "phases", content.phases(),
                "weeklySplit", content.weeklySplit(),
                "mealPlan", content.mealPlan(),
                "guidelines", content.guidelines(),
                "coachConstraints", content.coachConstraints() == null ? List.of() : content.coachConstraints(),
                "analysisSummary", content.analysisSummary() == null ? Map.of() : content.analysisSummary(),
                "versionNo", version.getVersionNo(),
                "changelog", List.of()));

        Roadmap roadmap = roadmaps
                .findFirstByProgramVersionIdIn(versions.findByProgramOrderByVersionNoAsc(program).stream()
                        .map(ProgramVersion::getId).toList())
                .orElseGet(Roadmap::new);

        roadmap.setUser(program.getUser());
        roadmap.setAssessmentId(program.getAssessment() != null ? program.getAssessment().getId() : null);
        roadmap.setAnalysisId(program.getAnalysis() != null ? program.getAnalysis().getId() : null);
        roadmap.setProgramVersionId(version.getId());
        roadmap.setBmi(BigDecimal.valueOf(m.bmi()));
        roadmap.setBmiCategory(m.bmiCategory());
        roadmap.setBmr(m.bmr());
        roadmap.setTdee(m.tdee());
        roadmap.setTargetCalories(m.targetCalories());
        roadmap.setProteinG(m.proteinG());
        roadmap.setCarbsG(m.carbsG());
        roadmap.setFatsG(m.fatsG());
        roadmap.setWaterLiters(BigDecimal.valueOf(m.waterLiters()));
        roadmap.setRoadmapTitle(m.roadmapTitle());
        roadmap.setRoadmapData(roadmapData);
        roadmap.setStatus(rmStatus);
        roadmap.setCoachStatus(coachStatus);
        roadmap.setCoachId(version.getApprovedBy());
        roadmap.setCoachNotes(version.getCoachNote());
        roadmap.setReviewedAt(version.getApprovedAt());

        roadmaps.save(roadmap);
    }
}
