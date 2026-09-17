package com.powerfitness.Services;

import static com.powerfitness.support.AssessmentInputBuilder.builder;
import static org.assertj.core.api.Assertions.assertThat;

import com.powerfitness.common.domain.AnalysisResult;
import com.powerfitness.common.domain.AnalysisResult.Contraindications;
import com.powerfitness.common.domain.AnalysisResult.Feasibility;
import com.powerfitness.common.domain.AnalysisResult.Metabolic;
import com.powerfitness.common.domain.AssessmentInput;
import com.powerfitness.common.domain.PlanContent;
import com.powerfitness.common.domain.PlanContent.PlannedExercise;
import com.powerfitness.common.domain.PlanContent.TrainingDay;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoadmapGeneratorTest {

    @Test
    void generatesWithoutAnalysisByComputingItsOwnMetabolicNumbers() {
        AssessmentInput input = builder().sexAtBirth("male").age(30).heightCm(180).weightKg(80)
                .activityLevel("sedentary").build();

        PlanContent plan = RoadmapGenerator.generate(input, null);

        assertThat(plan.metrics().bmr()).isEqualTo(1780);
        assertThat(plan.analysisSummary()).isNull();
    }

    @Test
    void analysisMetabolicNumbersOverrideTheLocallyComputedOnesWhenPresent() {
        AssessmentInput input = builder().sexAtBirth("male").age(30).heightCm(180).weightKg(80).build();
        Metabolic overriding = new Metabolic(30.5, "Overweight", 1234, 2222, 1.4, 2500, 180, 250, 70,
                "average (~20%)", "Custom Coach Title", 3.2);
        AnalysisResult analysis = analysisWith(overriding, null, null);

        PlanContent plan = RoadmapGenerator.generate(input, analysis);

        assertThat(plan.metrics().bmr()).isEqualTo(1234);
        assertThat(plan.metrics().tdee()).isEqualTo(2222);
        assertThat(plan.metrics().targetCalories()).isEqualTo(2500);
        assertThat(plan.metrics().roadmapTitle()).isEqualTo("Custom Coach Title");
    }

    @Test
    void alwaysProducesExactlyFourPhases() {
        PlanContent plan = RoadmapGenerator.generate(builder().build(), null);

        assertThat(plan.phases()).hasSize(4);
        assertThat(plan.phases()).extracting(p -> p.phase()).containsExactly(1, 2, 3, 4);
    }

    @Test
    void weeklySplitDayCountMatchesWorkoutFrequency() {
        assertThat(RoadmapGenerator.generate(builder().workoutFrequency(2).build(), null).weeklySplit()).hasSize(2);
        assertThat(RoadmapGenerator.generate(builder().workoutFrequency(3).build(), null).weeklySplit()).hasSize(3);
        assertThat(RoadmapGenerator.generate(builder().workoutFrequency(4).build(), null).weeklySplit()).hasSize(4);
        assertThat(RoadmapGenerator.generate(builder().workoutFrequency(5).build(), null).weeklySplit()).hasSize(5);
    }

    @Test
    void mealPlanHasExactlyTheRequestedNumberOfMealsWithMatchingDietSuggestions() {
        AssessmentInput input = builder().mealsPerDay(5).dietPreference("keto").build();

        PlanContent plan = RoadmapGenerator.generate(input, null);

        assertThat(plan.mealPlan()).hasSize(5);
        assertThat(plan.mealPlan()).allSatisfy(m -> assertThat(m.suggestion()).isNotBlank());
    }

    @Test
    void excludedPatternsFromContraindicationsSwapOrRemoveMatchingExercises() {
        AssessmentInput input = builder().workoutFrequency(3).build();
        // frequency 3, day 2 opens with "Deadlift (Conventional or Trap Bar)" in gym mode
        Contraindications contra = new Contraindications(
                List.of("heavy conventional deadlift"), "gym", 6, false, List.of());
        AnalysisResult analysis = analysisWith(null, null, contra);

        PlanContent plan = RoadmapGenerator.generate(input, analysis);

        boolean stillHasDeadlift = plan.weeklySplit().stream()
                .flatMap(d -> d.exercises().stream())
                .anyMatch(e -> e.name().toLowerCase().contains("deadlift"));
        assertThat(stillHasDeadlift).isFalse();
        assertThat(plan.coachConstraints()).isNotEmpty();
    }

    @Test
    void noHighImpactFlagStripsJumpAndRunningExercisesEvenWithoutAnExplicitPattern() {
        AssessmentInput input = builder().workoutFrequency(2).build();
        Contraindications contra = new Contraindications(List.of(), "gym", 6, true, List.of());
        AnalysisResult analysis = analysisWith(null, null, contra);

        PlanContent plan = RoadmapGenerator.generate(input, analysis);

        boolean anyHighImpact = plan.weeklySplit().stream()
                .flatMap(d -> d.exercises().stream())
                .anyMatch(e -> {
                    String n = e.name().toLowerCase();
                    return n.contains("jump") || n.contains("plyo") || n.contains("sprint") || n.contains("running");
                });
        assertThat(anyHighImpact).isFalse();
    }

    @Test
    void sessionIsCappedToMaxExercisesPerSessionFromContraindications() {
        AssessmentInput input = builder().workoutFrequency(4).build();
        Contraindications contra = new Contraindications(List.of(), "gym", 2, false, List.of());
        AnalysisResult analysis = analysisWith(null, null, contra);

        PlanContent plan = RoadmapGenerator.generate(input, analysis);

        assertThat(plan.weeklySplit()).allSatisfy(d -> assertThat(d.exercises()).hasSizeLessThanOrEqualTo(2));
    }

    @Test
    void nonGymEquipmentModeAppendsAnAdaptedSuffixToDayTitles() {
        AssessmentInput input = builder().workoutFrequency(3).build();
        Contraindications contra = new Contraindications(List.of(), "bands", 6, false, List.of());
        AnalysisResult analysis = analysisWith(null, null, contra);

        PlanContent plan = RoadmapGenerator.generate(input, analysis);

        assertThat(plan.weeklySplit()).allSatisfy(d -> assertThat(d.title()).contains("Bands adapted"));
    }

    @Test
    void summaryCarriesTopLimitersAndFocusHabitsFromTheAnalysis() {
        AssessmentInput input = builder().build();
        Feasibility feasibility = new Feasibility("realistic", "loss", -5, List.of(0.5, 1.0), 0.7, 12, 12, "msg");
        AnalysisResult analysis = new AnalysisResult(
                "now", null, feasibility,
                List.of(),
                List.of(new AnalysisResult.Limiter("high", "Sleep < 6h", "action")),
                new Contraindications(List.of(), "gym", 6, false, List.of()),
                new AnalysisResult.SuggestedStart("Full Body", 10, 2000, 150, 5, List.of("Log every session")),
                List.of());

        PlanContent plan = RoadmapGenerator.generate(input, analysis);

        assertThat(plan.analysisSummary()).isNotNull();
        assertThat(plan.analysisSummary().feasibility()).isEqualTo("realistic");
        assertThat(plan.analysisSummary().topLimiters()).contains("Sleep < 6h");
        assertThat(plan.analysisSummary().focusHabits()).contains("Log every session");
    }

    // --- helpers -------------------------------------------------------

    private static AnalysisResult analysisWith(Metabolic metabolic, Feasibility feasibility, Contraindications contra) {
        return new AnalysisResult(
                "now",
                metabolic,
                feasibility != null ? feasibility : new Feasibility("realistic", "maintain", 0, List.of(0.0, 0.0), 0.0, null, 0, "msg"),
                List.of(),
                List.of(),
                contra != null ? contra : new Contraindications(List.of(), "gym", 6, false, List.of()),
                new AnalysisResult.SuggestedStart("Full Body", 10, 2000, 150, 5, List.of()),
                List.of());
    }
}
