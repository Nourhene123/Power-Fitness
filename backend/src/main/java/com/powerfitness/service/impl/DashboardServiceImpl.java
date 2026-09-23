package com.powerfitness.service.impl;

import com.powerfitness.dto.ChangeFieldDto;
import com.powerfitness.dto.DashboardDto;
import com.powerfitness.entity.Assessment;
import com.powerfitness.entity.HabitLog;
import com.powerfitness.entity.Program;
import com.powerfitness.entity.ProgramChangeRequest;
import com.powerfitness.entity.ProgramStatus;
import com.powerfitness.entity.ProgramVersion;
import com.powerfitness.entity.ProgressLog;
import com.powerfitness.entity.User;
import com.powerfitness.exception.ResourceNotFoundException;
import com.powerfitness.repository.AssessmentRepository;
import com.powerfitness.repository.HabitLogRepository;
import com.powerfitness.repository.ProgressLogRepository;
import com.powerfitness.repository.UserRepository;
import com.powerfitness.repository.WorkoutSessionRepository;
import com.powerfitness.service.DashboardService;
import com.powerfitness.service.NotificationService;
import com.powerfitness.service.ProgramService;
import com.powerfitness.common.domain.PlanContent;
import com.powerfitness.common.util.JsonUtil;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final List<String> WEEK_DAYS = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
    private static final Pattern WEEKS_RANGE = Pattern.compile("(\\d+)\\s*-\\s*(\\d+)");
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);

    private final UserRepository users;
    private final AssessmentRepository assessments;
    private final ProgressLogRepository progressLogs;
    private final HabitLogRepository habitLogs;
    private final WorkoutSessionRepository workoutSessions;
    private final ProgramService programService;
    private final NotificationService notificationService;
    private final JsonUtil json;

    public DashboardServiceImpl(UserRepository users, AssessmentRepository assessments,
                                ProgressLogRepository progressLogs, HabitLogRepository habitLogs,
                                WorkoutSessionRepository workoutSessions, ProgramService programService,
                                NotificationService notificationService, JsonUtil json) {
        this.users = users;
        this.assessments = assessments;
        this.progressLogs = progressLogs;
        this.habitLogs = habitLogs;
        this.workoutSessions = workoutSessions;
        this.programService = programService;
        this.notificationService = notificationService;
        this.json = json;
    }

    @Override
    public DashboardDto build(Long userId) {
        User user = users.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        Optional<Assessment> assessmentOpt = assessments.findByUserAndLatestTrue(user);
        boolean hasAssessment = assessmentOpt.isPresent();

        Optional<Program> programOpt = programService.currentProgram(user);
        if (programOpt.isEmpty()) {
            return DashboardDto.none(hasAssessment);
        }
        Program program = programOpt.get();
        Optional<ProgramVersion> versionOpt = programService.currentVersion(program);

        if (program.getStatus() == ProgramStatus.CHANGES_REQUESTED) {
            return changesRequestedState(program, versionOpt.orElse(null), hasAssessment);
        }
        if (versionOpt.isEmpty() || versionOpt.get().getStatus() != ProgramStatus.ACTIVE) {
            return inReviewState(versionOpt.orElse(null), hasAssessment);
        }
        if (assessmentOpt.isEmpty()) {
            return inReviewState(versionOpt.orElse(null), hasAssessment);
        }

        ProgramVersion version = versionOpt.get();
        Assessment assessment = assessmentOpt.get();
        PlanContent content = json.read(version.getContent(), PlanContent.class);
        int weekNo = weekNoOf(program.getStartDate());

        return new DashboardDto(
                "ACTIVE", true, null, null,
                goal(user, assessment),
                phase(program, version, content.phases(), weekNo, content.metrics().roadmapTitle()),
                today(user, content.weeklySplit(), assessment, content.metrics()),
                progress(user, assessment),
                coach(user, version),
                milestone(program, content.phases(), weekNo));
    }

    private DashboardDto changesRequestedState(Program program, ProgramVersion version, boolean hasAssessment) {
        String note = version != null ? version.getCoachNote() : null;
        List<ChangeFieldDto> fields = List.of();
        Optional<ProgramChangeRequest> cr = programService.openChangeRequest(program);
        if (cr.isPresent()) {
            ChangeFieldDto[] parsed = json.read(cr.get().getFields(), ChangeFieldDto[].class);
            fields = parsed != null ? List.of(parsed) : List.of();
        }
        return new DashboardDto("CHANGES_REQUESTED", hasAssessment,
                new DashboardDto.ChangeRequestVm(program.getId(), note, fields),
                null, null, null, null, null, null, null);
    }

    private DashboardDto inReviewState(ProgramVersion version, boolean hasAssessment) {
        DashboardDto.PreviewVm preview = null;
        if (version != null) {
            PlanContent content = json.read(version.getContent(), PlanContent.class);
            if (content != null && content.metrics() != null) {
                Integer splitDays = content.weeklySplit() != null ? content.weeklySplit().size() : null;
                preview = new DashboardDto.PreviewVm(content.metrics().targetCalories(), splitDays);
            }
        }
        return new DashboardDto("IN_REVIEW", hasAssessment, null, preview, null, null, null, null, null, null);
    }

    private DashboardDto.GoalVm goal(User user, Assessment a) {
        double start = a.getWeightKg() != null ? a.getWeightKg().doubleValue() : 0;
        double target = a.getTargetWeightKg() != null ? a.getTargetWeightKg().doubleValue() : start;
        double current = progressLogs.findFirstByUserAndWeightKgIsNotNullOrderByLoggedOnDesc(user)
                .map(ProgressLog::getWeightKg).map(BigDecimal::doubleValue).orElse(start);

        double totalDelta = target - start;
        double doneDelta = current - start;
        String dir = Math.abs(totalDelta) < 0.5 ? "maintain" : (totalDelta < 0 ? "lose" : "gain");
        double toGo = round1(target - current);
        int pct = Math.abs(totalDelta) < 0.1 ? 100
                : (int) Math.max(0, Math.min(100, Math.round((doneDelta / totalDelta) * 100)));
        if (!dir.equals("maintain") && ((totalDelta < 0 && doneDelta > 0) || (totalDelta > 0 && doneDelta < 0))) {
            pct = 0;
        }

        LocalDate targetDate = a.getTargetDate();
        Boolean onTrack = null;
        String line;
        if (dir.equals("maintain")) {
            line = "Maintain ~" + fmt(round1(target)) + " kg · recomposition";
        } else {
            String verb = dir.equals("lose") ? "Lose" : "Gain";
            double amount = Math.abs(round1(totalDelta));
            line = verb + " " + fmt(amount) + " kg";
            LocalDate today = LocalDate.now();
            if (targetDate != null && targetDate.isAfter(today)) {
                long weeksLeft = Math.max(1, Math.round(ChronoUnit.DAYS.between(today, targetDate) / 7.0));
                double needPerWeek = Math.abs(toGo) / weeksLeft;
                double safe = (dir.equals("lose") ? 0.01 : 0.005) * current;
                onTrack = needPerWeek <= safe * 1.25;
                line += " by " + targetDate.format(SHORT_DATE);
            } else if (targetDate != null) {
                line += " by " + targetDate.format(SHORT_DATE) + " (past)";
                onTrack = false;
            }
        }

        return new DashboardDto.GoalVm(line, onTrack, start, current, target, toGo, pct, dir);
    }

    private DashboardDto.PhaseVm phase(Program program, ProgramVersion version, List<PlanContent.Phase> phases,
                                       int weekNo, String title) {
        String name = (title != null && !title.isBlank()) ? title
                : "Roadmap · " + titleCase(program.getPrimaryGoal().replace('_', ' '));
        PlanContent.Phase current = null;
        if (phases != null) {
            for (PlanContent.Phase p : phases) {
                Matcher m = WEEKS_RANGE.matcher(p.weeks() == null ? "" : p.weeks());
                if (m.find()) {
                    int from = Integer.parseInt(m.group(1));
                    int to = Integer.parseInt(m.group(2));
                    if (weekNo >= from && weekNo <= to) {
                        current = p;
                        break;
                    }
                }
            }
        }
        String phaseLabel = current != null ? current.title() : ("Week " + weekNo);
        String phaseShort = current != null ? ("Phase " + current.phase()) : null;
        return new DashboardDto.PhaseVm(name, version.getVersionNo(), weekNo, 12, phaseLabel, phaseShort);
    }

    private DashboardDto.TodayVm today(User user, List<PlanContent.TrainingDay> split, Assessment a,
                                       PlanContent.Metrics metrics) {
        List<String> trainingDays = trainingDaysOf(a);
        DayOfWeek dow = LocalDate.now().getDayOfWeek();
        String todayAbbr = dow.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

        Integer sessionIdx = null;
        PlanContent.TrainingDay session = null;
        if (split != null && !split.isEmpty()) {
            if (!trainingDays.isEmpty() && trainingDays.contains(todayAbbr)) {
                List<String> ordered = WEEK_DAYS.stream().filter(trainingDays::contains).toList();
                int pos = ordered.indexOf(todayAbbr);
                if (pos >= 0 && pos < split.size()) {
                    sessionIdx = pos;
                    session = split.get(pos);
                }
            } else if (trainingDays.isEmpty()) {
                Map<Integer, Integer> byIsoDow = Map.of(1, 0, 3, 1, 5, 2, 6, 3, 7, 4);
                Integer idx = byIsoDow.get(dow.getValue());
                if (idx != null && idx < split.size()) {
                    sessionIdx = idx;
                    session = split.get(idx);
                }
            }
        }

        boolean doneToday = session != null && workoutSessions.existsByUserAndSessionDate(user, LocalDate.now());
        HabitLog habit = habitLogs.findByUserAndLogDate(user, LocalDate.now()).orElse(null);

        DashboardDto.TodayVm.SessionVm sessionVm = session == null ? null
                : new DashboardDto.TodayVm.SessionVm(
                        session.title() != null ? session.title() : session.day(),
                        session.exercises() != null ? session.exercises().size() : 0,
                        session.duration());

        DashboardDto.TodayVm.HabitsVm habits = new DashboardDto.TodayVm.HabitsVm(
                habit != null && habit.isWaterCompleted(),
                habit != null && habit.isWorkoutCompleted(),
                habit != null && habit.isNutritionCompleted(),
                habit != null && habit.isSleepCompleted());

        return new DashboardDto.TodayVm(session == null, sessionIdx, sessionVm, doneToday,
                metrics.targetCalories(), metrics.proteinG(), metrics.carbsG(), metrics.fatsG(),
                metrics.waterLiters(), habits);
    }

    private DashboardDto.ProgressVm progress(User user, Assessment a) {
        LocalDate cutoff = LocalDate.now().minusDays(120);
        List<DashboardDto.ProgressVm.WeightPointVm> series = progressLogs
                .findByUserAndLoggedOnGreaterThanEqualOrderByLoggedOnAsc(user, cutoff).stream()
                .filter(p -> p.getWeightKg() != null)
                .map(p -> new DashboardDto.ProgressVm.WeightPointVm(p.getLoggedOn().toString(), p.getWeightKg().doubleValue()))
                .toList();

        List<DashboardDto.ProgressVm.WeekAdherenceVm> weeks = new ArrayList<>();
        LocalDate thisMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (int i = 3; i >= 0; i--) {
            LocalDate from = thisMonday.minusWeeks(i);
            LocalDate to = from.plusDays(6);
            List<HabitLog> week = habitLogs.findByUserAndLogDateBetweenOrderByLogDateAsc(user, from, to);
            long w = week.stream().filter(HabitLog::isWorkoutCompleted).count();
            long n = week.stream().filter(HabitLog::isNutritionCompleted).count();
            long h = week.stream().filter(HabitLog::isWaterCompleted).count();
            int days = Math.max(1, week.size());
            int adherencePct = (int) Math.round(((w + n + h) / (double) (days * 3)) * 100);
            weeks.add(new DashboardDto.ProgressVm.WeekAdherenceVm(from.format(SHORT_DATE), (int) w, adherencePct));
        }

        Set<LocalDate> activeDates = habitLogs.findByUserAndLogDateBetweenOrderByLogDateAsc(user, cutoff, LocalDate.now())
                .stream()
                .filter(l -> l.isWorkoutCompleted() || l.isNutritionCompleted() || l.isWaterCompleted() || l.isSleepCompleted())
                .map(HabitLog::getLogDate)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        int streak = 0;
        LocalDate cursor = activeDates.contains(LocalDate.now()) ? LocalDate.now() : LocalDate.now().minusDays(1);
        while (activeDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }

        double target = a.getTargetWeightKg() != null ? a.getTargetWeightKg().doubleValue() : 0;
        return new DashboardDto.ProgressVm(series, weeks, streak, target - 1, target + 1);
    }

    private DashboardDto.CoachVm coach(User user, ProgramVersion version) {
        long unread = notificationService.unreadCount(user.getId());
        String focusNote = version.getFocusNote() != null ? version.getFocusNote().trim() : "";
        String focus = !focusNote.isEmpty() ? focusNote : systemFocus(user);
        String note = version.getCoachNote() != null ? version.getCoachNote().trim() : "";
        return new DashboardDto.CoachVm(note, focus, !focusNote.isEmpty() ? "coach" : "system", unread);
    }

    private String systemFocus(User user) {
        LocalDate from = LocalDate.now().minusDays(7);
        List<HabitLog> logs = habitLogs.findByUserAndLogDateBetweenOrderByLogDateAsc(user, from, LocalDate.now());
        if (logs.isEmpty()) return "No check-ins logged yet this week — tap the habits above each evening.";
        int d = Math.max(1, logs.size());
        long w = logs.stream().filter(HabitLog::isWorkoutCompleted).count();
        long n = logs.stream().filter(HabitLog::isNutritionCompleted).count();
        long h = logs.stream().filter(HabitLog::isWaterCompleted).count();
        if (w / (double) d < 0.5) return "Only " + w + " of your planned sessions logged in the last 7 days. Protect your training slots.";
        if (n / (double) d < 0.6) return "Nutrition on target " + n + "/7 days last week — prep one meal ahead to lift that number.";
        if (h / (double) d < 0.6) return "Hydration is the low one this week (" + h + "/7). Keep a bottle on your desk.";
        return "Solid week — everything above 60%. Keep the streak going.";
    }

    private DashboardDto.MilestoneVm milestone(Program program, List<PlanContent.Phase> phases, int weekNo) {
        LocalDate startDate = program.getStartDate() != null ? program.getStartDate() : LocalDate.now();
        LocalDate today = LocalDate.now();
        record Candidate(LocalDate date, String label) {}
        List<Candidate> candidates = new ArrayList<>();

        if (phases != null) {
            for (PlanContent.Phase p : phases) {
                Matcher m = WEEKS_RANGE.matcher(p.weeks() == null ? "" : p.weeks());
                if (m.find()) {
                    int startWk = Integer.parseInt(m.group(1));
                    if (startWk > weekNo) {
                        candidates.add(new Candidate(startDate.plusWeeks(startWk - 1L), (p.title() != null ? p.title() : "Phase") + " starts"));
                    }
                }
            }
        }
        if (program.getNextCheckinDate() != null && program.getNextCheckinDate().isAfter(today)) {
            candidates.add(new Candidate(program.getNextCheckinDate(), "Mid-program check-in & photos"));
        }
        LocalDate end = startDate.plusWeeks(12);
        if (end.isAfter(today)) {
            candidates.add(new Candidate(end, "Program complete — final review"));
        }

        if (candidates.isEmpty()) return new DashboardDto.MilestoneVm(null, null, null);
        Candidate next = candidates.stream().min((c1, c2) -> c1.date().compareTo(c2.date())).orElseThrow();
        int inDays = (int) Math.max(0, ChronoUnit.DAYS.between(today, next.date()));
        return new DashboardDto.MilestoneVm(next.label(), next.date().format(SHORT_DATE), inDays);
    }

    @SuppressWarnings("unchecked")
    private List<String> trainingDaysOf(Assessment a) {
        if (a.getResponses() == null || a.getResponses().isBlank()) return List.of();
        Map<String, Object> resp = json.read(a.getResponses(), Map.class);
        Object raw = resp.get("trainingDays");
        if (!(raw instanceof List<?> list)) return List.of();
        return list.stream().map(String::valueOf).toList();
    }

    private static int weekNoOf(LocalDate startDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now();
        long daysSince = ChronoUnit.DAYS.between(start, LocalDate.now());
        int weekNo = (int) Math.floor(daysSince / 7.0) + 1;
        return Math.max(1, Math.min(12, weekNo));
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }

    private static String fmt(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    private static String titleCase(String s) {
        StringBuilder sb = new StringBuilder();
        for (String word : s.split(" ")) {
            if (word.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }
}
