package com.powerfitness.Services.Implimentation;

import com.powerfitness.DTO.AssessmentSubmittedDto;
import com.powerfitness.DTO.MyProgramDto;
import com.powerfitness.DTO.SubmitAssessmentRequest;
import com.powerfitness.Entity.Assessment;
import com.powerfitness.Entity.AssessmentAnalysis;
import com.powerfitness.Entity.GoalFeasibility;
import com.powerfitness.Entity.Program;
import com.powerfitness.Entity.ProgramVersion;
import com.powerfitness.Entity.User;
import com.powerfitness.Exception.ResourceNotFoundException;
import com.powerfitness.Mapper.ProgramMapper;
import com.powerfitness.Repository.AssessmentAnalysisRepository;
import com.powerfitness.Repository.AssessmentRepository;
import com.powerfitness.Repository.UserRepository;
import com.powerfitness.Services.AssessmentAnalyzer;
import com.powerfitness.Services.RoadmapGenerator;
import com.powerfitness.Services.Interface.AssessmentService;
import com.powerfitness.Services.Interface.ProgramService;
import com.powerfitness.common.domain.AnalysisResult;
import com.powerfitness.common.domain.AssessmentInput;
import com.powerfitness.common.domain.PlanContent;
import com.powerfitness.common.util.JsonUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssessmentServiceImpl implements AssessmentService {

    private final UserRepository users;
    private final AssessmentRepository assessments;
    private final AssessmentAnalysisRepository analyses;
    private final ProgramService programService;
    private final ProgramMapper mapper;
    private final JsonUtil json;

    public AssessmentServiceImpl(UserRepository users, AssessmentRepository assessments,
                                AssessmentAnalysisRepository analyses, ProgramService programService,
                                ProgramMapper mapper, JsonUtil json) {
        this.users = users;
        this.assessments = assessments;
        this.analyses = analyses;
        this.programService = programService;
        this.mapper = mapper;
        this.json = json;
    }

    @Override
    public AssessmentSubmittedDto submit(Long userId, SubmitAssessmentRequest r) {
        User user = users.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        int version = assessments.currentVersion(user) + 1;
        assessments.clearLatestFlag(user);

        int age = r.dateOfBirth() != null ? Period.between(r.dateOfBirth(), LocalDate.now()).getYears() : 22;

        Map<String, Object> responses = buildResponses(r);

        Assessment assessment = assessments.save(Assessment.builder()
                .user(user)
                .version(version)
                .latest(true)
                .dateOfBirth(r.dateOfBirth())
                .age(age)
                .gender(coalesce(r.gender(), r.sexAtBirth(), "male"))
                .sexAtBirth(r.sexAtBirth())
                .heightCm(BigDecimal.valueOf(r.heightCm()))
                .weightKg(BigDecimal.valueOf(r.weightKg()))
                .targetWeightKg(BigDecimal.valueOf(r.targetWeightKg()))
                .targetDate(r.targetDate())
                .workoutFrequency(clamp(r.workoutFrequency(), 2, 6, 3))
                .sessionDurationMin(clamp(r.sessionDurationMin(), 20, 120, 60))
                .workoutLocation(deriveLocation(r.equipment()))
                .experienceLevel(coalesce(r.experienceLevel(), "beginner"))
                .yearsTraining(bd(r.yearsTraining()))
                .activityLevel(coalesce(r.activityLevel(), "moderate"))
                .dailySteps(r.dailySteps())
                .occupationType(coalesce(r.occupationType(), "desk"))
                .primaryGoal(coalesce(r.primaryGoal(), "general_fitness"))
                .secondaryGoal(r.secondaryGoal())
                .focusAreas(r.priorityMuscles() != null && !r.priorityMuscles().isEmpty()
                        ? String.join(",", r.priorityMuscles()) : "full_body")
                .mealsPerDay(clamp(r.mealsPerDay(), 2, 6, 3))
                .dietPreference(coalesce(r.dietPreference(), "high_protein"))
                .waterIntakeLiters(bd(r.waterIntakeLiters()))
                .cookingAbility(r.cookingAbility())
                .eatingOutPerWeek(r.eatingOutPerWeek())
                .budgetLevel(r.budgetLevel())
                .caffeineMg(r.caffeineMg())
                .alcoholUnitsWeek(r.alcoholUnitsWeek())
                .sleepHours(r.sleepHours() != null ? r.sleepHours() : 7)
                .sleepQuality(r.sleepQuality())
                .wakeFeeling(r.wakeFeeling())
                .stressLevel(r.stressLevel())
                .energyLevel(r.energyLevel())
                .hasInjuries(r.hasInjuries())
                .pregnant(r.isPregnant())
                .medicalConditions(r.medicalConditions())
                .medications(r.medications())
                .medicalAck(r.medicalAck())
                .trainingStylePref(r.trainingStylePref())
                .coachingTonePref(r.coachingTonePref())
                .checkinFrequency(r.checkinFrequency())
                .contactChannel(r.contactChannel())
                .units(coalesce(r.units(), "metric"))
                .country(r.country())
                .timezone(r.timezone())
                .motivation(r.motivation())
                .successDefinition(r.successDefinition())
                .responses(json.write(responses))
                .build());

        AssessmentInput input = toInput(assessment, r, age);

        AnalysisResult analysisResult = AssessmentAnalyzer.analyze(input);
        AnalysisResult.Metabolic met = analysisResult.metabolic();
        AssessmentAnalysis analysis = analyses.save(AssessmentAnalysis.builder()
                .assessment(assessment)
                .user(user)
                .bmi(BigDecimal.valueOf(met.bmi()))
                .bmiCategory(met.bmiCategory())
                .bmr(met.bmr())
                .tdee(met.tdee())
                .targetCalories(met.targetCalories())
                .proteinG(met.proteinG())
                .carbsG(met.carbsG())
                .fatsG(met.fatsG())
                .bodyfatBand(met.bodyfatBand())
                .goalFeasibility(feasibilityEnum(analysisResult))
                .weeklyRatePct(analysisResult.feasibility() != null && analysisResult.feasibility().weeklyRatePct() != null
                        ? BigDecimal.valueOf(analysisResult.feasibility().weeklyRatePct()) : null)
                .projectedWeeks(analysisResult.feasibility() != null ? analysisResult.feasibility().projectedWeeks() : null)
                .riskCount(analysisResult.limiters().size())
                .dataQualityFlagCount(analysisResult.dataQualityFlags().size())
                .analysis(json.write(analysisResult))
                .build());

        PlanContent content = RoadmapGenerator.generate(input, analysisResult);

        ProgramService.CreateResult created = programService.createFromGeneration(
                user, assessment, analysis, content, assessment.getPrimaryGoal());

        return new AssessmentSubmittedDto(
                assessment.getId(), analysis.getId(), created.programId(), created.versionId(),
                "IN_REVIEW", content.metrics().roadmapTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAssessment(Long userId) {
        User user = users.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        return assessments.existsByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public int assessmentCount(Long userId) {
        User user = users.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        return assessments.currentVersion(user);
    }

    @Override
    @Transactional(readOnly = true)
    public MyProgramDto currentProgram(Long userId) {
        User user = users.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        Optional<Program> programOpt = programService.currentProgram(user);
        if (programOpt.isEmpty()) {
            return MyProgramDto.none();
        }
        Program program = programOpt.get();
        ProgramVersion version = programService.currentVersion(program).orElse(null);
        PlanContent content = version != null ? json.read(version.getContent(), PlanContent.class) : null;

        return mapper.toDto(program, version, content);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<String, Object> latestAssessmentValues(Long userId) {
        User user = users.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        Optional<Assessment> opt = assessments.findByUserAndLatestTrue(user);
        if (opt.isEmpty()) {
            return Map.of();
        }
        Assessment a = opt.get();
        Map<String, Object> values = new LinkedHashMap<>();

        // the long tail (trainingDays, equipment, priorityMuscles, injuries, allergies, ...)
        if (a.getResponses() != null && !a.getResponses().isBlank()) {
            values.putAll(json.read(a.getResponses(), Map.class));
        }

        // typed columns win over the responses blob, same precedence as the PHP row
        putIfNotNull(values, "dateOfBirth", a.getDateOfBirth());
        putIfNotNull(values, "gender", a.getGender());
        putIfNotNull(values, "sexAtBirth", a.getSexAtBirth());
        putIfNotNull(values, "heightCm", a.getHeightCm());
        putIfNotNull(values, "weightKg", a.getWeightKg());
        putIfNotNull(values, "targetWeightKg", a.getTargetWeightKg());
        putIfNotNull(values, "targetDate", a.getTargetDate());
        putIfNotNull(values, "workoutFrequency", a.getWorkoutFrequency());
        putIfNotNull(values, "sessionDurationMin", a.getSessionDurationMin());
        putIfNotNull(values, "workoutLocation", a.getWorkoutLocation());
        putIfNotNull(values, "experienceLevel", a.getExperienceLevel());
        putIfNotNull(values, "activityLevel", a.getActivityLevel());
        putIfNotNull(values, "dailySteps", a.getDailySteps());
        putIfNotNull(values, "occupationType", a.getOccupationType());
        putIfNotNull(values, "primaryGoal", a.getPrimaryGoal());
        putIfNotNull(values, "secondaryGoal", a.getSecondaryGoal());
        putIfNotNull(values, "mealsPerDay", a.getMealsPerDay());
        putIfNotNull(values, "dietPreference", a.getDietPreference());
        putIfNotNull(values, "waterIntakeLiters", a.getWaterIntakeLiters());
        putIfNotNull(values, "cookingAbility", a.getCookingAbility());
        putIfNotNull(values, "eatingOutPerWeek", a.getEatingOutPerWeek());
        putIfNotNull(values, "budgetLevel", a.getBudgetLevel());
        putIfNotNull(values, "sleepHours", a.getSleepHours());
        putIfNotNull(values, "sleepQuality", a.getSleepQuality());
        putIfNotNull(values, "wakeFeeling", a.getWakeFeeling());
        putIfNotNull(values, "stressLevel", a.getStressLevel());
        putIfNotNull(values, "energyLevel", a.getEnergyLevel());
        putIfNotNull(values, "medicalConditions", a.getMedicalConditions());
        putIfNotNull(values, "medications", a.getMedications());
        putIfNotNull(values, "trainingStylePref", a.getTrainingStylePref());
        putIfNotNull(values, "coachingTonePref", a.getCoachingTonePref());
        putIfNotNull(values, "checkinFrequency", a.getCheckinFrequency());
        putIfNotNull(values, "contactChannel", a.getContactChannel());
        putIfNotNull(values, "country", a.getCountry());
        putIfNotNull(values, "motivation", a.getMotivation());
        putIfNotNull(values, "successDefinition", a.getSuccessDefinition());
        return values;
    }

    private static void putIfNotNull(Map<String, Object> values, String key, Object value) {
        if (value != null) {
            values.put(key, value);
        }
    }


    private Map<String, Object> buildResponses(SubmitAssessmentRequest r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("fullName", r.fullName());
        m.put("measurements", nonNullMap(
                "waistCm", r.waistCm(), "hipCm", r.hipCm(), "chestCm", r.chestCm(),
                "armCm", r.armCm(), "thighCm", r.thighCm(), "neckCm", r.neckCm()));
        m.put("bodyfatPct", r.bodyfatPct());
        m.put("weightTrend", r.weightTrend());
        m.put("yearsTraining", r.yearsTraining());
        m.put("currentProgram", r.currentProgram());
        m.put("strength", nonNullMap(
                "squat", r.strengthSquat(), "bench", r.strengthBench(),
                "deadlift", r.strengthDeadlift(), "pullups", r.strengthPullups()));
        m.put("trainingDays", r.trainingDays());
        m.put("equipment", r.equipment());
        m.put("cardioBaseline", r.cardioBaseline());
        m.put("dailySteps", r.dailySteps());
        m.put("secondaryGoal", r.secondaryGoal());
        m.put("targetEvent", r.targetEvent());
        m.put("priorityMuscles", r.priorityMuscles());
        m.put("priorityLifts", r.priorityLifts());
        m.put("successDefinition", r.successDefinition());
        m.put("cookingAbility", r.cookingAbility());
        m.put("budgetLevel", r.budgetLevel());
        m.put("eatingOutPerWeek", r.eatingOutPerWeek());
        m.put("allergies", r.allergies());
        m.put("dislikedFoods", r.dislikedFoods());
        m.put("alcoholUnitsWeek", r.alcoholUnitsWeek());
        m.put("caffeineMg", r.caffeineMg());
        m.put("supplements", r.supplements());
        m.put("sleepQuality", r.sleepQuality());
        m.put("wakeFeeling", r.wakeFeeling());
        m.put("stressLevel", r.stressLevel());
        m.put("energyLevel", r.energyLevel());
        m.put("injuries", r.injuries() == null ? List.of() : r.injuries());
        m.put("medicalConditions", r.medicalConditions());
        m.put("medications", r.medications());
        m.put("isPregnant", r.isPregnant());
        m.put("lovedExercises", r.lovedExercises());
        m.put("hatedExercises", r.hatedExercises());
        m.put("trainingStylePref", r.trainingStylePref());
        m.put("coachingTonePref", r.coachingTonePref());
        m.put("checkinFrequency", r.checkinFrequency());
        m.put("contactChannel", r.contactChannel());
        return m;
    }

    private AssessmentInput toInput(Assessment a, SubmitAssessmentRequest r, int age) {
        List<AssessmentInput.Injury> injuries = new ArrayList<>();
        if (r.injuries() != null) {
            for (var inj : r.injuries()) {
                injuries.add(new AssessmentInput.Injury(
                        inj.bodyPart(), inj.stillPainful(), inj.avoid()));
            }
        }
        List<String> equipment = r.equipment() != null ? r.equipment() : List.of(a.getWorkoutLocation());

        return new AssessmentInput(
                age, r.dateOfBirth(), a.getGender(), a.getSexAtBirth(),
                r.heightCm(), r.weightKg(), r.targetWeightKg(), r.targetDate(),
                a.getWorkoutFrequency(), a.getSessionDurationMin(), a.getWorkoutLocation(),
                a.getExperienceLevel(), toDouble(r.yearsTraining()), a.getActivityLevel(), r.dailySteps(),
                a.getOccupationType(), a.getPrimaryGoal(), a.getFocusAreas(), a.getMealsPerDay(),
                a.getDietPreference(), a.getSleepHours(), r.sleepQuality(), r.stressLevel(), r.energyLevel(),
                r.caffeineMg(), r.alcoholUnitsWeek(), r.cookingAbility(), r.eatingOutPerWeek(), r.budgetLevel(),
                r.isPregnant(), r.medicalConditions(),
                r.strengthSquat(), r.strengthBench(), r.strengthDeadlift(), r.waistCm(),
                equipment, injuries);
    }

    private static GoalFeasibility feasibilityEnum(AnalysisResult result) {
        if (result.feasibility() == null || result.feasibility().verdict() == null) {
            return GoalFeasibility.REALISTIC;
        }
        return switch (result.feasibility().verdict()) {
            case "aggressive" -> GoalFeasibility.AGGRESSIVE;
            case "unrealistic" -> GoalFeasibility.UNREALISTIC;
            case "maintenance" -> GoalFeasibility.MAINTENANCE;
            default -> GoalFeasibility.REALISTIC;
        };
    }

    private static String deriveLocation(List<String> equipment) {
        if (equipment == null || equipment.isEmpty()) return "gym";
        if (equipment.contains("full_gym") || equipment.contains("gym") || equipment.contains("machines")) return "gym";
        if (equipment.contains("bodyweight") || equipment.contains("calisthenics")) return "calisthenics";
        return "home";
    }

    private static Map<String, Object> nonNullMap(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            if (kv[i + 1] != null) m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    private static String coalesce(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return values.length > 0 ? values[values.length - 1] : null;
    }

    private static int clamp(int v, int lo, int hi, int fallback) {
        if (v == 0) return fallback;
        return Math.max(lo, Math.min(hi, v));
    }

    private static BigDecimal bd(Double d) {
        return d == null ? null : BigDecimal.valueOf(d);
    }

    private static Double toDouble(Double d) {
        return d;
    }
}
