package com.powerfitness.Services;

import com.powerfitness.common.domain.AnalysisResult;
import com.powerfitness.common.domain.AnalysisResult.Contraindications;
import com.powerfitness.common.domain.AnalysisResult.DataQualityFlag;
import com.powerfitness.common.domain.AnalysisResult.Feasibility;
import com.powerfitness.common.domain.AnalysisResult.Limiter;
import com.powerfitness.common.domain.AnalysisResult.Metabolic;
import com.powerfitness.common.domain.AnalysisResult.Strength;
import com.powerfitness.common.domain.AnalysisResult.SuggestedStart;
import com.powerfitness.common.domain.AssessmentInput;
import com.powerfitness.common.domain.AssessmentInput.Injury;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class AssessmentAnalyzer {

    private static final Map<String, List<String>> INJURY_PATTERNS = Map.of(
            "lower_back", List.of("loaded spinal flexion", "heavy conventional deadlift", "good morning", "bent-over barbell row"),
            "upper_back", List.of("heavy loaded carries", "behind-the-neck press"),
            "neck", List.of("behind-the-neck press", "heavy barbell shrug", "loaded neck flexion"),
            "shoulder", List.of("barbell overhead press", "upright row", "behind-the-neck", "deep weighted dip", "wide-grip bench"),
            "elbow", List.of("skullcrusher", "heavy close-grip bench", "weighted dip"),
            "wrist", List.of("straight-bar pressing", "front rack hold", "heavy barbell curl"),
            "hip", List.of("deep below-parallel squat", "wide-stance squat", "heavy deadlift"),
            "knee", List.of("deep loaded knee flexion", "plyometric jump", "sissy squat", "leg extension (heavy)", "high-impact running"),
            "ankle", List.of("plyometric jump", "high-impact running", "jump rope"),
            "other", List.of());

    private AssessmentAnalyzer() {}

    public static AnalysisResult analyze(AssessmentInput a) {
        String sex = "female".equals(orElse(a.sexAtBirth(), a.gender())) ? "female" : "male";
        int age = resolveAge(a);
        double height = a.heightCm() > 0 ? a.heightCm() : 175;
        double weight = a.weightKg() > 0 ? a.weightKg() : 75;
        double goalWt = a.targetWeightKg() > 0 ? a.targetWeightKg() : weight;
        String goal = orElse(a.primaryGoal(), "general_fitness");
        int freq = a.workoutFrequency() > 0 ? a.workoutFrequency() : 3;
        String exp = orElse(a.experienceLevel(), "beginner");
        int dur = a.sessionDurationMin() > 0 ? a.sessionDurationMin() : 60;
        double sleep = a.sleepHours() != null ? a.sleepHours() : 7;
        Integer sleepQ = a.sleepQuality();
        Integer stress = a.stressLevel();
        Integer energy = a.energyLevel();
        String occ = orElse(a.occupationType(), "desk");
        Integer steps = a.dailySteps();
        List<String> equip = a.equipment() != null ? a.equipment() : List.of();
        List<Injury> injuries = a.injuries() != null ? a.injuries() : List.of();
        Integer alcohol = a.alcoholUnitsWeek();
        String cook = a.cookingAbility();
        Integer eatOut = a.eatingOutPerWeek();
        String budget = a.budgetLevel();
        Double yearsTr = a.yearsTraining();

        double heightM = Math.max(1.2, height / 100);
        double bmi = round1(weight / (heightM * heightM));
        String bmiCat = bmiCategory(bmi);

        int bmr = (int) Math.round("female".equals(sex)
                ? 10 * weight + 6.25 * height - 5 * age - 161
                : 10 * weight + 6.25 * height - 5 * age + 5);

        double mult = switch (orElse(a.activityLevel(), "moderate")) {
            case "sedentary" -> 1.2;
            case "light" -> 1.375;
            case "very_active" -> 1.725;
            default -> 1.55;
        };
        if (steps != null) {
            if (steps >= 12000) mult = Math.max(mult, 1.7);
            else if (steps >= 8000) mult = Math.max(mult, 1.55);
            else if (steps < 4000) mult = Math.min(mult, 1.35);
        }
        if ("physical".equals(occ)) mult += 0.05;
        int tdee = (int) Math.round(bmr * mult);

        double[] target = calorieTarget(goal, tdee);
        int targetCals = (int) target[0];
        String title = calorieTitle(goal);
        int protein = (int) Math.round(weight * ("fat_loss".equals(goal) ? 2.2 : 2.0));
        int fatG = (int) Math.round(targetCals * 0.25 / 9);
        int carbG = (int) Math.round(Math.max(0, targetCals - protein * 4.0 - fatG * 9.0) / 4);
        String bfBand = bodyFatBand(bmi, age, sex);
        double water = round1(Math.max(2.5, weight * 0.035 + 0.5));

        Feasibility feas = feasibility(weight, goalWt, a.targetDate());

        List<Strength> strengths = new ArrayList<>();
        if (freq >= 5) strengths.add(new Strength(freq + " training days available", "High training frequency — supports a full split."));
        else if (freq >= 4) strengths.add(new Strength(freq + " training days available", "Enough frequency for an upper/lower split."));
        if (sleep >= 7.5 && (sleepQ == null || sleepQ >= 4)) strengths.add(new Strength("Sleeps " + trimNum(sleep) + "h", "Recovery capacity is good — normal volume tolerated."));
        if ("confident".equals(cook)) strengths.add(new Strength("Cooks at home", "Nutrition adherence will be easier — can run a real meal plan."));
        if (yearsTr != null && yearsTr >= 2) strengths.add(new Strength(trimNum(yearsTr) + " yrs experience", "Can handle intermediate loading and exercise variety."));
        if (steps != null && steps >= 8000) strengths.add(new Strength(steps + " steps/day", "Strong NEAT baseline — helps with energy balance."));
        if (stress != null && stress <= 4) strengths.add(new Strength("Low stress load", "More room for training stress and progression."));
        if (energy != null && energy >= 7) strengths.add(new Strength("High baseline energy", "Good starting point for intensity."));
        if (equip.contains("full_gym") || equip.contains("gym")) strengths.add(new Strength("Full gym access", "No equipment constraint on exercise selection."));
        if (a.strengthSquat() != null || a.strengthBench() != null || a.strengthDeadlift() != null)
            strengths.add(new Strength("Provided current lift numbers", "Starting loads can be prescribed, not guessed."));

        List<Limiter> limiters = new ArrayList<>();
        if (sleep < 6) limiters.add(new Limiter("high", "Sleep < 6h", "Recovery-limited — cap weekly volume ~25%, no early peak intensity."));
        else if (sleep < 6.9 || (sleepQ != null && sleepQ <= 2)) limiters.add(new Limiter("medium", "Sub-optimal sleep", "Moderate volume; make sleep the first focus habit."));

        if ("shift".equals(occ)) limiters.add(new Limiter("medium", "Shift work", "Irregular meal & sleep timing — flexible meal plan, autoregulated sessions."));
        else if ("physical".equals(occ)) limiters.add(new Limiter("low", "Physically demanding job", "Extra daily fatigue — keep leg volume moderate, watch recovery."));

        if (stress != null && stress >= 8) limiters.add(new Limiter("medium", "High stress (" + stress + "/10)", "Manage total stress load — build in a deload every 4-5 weeks."));
        if (energy != null && energy <= 3) limiters.add(new Limiter("medium", "Low energy (" + energy + "/10)", "Start conservative; verify calorie/iron/sleep adequacy."));

        for (Injury inj : injuries) {
            String part = orElse(inj.bodyPart(), "other");
            boolean painful = inj.stillPainful();
            String label = capitalize(part.replace('_', ' ')) + " issue" + (painful ? " (still painful)" : " (historical)");
            String avoid = orElse(inj.avoid(), String.join(", ", INJURY_PATTERNS.getOrDefault(part, List.of())));
            limiters.add(new Limiter(painful ? "high" : "medium", label,
                    "Avoid: " + (avoid.isBlank() ? "aggravating loaded patterns" : avoid) + ". Substitute with pain-free variations."));
        }

        boolean limitedEquip = disjoint(equip, List.of("full_gym", "gym", "barbell"))
                && (equip.contains("bands") || equip.contains("bodyweight") || equip.contains("bodyweight_only") || equip.contains("calisthenics"));
        if (limitedEquip) limiters.add(new Limiter("medium", "Limited equipment", "Progressive overload constrained — use tempo, unilateral work, density and RIR progression."));

        if (dur < 40) limiters.add(new Limiter("medium", "Short sessions (" + dur + " min)", "Cap at 3-4 exercises/session, superset antagonists."));
        if (eatOut != null && eatOut >= 5) limiters.add(new Limiter("medium", "Eats out " + eatOut + "x/week", "Focus on portion & protein habits rather than strict macro tracking."));
        if ("tight".equals(budget)) limiters.add(new Limiter("low", "Tight food budget", "Prioritise cheap protein (eggs, dairy, legumes, frozen); keep the plan simple."));
        if (alcohol != null && alcohol >= 10) limiters.add(new Limiter("medium", "High alcohol (" + alcohol + " units/wk)", "Impairs recovery, sleep and fat loss — flag for a conversation."));
        if (a.pregnant()) limiters.add(new Limiter("high", "Pregnancy declared", "Medical clearance required. Coach-built plan only, no algorithmic prescription."));
        if (a.medicalConditions() != null && !a.medicalConditions().isBlank())
            limiters.add(new Limiter("high", "Medical condition reported", "Review the details and confirm clearance to exercise before approving."));

        Map<String, Integer> rank = Map.of("high", 0, "medium", 1, "low", 2);
        limiters.sort(Comparator.comparingInt(l -> rank.getOrDefault(l.severity(), 3)));

        List<String> excluded = new ArrayList<>();
        for (Injury inj : injuries) {
            excluded.addAll(INJURY_PATTERNS.getOrDefault(orElse(inj.bodyPart(), "other"), List.of()));
        }
        excluded = excluded.stream().distinct().toList();

        int maxPerSession = dur < 30 ? 3 : dur < 45 ? 4 : dur < 60 ? 5 : dur < 90 ? 6 : 7;

        String equipmentMode = "gym";
        if (limitedEquip && equip.contains("dumbbells")) equipmentMode = "home";
        else if (limitedEquip && equip.contains("bands")) equipmentMode = "bands";
        else if (limitedEquip) equipmentMode = "bodyweight";

        boolean noHighImpact = injuries.stream().anyMatch(i -> "knee".equals(i.bodyPart()) || "ankle".equals(i.bodyPart()));

        List<String> notes = new ArrayList<>();
        if (!excluded.isEmpty()) notes.add("Generator must exclude: " + String.join("; ", excluded) + ".");
        notes.add("Hard cap: " + maxPerSession + " exercises per session (" + dur + " min).");
        if (!"gym".equals(equipmentMode)) notes.add("Equipment mode: " + equipmentMode + " — no barbell-only prescriptions.");
        Contraindications contra = new Contraindications(excluded, equipmentMode, maxPerSession, noHighImpact, notes);

        String split = suggestedSplit(freq);
        int baseSets = switch (exp) {
            case "intermediate" -> 14;
            case "advanced" -> 17;
            default -> 10;
        };
        if (sleep < 6) baseSets = (int) Math.round(baseSets * 0.75);
        if (limitedEquip) baseSets = (int) Math.round(baseSets * 0.85);
        List<String> habits = focusHabits(limiters, goal);
        SuggestedStart suggested = new SuggestedStart(split, baseSets, targetCals, protein, maxPerSession, habits);

        List<DataQualityFlag> flags = new ArrayList<>();
        double delta = round1(goalWt - weight);
        if ("fat_loss".equals(goal) && delta > 2) flags.add(new DataQualityFlag("contradiction", "Goal is fat loss but goal weight is +" + delta + " kg above current."));
        if ((goal.equals("muscle_gain") || goal.equals("weight_gain")) && delta < -2) flags.add(new DataQualityFlag("contradiction", "Goal is mass gain but goal weight is " + delta + " kg below current."));
        if (goalWt <= 0 || goalWt < 35 || goalWt > 250) flags.add(new DataQualityFlag("invalid", "Goal weight (" + goalWt + " kg) looks unrealistic."));
        if (a.targetDate() != null && a.targetDate().isBefore(LocalDate.now())) flags.add(new DataQualityFlag("invalid", "Target date is in the past."));
        if ("unrealistic".equals(feas.verdict())) flags.add(new DataQualityFlag("unrealistic_date", feas.message()));
        if (age < 16) flags.add(new DataQualityFlag("age", "Client is under 16 — parental consent & conservative programming required."));
        if (age > 70) flags.add(new DataQualityFlag("age", "Client is 70+ — prioritise joint-friendly loading and medical clearance."));
        if (injuries.isEmpty()) flags.add(new DataQualityFlag("missing", "Injury history not answered — confirm there are none."));
        if (a.strengthSquat() == null && a.strengthBench() == null && a.strengthDeadlift() == null)
            flags.add(new DataQualityFlag("missing", "No current lift numbers — ask before prescribing loads."));
        if (a.waistCm() == null) flags.add(new DataQualityFlag("missing", "No waist measurement — request one for body-composition tracking."));

        Metabolic metabolic = new Metabolic(bmi, bmiCat, bmr, tdee, round3(mult), targetCals, protein, carbG, fatG, bfBand, title, water);
        return new AnalysisResult(
                java.time.Instant.now().toString(),
                metabolic, feas, strengths, limiters, contra, suggested, flags);
    }



    private static int resolveAge(AssessmentInput a) {
        if (a.dateOfBirth() != null) {
            int y = Period.between(a.dateOfBirth(), LocalDate.now()).getYears();
            return Math.max(12, y);
        }
        return Math.max(12, a.age() > 0 ? a.age() : 22);
    }

    private static String bmiCategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Healthy weight";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }

    private static String bodyFatBand(double bmi, int age, String sex) {
        double bf = 1.20 * bmi + 0.23 * age - 10.8 * ("male".equals(sex) ? 1 : 0) - 5.4;
        bf = Math.max(4, Math.min(55, bf));
        long r = Math.round(bf);
        if ("male".equals(sex)) {
            if (bf < 10) return "very lean (~" + r + "%)";
            if (bf < 15) return "lean (~" + r + "%)";
            if (bf < 20) return "average (~" + r + "%)";
            if (bf < 25) return "above average (~" + r + "%)";
            return "high (~" + r + "%)";
        }
        if (bf < 18) return "very lean (~" + r + "%)";
        if (bf < 24) return "lean (~" + r + "%)";
        if (bf < 30) return "average (~" + r + "%)";
        if (bf < 35) return "above average (~" + r + "%)";
        return "high (~" + r + "%)";
    }

    static double[] calorieTarget(String goal, int tdee) {
        return switch (goal) {
            case "fat_loss" -> new double[] {Math.max(1400, tdee - 450)};
            case "muscle_gain" -> new double[] {tdee + 350};
            case "weight_gain" -> new double[] {tdee + 500};
            case "athletic_performance" -> new double[] {tdee + 200};
            default -> new double[] {tdee};
        };
    }

    static String calorieTitle(String goal) {
        return switch (goal) {
            case "fat_loss" -> "12-Week Lean Physique & Fat Loss Roadmap";
            case "muscle_gain" -> "12-Week Hypertrophy & Muscle Building Roadmap";
            case "weight_gain" -> "12-Week Mass & Caloric Surplus Blueprint";
            case "athletic_performance" -> "12-Week Athletic Performance Roadmap";
            default -> "12-Week Full Body Transformation Roadmap";
        };
    }

    private static Feasibility feasibility(double current, double goal, LocalDate targetDate) {
        double delta = round1(goal - current);
        String dir = Math.abs(delta) < 1 ? "maintain" : (delta < 0 ? "loss" : "gain");

        if ("maintain".equals(dir)) {
            return new Feasibility("maintenance", "maintain", delta, List.of(0.0, 0.0), 0.0, null, 0,
                    "Goal weight is close to current weight - programme for recomposition / performance, not scale change.");
        }

        double loPct = "loss".equals(dir) ? 0.5 : 0.15;
        double hiPct = "loss".equals(dir) ? 1.0 : 0.4;
        double midKgPerWeek = (loPct + hiPct) / 2 / 100 * current;
        int projectedWeeks = (int) Math.ceil(Math.abs(delta) / Math.max(0.05, midKgPerWeek));

        Integer weeksAvailable = null;
        Double requiredPct = null;
        String verdict;
        String msg;
        if (targetDate != null && targetDate.isAfter(LocalDate.now())) {
            weeksAvailable = (int) Math.max(1, Math.round(ChronoUnit.DAYS.between(LocalDate.now(), targetDate) / 7.0));
            double requiredKgPerWeek = Math.abs(delta) / weeksAvailable;
            requiredPct = round2(requiredKgPerWeek / current * 100);

            if (requiredPct <= hiPct) {
                verdict = "realistic";
                msg = "On track: needs ~" + requiredPct + "% BW/week over " + weeksAvailable + " weeks (safe range <= " + hiPct + "%).";
            } else if (requiredPct <= hiPct * 1.5) {
                verdict = "aggressive";
                msg = "Aggressive: needs ~" + requiredPct + "% BW/week - above the " + hiPct + "% comfort zone. Possible but tighten adherence.";
            } else {
                verdict = "unrealistic";
                int suggestedWeeks = (int) Math.ceil(Math.abs(delta) / (hiPct / 100 * current));
                msg = "Unrealistic target date: needs ~" + requiredPct + "% BW/week. Recommend extending to ~" + suggestedWeeks + " weeks or reducing the goal.";
            }
        } else {
            verdict = "realistic";
            msg = "No fixed date. Safe pace (" + loPct + "-" + hiPct + "% BW/week) puts this at ~" + projectedWeeks + " weeks.";
        }

        double rate = requiredPct != null ? requiredPct : round2((loPct + hiPct) / 2);
        return new Feasibility(verdict, dir, delta, List.of(loPct, hiPct), rate, weeksAvailable, projectedWeeks, msg);
    }

    private static String suggestedSplit(int freq) {
        if (freq <= 2) return "Full Body A / B";
        if (freq == 3) return "Full Body x3 (push-bias / pull-bias / lower)";
        if (freq == 4) return "Upper / Lower x2";
        return "Push / Pull / Legs (x" + (freq >= 6 ? "2" : "1.5") + ")";
    }

    private static List<String> focusHabits(List<Limiter> limiters, String goal) {
        List<String> h = new ArrayList<>();
        for (Limiter l : limiters) {
            if (l.label().contains("Sleep") && h.isEmpty()) h.add("Sleep 7-8h - fixed bedtime, no screens 30 min before.");
            if (l.label().toLowerCase().contains("alcohol")) h.add("Cap alcohol to <= 3 units/week during the block.");
            if (l.label().toLowerCase().contains("eats out")) h.add("Half the plate vegetables + a palm of protein when eating out.");
        }
        if ("fat_loss".equals(goal)) h.add("Hit the protein target every day (weigh food for week 1-2).");
        if (goal.equals("muscle_gain") || goal.equals("weight_gain")) h.add("Eat in the first hour after waking + a pre-sleep protein feed.");
        h.add("Log every training session in the dashboard.");
        return h.stream().distinct().limit(3).toList();
    }

    private static String orElse(String v, String fallback) {
        return v != null && !v.isBlank() ? v : fallback;
    }

    private static boolean disjoint(List<String> a, List<String> b) {
        return a.stream().noneMatch(b::contains);
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String trimNum(double d) {
        return d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(round1(d));
    }

    private static double round1(double d) {
        return Math.round(d * 10.0) / 10.0;
    }

    private static double round2(double d) {
        return Math.round(d * 100.0) / 100.0;
    }

    private static double round3(double d) {
        return Math.round(d * 1000.0) / 1000.0;
    }
}
