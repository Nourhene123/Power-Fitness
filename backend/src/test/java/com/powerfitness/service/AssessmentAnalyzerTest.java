package com.powerfitness.service;

import static com.powerfitness.support.AssessmentInputBuilder.builder;
import static org.assertj.core.api.Assertions.assertThat;

import com.powerfitness.common.domain.AnalysisResult;
import com.powerfitness.common.domain.AssessmentInput;
import com.powerfitness.common.domain.AssessmentInput.Injury;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AssessmentAnalyzerTest {

    @Test
    void computesBmrAndTdeeForAKnownMaleProfile() {
        AssessmentInput input = builder()
                .sexAtBirth("male")
                .age(30)
                .heightCm(180)
                .weightKg(80)
                .activityLevel("sedentary")
                .build();

        AnalysisResult.Metabolic metabolic = AssessmentAnalyzer.analyze(input).metabolic();

        // Mifflin-St Jeor, male: 10*80 + 6.25*180 - 5*30 + 5 = 1780
        assertThat(metabolic.bmr()).isEqualTo(1780);
        // sedentary multiplier is 1.2
        assertThat(metabolic.tdee()).isEqualTo((int) Math.round(1780 * 1.2));
    }

    @Test
    void computesBmrForFemaleUsingTheDifferentOffset() {
        AssessmentInput input = builder()
                .sexAtBirth("female")
                .age(30)
                .heightCm(165)
                .weightKg(60)
                .activityLevel("sedentary")
                .build();

        int bmr = AssessmentAnalyzer.analyze(input).metabolic().bmr();

        // Mifflin-St Jeor, female: 10*60 + 6.25*165 - 5*30 - 161
        assertThat(bmr).isEqualTo((int) Math.round(10 * 60 + 6.25 * 165 - 5 * 30 - 161));
    }

    @Test
    void highDailyStepsRaiseTheActivityMultiplierEvenOnASedentaryLabel() {
        AssessmentInput input = builder().activityLevel("sedentary").dailySteps(13000).build();

        double multiplier = AssessmentAnalyzer.analyze(input).metabolic().activityMultiplier();

        assertThat(multiplier).isEqualTo(1.7);
    }

    @Test
    void fatLossTargetIsBelowTdeeButNeverUnderTheFloor() {
        double[] target = AssessmentAnalyzer.calorieTarget("fat_loss", 1500);

        // tdee - 450 would be 1050, below the 1400 floor
        assertThat(target[0]).isEqualTo(1400);
    }

    @Test
    void calorieTargetsAdjustByGoal() {
        assertThat(AssessmentAnalyzer.calorieTarget("fat_loss", 2500)[0]).isEqualTo(2050);
        assertThat(AssessmentAnalyzer.calorieTarget("muscle_gain", 2500)[0]).isEqualTo(2850);
        assertThat(AssessmentAnalyzer.calorieTarget("weight_gain", 2500)[0]).isEqualTo(3000);
        assertThat(AssessmentAnalyzer.calorieTarget("athletic_performance", 2500)[0]).isEqualTo(2700);
        assertThat(AssessmentAnalyzer.calorieTarget("general_fitness", 2500)[0]).isEqualTo(2500);
    }

    @Test
    void feasibilityIsRealisticForAConservativeFatLossTarget() {
        AssessmentInput input = builder()
                .weightKg(90)
                .targetWeightKg(85)
                .targetDate(LocalDate.now().plusWeeks(12))
                .build();

        AnalysisResult.Feasibility feasibility = AssessmentAnalyzer.analyze(input).feasibility();

        assertThat(feasibility.verdict()).isEqualTo("realistic");
        assertThat(feasibility.direction()).isEqualTo("loss");
    }

    @Test
    void feasibilityIsUnrealisticForAnExtremeTargetDate() {
        AssessmentInput input = builder()
                .weightKg(100)
                .targetWeightKg(70)
                .targetDate(LocalDate.now().plusWeeks(2))
                .build();

        AnalysisResult.Feasibility feasibility = AssessmentAnalyzer.analyze(input).feasibility();

        assertThat(feasibility.verdict()).isEqualTo("unrealistic");
    }

    @Test
    void feasibilityIsMaintenanceWhenGoalWeightMatchesCurrentWeight() {
        AssessmentInput input = builder().weightKg(75).targetWeightKg(75).build();

        AnalysisResult.Feasibility feasibility = AssessmentAnalyzer.analyze(input).feasibility();

        assertThat(feasibility.verdict()).isEqualTo("maintenance");
        assertThat(feasibility.direction()).isEqualTo("maintain");
    }

    @Test
    void aPainfulKneeInjuryExcludesHighImpactPatternsAndAddsAHighSeverityLimiter() {
        AssessmentInput input = builder()
                .injuries(List.of(new Injury("knee", true, null)))
                .build();

        AnalysisResult result = AssessmentAnalyzer.analyze(input);

        assertThat(result.contraindications().noHighImpact()).isTrue();
        assertThat(result.contraindications().excludedPatterns()).contains("plyometric jump", "high-impact running");
        assertThat(result.limiters())
                .anySatisfy(l -> {
                    assertThat(l.severity()).isEqualTo("high");
                    assertThat(l.label()).containsIgnoringCase("knee");
                });
    }

    @Test
    void limitersAreSortedBySeverityHighFirst() {
        AssessmentInput input = builder()
                .sleepHours(9) // no sleep limiter
                .injuries(List.of(new Injury("wrist", false, null))) // medium
                .eatingOutPerWeek(6) // medium
                .pregnant(true) // high
                .build();

        List<AnalysisResult.Limiter> limiters = AssessmentAnalyzer.analyze(input).limiters();

        assertThat(limiters.get(0).severity()).isEqualTo("high");
    }

    @Test
    void limitedEquipmentWithDumbbellsPlusBodyweightResolvesToHomeMode() {
        // "limited equipment" only trips when the set is disjoint from {full_gym, gym, barbell}
        // AND contains one of {bands, bodyweight, bodyweight_only, calisthenics} — dumbbells alone
        // isn't enough to flag it as equipment-constrained.
        AssessmentInput input = builder().equipment(List.of("dumbbells", "bodyweight")).build();

        String mode = AssessmentAnalyzer.analyze(input).contraindications().equipmentMode();

        assertThat(mode).isEqualTo("home");
    }

    @Test
    void dumbbellsAloneAreNotEnoughToTriggerLimitedEquipmentMode() {
        AssessmentInput input = builder().equipment(List.of("dumbbells")).build();

        String mode = AssessmentAnalyzer.analyze(input).contraindications().equipmentMode();

        assertThat(mode).isEqualTo("gym");
    }

    @Test
    void limitedEquipmentWithOnlyBandsResolvesToBandsMode() {
        AssessmentInput input = builder().equipment(List.of("bands")).build();

        String mode = AssessmentAnalyzer.analyze(input).contraindications().equipmentMode();

        assertThat(mode).isEqualTo("bands");
    }

    @Test
    void fullGymAccessResolvesToGymMode() {
        AssessmentInput input = builder().equipment(List.of("full_gym")).build();

        String mode = AssessmentAnalyzer.analyze(input).contraindications().equipmentMode();

        assertThat(mode).isEqualTo("gym");
    }

    @Test
    void shortSessionsCapExercisesPerSessionLower() {
        AssessmentInput input = builder().sessionDurationMin(25).build();

        int cap = AssessmentAnalyzer.analyze(input).contraindications().maxExercisesPerSession();

        assertThat(cap).isEqualTo(3);
    }

    @Test
    void flagsAContradictionWhenFatLossGoalHasAHigherTargetWeight() {
        AssessmentInput input = builder()
                .primaryGoal("fat_loss")
                .weightKg(80)
                .targetWeightKg(85)
                .build();

        List<AnalysisResult.DataQualityFlag> flags = AssessmentAnalyzer.analyze(input).dataQualityFlags();

        assertThat(flags).anySatisfy(f -> assertThat(f.type()).isEqualTo("contradiction"));
    }

    @Test
    void flagsAPastTargetDateAsInvalid() {
        AssessmentInput input = builder().targetDate(LocalDate.now().minusDays(1)).build();

        List<AnalysisResult.DataQualityFlag> flags = AssessmentAnalyzer.analyze(input).dataQualityFlags();

        assertThat(flags).anySatisfy(f -> assertThat(f.type()).isEqualTo("invalid"));
    }

    @Test
    void flagsUnder16AndOver70AgesForConservativeProgramming() {
        AssessmentInput minor = builder().age(14).build();
        AssessmentInput senior = builder().age(75).build();

        assertThat(AssessmentAnalyzer.analyze(minor).dataQualityFlags())
                .anySatisfy(f -> assertThat(f.type()).isEqualTo("age"));
        assertThat(AssessmentAnalyzer.analyze(senior).dataQualityFlags())
                .anySatisfy(f -> assertThat(f.type()).isEqualTo("age"));
    }

    @Test
    void flagsMissingInjuryHistoryLiftNumbersAndWaistMeasurement() {
        AssessmentInput input = builder().injuries(List.of()).build();

        List<AnalysisResult.DataQualityFlag> flags = AssessmentAnalyzer.analyze(input).dataQualityFlags();

        assertThat(flags).extracting(AnalysisResult.DataQualityFlag::type)
                .contains("missing");
        assertThat(flags).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void roadmapTitleMatchesThePrimaryGoal() {
        assertThat(AssessmentAnalyzer.calorieTitle("fat_loss")).contains("Fat Loss");
        assertThat(AssessmentAnalyzer.calorieTitle("muscle_gain")).contains("Hypertrophy");
        assertThat(AssessmentAnalyzer.calorieTitle("weight_gain")).contains("Mass");
        assertThat(AssessmentAnalyzer.calorieTitle("athletic_performance")).contains("Athletic Performance");
        assertThat(AssessmentAnalyzer.calorieTitle("general_fitness")).contains("Full Body");
    }

}
