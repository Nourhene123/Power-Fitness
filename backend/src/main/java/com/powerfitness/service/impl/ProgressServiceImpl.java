package com.powerfitness.service.impl;

import com.powerfitness.dto.HabitToggleResult;
import com.powerfitness.dto.LogWeighInRequest;
import com.powerfitness.dto.LogWorkoutRequest;
import com.powerfitness.dto.ProgressLogDto;
import com.powerfitness.dto.TimelineItemDto;
import com.powerfitness.dto.WorkoutSessionDto;
import com.powerfitness.entity.HabitLog;
import com.powerfitness.entity.Program;
import com.powerfitness.entity.ProgramReviewEvent;
import com.powerfitness.entity.ProgramVersion;
import com.powerfitness.entity.ProgressLog;
import com.powerfitness.entity.ReviewAction;
import com.powerfitness.entity.User;
import com.powerfitness.entity.WorkoutSession;
import com.powerfitness.exception.BusinessRuleException;
import com.powerfitness.exception.ResourceNotFoundException;
import com.powerfitness.mapper.ProgressMapper;
import com.powerfitness.repository.HabitLogRepository;
import com.powerfitness.repository.ProgramReviewEventRepository;
import com.powerfitness.repository.ProgressLogRepository;
import com.powerfitness.repository.UserRepository;
import com.powerfitness.repository.WorkoutSessionRepository;
import com.powerfitness.service.ProgramService;
import com.powerfitness.service.ProgressService;
import com.powerfitness.common.domain.PlanContent;
import com.powerfitness.common.util.JsonUtil;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class ProgressServiceImpl implements ProgressService {

    private static final Map<String, String> REVIEW_LABELS = Map.of(
            "SUBMITTED", "Assessment submitted",
            "APPROVED", "Plan approved by coach",
            "EDITED", "Coach edited the plan",
            "CHANGES_REQUESTED", "Coach requested changes",
            "RESUBMITTED", "You resubmitted",
            "NEW_VERSION", "Coach started a new version");

    private final UserRepository users;
    private final ProgressLogRepository progressLogs;
    private final WorkoutSessionRepository workoutSessions;
    private final HabitLogRepository habitLogs;
    private final ProgramReviewEventRepository reviewEvents;
    private final ProgramService programService;
    private final ProgressMapper mapper;
    private final JsonUtil json;

    public ProgressServiceImpl(UserRepository users, ProgressLogRepository progressLogs,
                               WorkoutSessionRepository workoutSessions, HabitLogRepository habitLogs,
                               ProgramReviewEventRepository reviewEvents, ProgramService programService,
                               ProgressMapper mapper, JsonUtil json) {
        this.users = users;
        this.progressLogs = progressLogs;
        this.workoutSessions = workoutSessions;
        this.habitLogs = habitLogs;
        this.reviewEvents = reviewEvents;
        this.programService = programService;
        this.mapper = mapper;
        this.json = json;
    }

    @Override
    public WorkoutSessionDto logWorkout(Long userId, LogWorkoutRequest body) {
        User user = userOf(userId);
        LocalDate date = body.sessionDate() != null ? body.sessionDate() : LocalDate.now();
        if (date.isAfter(LocalDate.now().plusDays(1))) {
            throw new BusinessRuleException("That date is in the future.");
        }
        int dayIndex = body.dayIndex() != null ? Math.max(0, body.dayIndex()) : 0;
        String label = dayLabelOf(user, dayIndex);

        WorkoutSession session = workoutSessions.save(WorkoutSession.builder()
                .user(user)
                .dayIndex(dayIndex)
                .dayLabel(label)
                .sessionDate(date)
                .durationMin(positiveOrNull(body.durationMin()))
                .rpe(positiveOrNull(body.rpe()))
                .note(blankToNull(body.note()))
                .build());

        HabitLog habit = habitLogs.findByUserAndLogDate(user, date)
                .orElseGet(() -> HabitLog.builder().user(user).logDate(date).build());
        habit.setWorkoutCompleted(true);
        habitLogs.save(habit);

        return mapper.toDto(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutSessionDto> recentWorkouts(Long userId) {
        User user = userOf(userId);
        return workoutSessions.findByUserOrderBySessionDateDescIdDesc(user).stream()
                .limit(10)
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public ProgressLogDto logWeighIn(Long userId, LogWeighInRequest body) {
        User user = userOf(userId);
        LocalDate date = body.loggedOn() != null ? body.loggedOn() : LocalDate.now();
        if (date.isAfter(LocalDate.now().plusDays(1))) {
            throw new BusinessRuleException("That date is in the future.");
        }
        if (body.weightKg() != null && (body.weightKg() < 30 || body.weightKg() > 300)) {
            throw new BusinessRuleException("Enter a realistic weight (30-300 kg).");
        }

        ProgressLog log = progressLogs.findByUserAndLoggedOn(user, date)
                .orElseGet(() -> ProgressLog.builder().user(user).loggedOn(date).build());
        log.setWeightKg(toDecimal(body.weightKg()));
        log.setWaistCm(toDecimal(body.waistCm()));
        log.setHipCm(toDecimal(body.hipCm()));
        log.setChestCm(toDecimal(body.chestCm()));
        log.setArmCm(toDecimal(body.armCm()));
        log.setThighCm(toDecimal(body.thighCm()));
        log.setNeckCm(toDecimal(body.neckCm()));
        log.setEnergy(body.energy());
        log.setNote(blankToNull(body.note()));

        return mapper.toDto(progressLogs.save(log));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgressLogDto> recentWeighIns(Long userId) {
        User user = userOf(userId);
        return progressLogs.findByUserOrderByLoggedOnDesc(user).stream()
                .limit(20)
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public HabitToggleResult toggleHabit(Long userId, String habit) {
        User user = userOf(userId);
        LocalDate today = LocalDate.now();
        HabitLog log = habitLogs.findByUserAndLogDate(user, today)
                .orElseGet(() -> HabitLog.builder().user(user).logDate(today).build());

        boolean newValue = switch (habit) {
            case "water" -> flip(log.isWaterCompleted(), log::setWaterCompleted);
            case "workout" -> flip(log.isWorkoutCompleted(), log::setWorkoutCompleted);
            case "nutrition" -> flip(log.isNutritionCompleted(), log::setNutritionCompleted);
            case "sleep" -> flip(log.isSleepCompleted(), log::setSleepCompleted);
            default -> throw new BusinessRuleException("Invalid habit key.");
        };
        habitLogs.save(log);
        return new HabitToggleResult(newValue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimelineItemDto> timeline(Long userId) {
        User user = userOf(userId);
        record Item(Instant ts, TimelineItemDto dto) {}
        List<Item> items = new ArrayList<>();

        progressLogs.findByUserOrderByLoggedOnDesc(user).stream()
                .filter(p -> p.getWeightKg() != null)
                .limit(40)
                .forEach(p -> {
                    String text = fmt1(p.getWeightKg()) + " kg"
                            + (p.getNote() != null && !p.getNote().isBlank() ? " — " + p.getNote() : "");
                    items.add(new Item(p.getLoggedOn().atStartOfDay(ZoneOffset.UTC).toInstant(),
                            new TimelineItemDto(p.getLoggedOn().toString(), "weigh", text)));
                });

        workoutSessions.findByUserOrderBySessionDateDescIdDesc(user).stream()
                .limit(40)
                .forEach(s -> {
                    String text = s.getDayLabel()
                            + (s.getDurationMin() != null ? " · " + s.getDurationMin() + " min" : "")
                            + (s.getRpe() != null ? " · RPE " + s.getRpe() : "");
                    items.add(new Item(s.getSessionDate().atStartOfDay(ZoneOffset.UTC).toInstant(),
                            new TimelineItemDto(s.getSessionDate().toString(), "workout", text)));
                });

        programService.currentProgram(user).ifPresent(program ->
                reviewEvents.findByProgramOrderByCreatedAtDesc(program).stream()
                        .limit(30)
                        .forEach(e -> {
                            String label = REVIEW_LABELS.get(e.getAction().name());
                            if (label != null) {
                                items.add(new Item(e.getCreatedAt(),
                                        new TimelineItemDto(
                                                e.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate().toString(),
                                                "plan", label)));
                            }
                        }));

        return items.stream()
                .sorted((a, b) -> b.ts().compareTo(a.ts()))
                .map(Item::dto)
                .toList();
    }

    /* -------- helpers -------- */

    private User userOf(Long userId) {
        return users.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }

    private String dayLabelOf(User user, int dayIndex) {
        Optional<Program> program = programService.currentProgram(user);
        if (program.isEmpty()) return "Session";
        Optional<ProgramVersion> version = programService.currentVersion(program.get());
        if (version.isEmpty() || version.get().getContent() == null) return "Session";
        PlanContent content = json.read(version.get().getContent(), PlanContent.class);
        if (content == null || content.weeklySplit() == null || dayIndex >= content.weeklySplit().size()) {
            return "Session";
        }
        PlanContent.TrainingDay day = content.weeklySplit().get(dayIndex);
        if (day.title() != null && !day.title().isBlank()) return day.title();
        if (day.day() != null && !day.day().isBlank()) return day.day();
        return "Session";
    }

    private boolean flip(boolean current, java.util.function.Consumer<Boolean> setter) {
        boolean next = !current;
        setter.accept(next);
        return next;
    }

    private static Integer positiveOrNull(Integer v) {
        return (v != null && v > 0) ? v : null;
    }

    private static String blankToNull(String s) {
        return (s != null && !s.trim().isEmpty()) ? s.trim() : null;
    }

    private static BigDecimal toDecimal(Double v) {
        return v != null ? BigDecimal.valueOf(v) : null;
    }

    private static String fmt1(BigDecimal v) {
        return v.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString();
    }

}
