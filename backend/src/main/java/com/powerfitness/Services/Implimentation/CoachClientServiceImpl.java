package com.powerfitness.Services.Implimentation;

import com.powerfitness.DTO.AssessmentSummaryDto;
import com.powerfitness.DTO.ClientProfileDto;
import com.powerfitness.DTO.ClientProgressDto;
import com.powerfitness.DTO.ExerciseSummaryDto;
import com.powerfitness.Entity.Assessment;
import com.powerfitness.Entity.AssessmentAnalysis;
import com.powerfitness.Entity.Exercise;
import com.powerfitness.Entity.HabitLog;
import com.powerfitness.Entity.Program;
import com.powerfitness.Entity.ProgramVersion;
import com.powerfitness.Entity.ProgressLog;
import com.powerfitness.Entity.User;
import com.powerfitness.Entity.WorkoutSession;
import com.powerfitness.Exception.ResourceNotFoundException;
import com.powerfitness.Mapper.AssessmentSummaryMapper;
import com.powerfitness.Mapper.ClientProgressMapper;
import com.powerfitness.Mapper.ExerciseMapper;
import com.powerfitness.Repository.AssessmentAnalysisRepository;
import com.powerfitness.Repository.AssessmentRepository;
import com.powerfitness.Repository.ExerciseRepository;
import com.powerfitness.Repository.HabitLogRepository;
import com.powerfitness.Repository.ProgressLogRepository;
import com.powerfitness.Repository.UserRepository;
import com.powerfitness.Repository.WorkoutSessionRepository;
import com.powerfitness.Services.Interface.ClientRiskService;
import com.powerfitness.Services.Interface.CoachClientService;
import com.powerfitness.Services.Interface.ProgramService;
import com.powerfitness.common.domain.AnalysisResult;
import com.powerfitness.common.domain.PlanContent;
import com.powerfitness.common.util.JsonUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CoachClientServiceImpl implements CoachClientService {

    private static final int PROGRESS_WINDOW_DAYS = 30;

    private final UserRepository users;
    private final AssessmentRepository assessments;
    private final AssessmentAnalysisRepository analyses;
    private final ProgressLogRepository progressLogs;
    private final HabitLogRepository habitLogs;
    private final WorkoutSessionRepository workoutSessions;
    private final ExerciseRepository exercises;
    private final ProgramService programService;
    private final ClientRiskService riskService;
    private final JsonUtil json;
    private final ExerciseMapper exerciseMapper;
    private final AssessmentSummaryMapper assessmentSummaryMapper;
    private final ClientProgressMapper progressMapper;

    public CoachClientServiceImpl(UserRepository users, AssessmentRepository assessments,
                                  AssessmentAnalysisRepository analyses, ProgressLogRepository progressLogs,
                                  HabitLogRepository habitLogs, WorkoutSessionRepository workoutSessions,
                                  ExerciseRepository exercises, ProgramService programService,
                                  ClientRiskService riskService, JsonUtil json, ExerciseMapper exerciseMapper,
                                  AssessmentSummaryMapper assessmentSummaryMapper, ClientProgressMapper progressMapper) {
        this.users = users;
        this.assessments = assessments;
        this.analyses = analyses;
        this.progressLogs = progressLogs;
        this.habitLogs = habitLogs;
        this.workoutSessions = workoutSessions;
        this.exercises = exercises;
        this.programService = programService;
        this.riskService = riskService;
        this.json = json;
        this.exerciseMapper = exerciseMapper;
        this.assessmentSummaryMapper = assessmentSummaryMapper;
        this.progressMapper = progressMapper;
    }

    @Override
    public ClientProfileDto profile(Long userId) {
        User user = user(userId);
        Assessment assessment = assessments.findByUserAndLatestTrue(user).orElse(null);
        AnalysisResult analysis = assessment != null
                ? analyses.findByAssessment(assessment).map(a -> json.read(a.getAnalysis(), AnalysisResult.class)).orElse(null)
                : null;

        Optional<Program> programOpt = programService.currentProgram(user);
        Long versionId = null;
        Integer versionNo = null;
        String versionStatus = null;
        PlanContent content = null;
        if (programOpt.isPresent()) {
            Optional<ProgramVersion> versionOpt = programService.currentVersion(programOpt.get());
            if (versionOpt.isPresent()) {
                ProgramVersion v = versionOpt.get();
                versionId = v.getId();
                versionNo = v.getVersionNo();
                versionStatus = v.getStatus().name();
                content = json.read(v.getContent(), PlanContent.class);
            }
        }

        var risk = riskService.assess(user);

        return new ClientProfileDto(
                user.getId(), user.getName(), user.getEmail(), user.getCreatedAt(),
                assessment != null ? toSummary(assessment) : null,
                analysis, versionId, versionNo, versionStatus, content,
                risk.atRisk(), risk.reasons());
    }

    @Override
    public ClientProgressDto progress(Long userId) {
        User user = user(userId);
        LocalDate from = LocalDate.now().minusDays(PROGRESS_WINDOW_DAYS);
        LocalDate today = LocalDate.now();

        List<HabitLog> logs = habitLogs.findByUserAndLogDateBetweenOrderByLogDateAsc(user, from, today);
        int workoutCount = (int) logs.stream().filter(HabitLog::isWorkoutCompleted).count();
        int nutritionCount = (int) logs.stream().filter(HabitLog::isNutritionCompleted).count();
        int waterCount = (int) logs.stream().filter(HabitLog::isWaterCompleted).count();
        List<Integer> sleepHours = logs.stream().map(HabitLog::getSleepHours).filter(h -> h != null && h > 0).toList();
        int totalDays = logs.size();

        int workoutPct = totalDays > 0 ? Math.round(workoutCount * 100f / totalDays) : 0;
        int nutritionPct = totalDays > 0 ? Math.round(nutritionCount * 100f / totalDays) : 0;
        int waterPct = totalDays > 0 ? Math.round(waterCount * 100f / totalDays) : 0;
        double avgSleep = sleepHours.isEmpty() ? 0
                : Math.round(sleepHours.stream().mapToInt(Integer::intValue).average().orElse(0) * 10) / 10.0;

        List<ProgressLog> weightRows = progressLogs.findByUserAndLoggedOnGreaterThanEqualOrderByLoggedOnAsc(user, from);
        double weightChange = 0;
        if (weightRows.size() > 1) {
            BigDecimal first = weightRows.get(0).getWeightKg();
            BigDecimal last = weightRows.get(weightRows.size() - 1).getWeightKg();
            if (first != null && last != null) {
                weightChange = Math.round(last.subtract(first).doubleValue() * 10) / 10.0;
            }
        }

        List<WorkoutSession> workoutRows = workoutSessions
                .findByUserAndSessionDateGreaterThanEqualOrderBySessionDateDesc(user, from).stream()
                .limit(15)
                .toList();

        List<ClientProgressDto.WeightLog> weightLogs = progressMapper.toWeightLogs(weightRows);

        List<ClientProgressDto.WorkoutLog> workoutLogs = progressMapper.toWorkoutLogs(workoutRows);

        Map<LocalDate, HabitLog> byDate = new java.util.HashMap<>();
        for (HabitLog l : logs) {
            byDate.put(l.getLogDate(), l);
        }
        List<ClientProgressDto.HabitDay> matrix = new ArrayList<>();
        for (int i = PROGRESS_WINDOW_DAYS - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            HabitLog l = byDate.get(d);
            matrix.add(new ClientProgressDto.HabitDay(
                    d.toString(),
                    l != null && l.isWaterCompleted(),
                    l != null && l.isWorkoutCompleted(),
                    l != null && l.isNutritionCompleted(),
                    l != null && l.getSleepHours() != null && l.getSleepHours() > 0));
        }

        ClientProgressDto.Stats stats = new ClientProgressDto.Stats(
                workoutPct, nutritionPct, waterPct, avgSleep, weightChange, workoutRows.size(), totalDays);

        var risk = riskService.assess(user);

        Integer daysSinceLastWeighIn = progressLogs.findFirstByUserAndWeightKgIsNotNullOrderByLoggedOnDesc(user)
                .map(p -> (int) ChronoUnit.DAYS.between(p.getLoggedOn(), today))
                .orElse(null);

        List<WorkoutSession> allWorkouts = workoutSessions.findByUserOrderBySessionDateDescIdDesc(user);
        Integer daysSinceLastWorkout = allWorkouts.isEmpty() ? null
                : (int) ChronoUnit.DAYS.between(allWorkouts.get(0).getSessionDate(), today);

        Set<LocalDate> activeDates = logs.stream()
                .filter(l -> l.isWorkoutCompleted() || l.isNutritionCompleted() || l.isWaterCompleted() || l.isSleepCompleted())
                .map(HabitLog::getLogDate)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        int streak = 0;
        LocalDate cursor = activeDates.contains(today) ? today : today.minusDays(1);
        while (activeDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }

        return new ClientProgressDto(user.getId(), user.getName(), user.getEmail(), stats, weightLogs, workoutLogs,
                matrix, risk.atRisk(), risk.reasons(), daysSinceLastWeighIn, daysSinceLastWorkout, streak);
    }

    @Override
    public List<ExerciseSummaryDto> safeExercisesFor(Long userId) {
        User user = user(userId);
        Assessment assessment = assessments.findByUserAndLatestTrue(user).orElse(null);
        AnalysisResult analysis = assessment != null
                ? analyses.findByAssessment(assessment).map(a -> json.read(a.getAnalysis(), AnalysisResult.class)).orElse(null)
                : null;

        Set<String> excludedPatterns = analysis != null && analysis.contraindications() != null
                && analysis.contraindications().excludedPatterns() != null
                ? Set.copyOf(analysis.contraindications().excludedPatterns())
                : Set.of();
        String equipmentMode = analysis != null && analysis.contraindications() != null
                && analysis.contraindications().equipmentMode() != null
                ? analysis.contraindications().equipmentMode()
                : "gym";

        return exercises.findAllByOrderByCategoryAscNameAsc().stream()
                .filter(e -> safeFor(e, excludedPatterns, equipmentMode))
                .map(exerciseMapper::toDto)
                .toList();
    }

    private boolean safeFor(Exercise e, Set<String> excludedPatterns, String equipmentMode) {
        if (!excludedPatterns.isEmpty() && e.getPatterns() != null) {
            List<String> patterns = Arrays.stream(e.getPatterns().split(",")).map(String::trim).toList();
            if (patterns.stream().anyMatch(excludedPatterns::contains)) {
                return false;
            }
        }
        String eq = e.getEquipment();
        return switch (equipmentMode) {
            case "bands" -> "bands".equals(eq) || "bodyweight".equals(eq);
            case "bodyweight" -> "bodyweight".equals(eq);
            case "home" -> !List.of("barbell", "machine", "cable").contains(eq);
            default -> true;
        };
    }

    @SuppressWarnings("unchecked")
    private AssessmentSummaryDto toSummary(Assessment a) {
        Map<String, Object> responses = a.getResponses() != null && !a.getResponses().isBlank()
                ? json.read(a.getResponses(), Map.class) : Map.of();

        List<String> equipment = asStringList(responses.get("equipment"));
        List<AssessmentSummaryDto.InjuryDto> injuries = new ArrayList<>();
        Object rawInjuries = responses.get("injuries");
        if (rawInjuries instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> m) {
                    injuries.add(new AssessmentSummaryDto.InjuryDto(
                            String.valueOf(m.get("bodyPart")),
                            Boolean.TRUE.equals(m.get("stillPainful")),
                            m.get("avoid") != null ? String.valueOf(m.get("avoid")) : null));
                }
            }
        }

        return assessmentSummaryMapper.toDto(a, equipment, injuries,
                stringOrNull(responses.get("allergies")), stringOrNull(responses.get("dislikedFoods")),
                stringOrNull(responses.get("lovedExercises")), stringOrNull(responses.get("hatedExercises")));
    }

    private static List<String> asStringList(Object raw) {
        if (raw instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private static String stringOrNull(Object raw) {
        return raw != null ? String.valueOf(raw) : null;
    }

    private User user(Long id) {
        return users.findById(id).orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }
}
