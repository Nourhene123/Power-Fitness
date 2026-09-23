package com.powerfitness.service;

import com.powerfitness.common.domain.AnalysisResult;
import com.powerfitness.common.domain.AssessmentInput;
import com.powerfitness.common.domain.PlanContent;
import com.powerfitness.common.domain.PlanContent.AnalysisSummary;
import com.powerfitness.common.domain.PlanContent.Guidelines;
import com.powerfitness.common.domain.PlanContent.Meal;
import com.powerfitness.common.domain.PlanContent.Metrics;
import com.powerfitness.common.domain.PlanContent.Phase;
import com.powerfitness.common.domain.PlanContent.PlannedExercise;
import com.powerfitness.common.domain.PlanContent.TrainingDay;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RoadmapGenerator {

    private RoadmapGenerator() {}

    public static PlanContent generate(AssessmentInput a, AnalysisResult analysis) {
        int age = a.age() > 0 ? a.age() : 22;
        String gender = firstNonBlank(a.sexAtBirth(), a.gender(), "male");
        double height = a.heightCm() > 0 ? a.heightCm() : 175;
        double weight = a.weightKg() > 0 ? a.weightKg() : 70;
        double targetWeight = a.targetWeightKg() > 0 ? a.targetWeightKg() : 75;
        int workoutFreq = a.workoutFrequency() > 0 ? a.workoutFrequency() : 4;
        int duration = a.sessionDurationMin() > 0 ? a.sessionDurationMin() : 60;
        String location = firstNonBlank(a.workoutLocation(), "gym");
        int mealsPerDay = Math.max(2, Math.min(6, a.mealsPerDay() > 0 ? a.mealsPerDay() : 4));
        String dietPref = firstNonBlank(a.dietPreference(), "high_protein");
        String activityLevel = firstNonBlank(a.activityLevel(), "moderate");
        String experience = firstNonBlank(a.experienceLevel(), "beginner");
        String goal = firstNonBlank(a.primaryGoal(), "muscle_gain");
        String focusAreas = firstNonBlank(a.focusAreas(), "full_body");

        double heightM = height / 100;
        double bmi = round1(weight / (heightM * heightM));
        String bmiCategory = bmiCategory(bmi);

        int bmr = (int) Math.round("female".equals(gender)
                ? 10 * weight + 6.25 * height - 5 * age - 161
                : 10 * weight + 6.25 * height - 5 * age + 5);

        double multiplier = switch (activityLevel) {
            case "sedentary" -> 1.2;
            case "light" -> 1.375;
            case "very_active" -> 1.725;
            default -> 1.55;
        };
        int tdee = (int) Math.round(bmr * multiplier);

        int targetCalories;
        String roadmapTitle;
        switch (goal) {
            case "fat_loss" -> {
                targetCalories = Math.max(1400, tdee - 450);
                roadmapTitle = "12-Week Lean Physique & Fat Shredding Roadmap";
            }
            case "muscle_gain" -> {
                targetCalories = tdee + 350;
                roadmapTitle = "12-Week Hypertrophy & Muscle Building Roadmap";
            }
            case "weight_gain" -> {
                targetCalories = tdee + 500;
                roadmapTitle = "12-Week Mass & Caloric Surplus Blueprint";
            }
            case "athletic_performance" -> {
                targetCalories = tdee + 200;
                roadmapTitle = "12-Week High-Performance Conditioning Roadmap";
            }
            default -> {
                targetCalories = tdee;
                roadmapTitle = "12-Week Full Body Transformation Roadmap";
            }
        }

        int proteinGrams = (int) Math.round(weight * ("fat_loss".equals(goal) ? 2.2 : 2.0));
        int proteinCals = proteinGrams * 4;
        int fatGrams = (int) Math.round(targetCalories * 0.25 / 9);
        int carbGrams = (int) Math.round(Math.max(0, targetCalories - (proteinCals + fatGrams * 9.0)) / 4);
        double waterLiters = round1(Math.max(2.5, weight * 0.035 + 0.5));

        // Analysis metabolic numbers win when present.
        List<String> coachConstraints = new ArrayList<>();
        if (analysis != null && analysis.metabolic() != null) {
            var m = analysis.metabolic();
            bmi = m.bmi();
            bmiCategory = m.bmiCategory();
            bmr = m.bmr();
            tdee = m.tdee();
            targetCalories = m.targetCalories();
            proteinGrams = m.proteinG();
            carbGrams = m.carbsG();
            fatGrams = m.fatsG();
            waterLiters = m.waterLiters();
            if (m.roadmapTitle() != null && !m.roadmapTitle().isBlank()) {
                roadmapTitle = m.roadmapTitle();
            }
        }

        List<Phase> phases = generatePhases();
        List<TrainingDay> weeklySplit = generateWeeklySplit(workoutFreq, location, duration);

        if (analysis != null && analysis.contraindications() != null) {
            var applied = applyConstraints(weeklySplit, analysis.contraindications());
            weeklySplit = applied.split();
            coachConstraints = applied.log();
        }

        List<Meal> mealPlan = generateMealPlan(mealsPerDay, dietPref, targetCalories, proteinGrams, carbGrams, fatGrams);
        Guidelines guidelines = generateGuidelines();

        AnalysisSummary summary = analysis == null ? null : new AnalysisSummary(
                analysis.feasibility() != null ? analysis.feasibility().verdict() : null,
                analysis.limiters().stream().map(AnalysisResult.Limiter::label).limit(3).toList(),
                analysis.suggestedStart() != null ? analysis.suggestedStart().focusHabits() : List.of());

        Metrics metrics = new Metrics(bmi, bmiCategory, bmr, tdee, targetCalories,
                proteinGrams, carbGrams, fatGrams, waterLiters, roadmapTitle);

        return new PlanContent(metrics, phases, weeklySplit, mealPlan, guidelines,
                coachConstraints, summary, List.of());
    }


    private static List<Phase> generatePhases() {
        return List.of(
                new Phase(1, "Phase 1: Neuromuscular Adaptation & Foundation", "Weeks 1 - 3",
                        "Movement mechanics, core activation, strict form & baseline conditioning",
                        "65% - 70% 1RM (RPE 6-7)",
                        List.of("Establish consistent workout habits without missing sessions",
                                "Dial in daily hydration (2.5L+ daily) and sleep hygiene",
                                "Master compound movement cues (hip hinge, scapular retraction)")),
                new Phase(2, "Phase 2: Progressive Overload & Hypertrophic Density", "Weeks 4 - 7",
                        "Systematic load progression, time under tension & metabolic conditioning",
                        "75% - 82% 1RM (RPE 7.5-8.5)",
                        List.of("Add 1-2.5kg or 1 extra rep each week on compound lifts",
                                "Hit 100% of daily protein target across all scheduled meals",
                                "Track resting heart rate and recovery metrics")),
                new Phase(3, "Phase 3: Peak Intensity & Physique Sculpting", "Weeks 8 - 10",
                        "High-yield mechanical tension, supersets & metabolic peak",
                        "85% - 90% 1RM (RPE 8.5-9)",
                        List.of("Achieve peak personal records on key compound movements",
                                "Strict meal timing around pre and post-workout windows",
                                "Visible transformation in body composition and definition")),
                new Phase(4, "Phase 4: Deload, Consolidation & Long-Term Mastery", "Weeks 11 - 12",
                        "Active recovery, joint decompression & sustainable long-term lifestyle",
                        "60% 1RM (Active recovery & technique consolidation)",
                        List.of("Allow central nervous system (CNS) and connective tissues to recover",
                                "Evaluate final weight, body composition, and milestone achievement",
                                "Set new quarterly athletic benchmarks")));
    }



    private static PlannedExercise ex(String name, String sets, String reps, String rest) {
        return new PlannedExercise(name, sets, reps, rest, "");
    }

    private static TrainingDay day(String day, String title, int duration, PlannedExercise... exercises) {
        return new TrainingDay(day, title, duration + " mins", List.of(exercises));
    }

    private static List<TrainingDay> generateWeeklySplit(int frequency, String location, int duration) {
        boolean isGym = "gym".equals(location);
        boolean isCal = "calisthenics".equals(location);
        List<TrainingDay> splits = new ArrayList<>();

        if (frequency == 2) {
            splits.add(day("Day 1: Tuesday", "Full Body Power & Core Engine", duration,
                    ex(isGym ? "Barbell Squats" : (isCal ? "Pistol Squats / Bulgarian Split Squats" : "Goblet Squats"), "4 sets", "8-10 reps", "90s"),
                    ex(isGym ? "Incline Dumbbell Bench Press" : "Weighted Push-ups / Decline Push-ups", "4 sets", "8-12 reps", "75s"),
                    ex(isGym ? "Barbell Bent-Over Row" : "Pull-ups / Inverted Rows", "4 sets", "8-10 reps", "75s"),
                    ex("Romanian Deadlift (Dumbbell or Barbell)", "3 sets", "10-12 reps", "60s"),
                    ex("Hanging Knee Raises / Hollow Body Holds", "3 sets", "15 reps", "45s")));
            splits.add(day("Day 2: Friday", "Full Body Hypertrophy & Athletic Stamina", duration,
                    ex(isGym ? "Overhead Barbell Press" : "Handstand Push-up Progression / Pike Push-ups", "4 sets", "8-10 reps", "90s"),
                    ex(isGym ? "Leg Press or Lunges" : "Walking Lunges with explosive jumps", "4 sets", "12 reps / leg", "60s"),
                    ex(isGym ? "Lat Pulldown or Weighted Pull-up" : "Wide Grip Pull-ups", "4 sets", "10 reps", "75s"),
                    ex("Dips (Parallel bars or bench)", "3 sets", "12-15 reps", "60s"),
                    ex("Plank to Push-up with Side Planks", "3 sets", "45s hold", "45s")));
        } else if (frequency == 3) {
            splits.add(day("Day 1: Monday", "Full Body Push & Quad Bias", duration,
                    ex(isGym ? "Barbell Back Squat" : "Bulgarian Split Squats", "4 sets", "6-8 reps", "120s"),
                    ex(isGym ? "Flat Dumbbell Bench Press" : "Push-ups with Pause at bottom", "4 sets", "8-10 reps", "90s"),
                    ex(isGym ? "Standing Overhead Press" : "Pike Push-ups", "3 sets", "10 reps", "75s"),
                    ex("Leg Extensions or Sissy Squats", "3 sets", "12-15 reps", "60s"),
                    ex("Cable / Resistance Band Tricep Extensions", "3 sets", "12 reps", "45s")));
            splits.add(day("Day 2: Wednesday", "Full Body Pull & Posterior Chain", duration,
                    ex(isGym ? "Deadlift (Conventional or Trap Bar)" : "Single-Leg Romanian Deadlift", "4 sets", "6 reps", "120s"),
                    ex(isGym ? "Chest Supported T-Bar Row" : "Pull-ups / Chin-ups", "4 sets", "8-10 reps", "90s"),
                    ex(isGym ? "Hamstring Curls" : "Nordic Hamstring Curls / Glute Bridges", "3 sets", "12 reps", "60s"),
                    ex("Incline Dumbbell Bicep Curls", "3 sets", "10-12 reps", "60s"),
                    ex("Face Pulls / Rear Delt Flyes", "3 sets", "15 reps", "45s")));
            splits.add(day("Day 3: Friday", "Athletic Power, Shoulders & Core", duration,
                    ex(isGym ? "Incline Barbell Bench Press" : "Decline Push-ups", "4 sets", "8-10 reps", "90s"),
                    ex("Walking Lunges with Dumbbells", "3 sets", "12 reps / leg", "60s"),
                    ex(isGym ? "Lat Pulldowns (Neutral Grip)" : "Inverted Rows", "4 sets", "10-12 reps", "60s"),
                    ex("Dumbbell Lateral Raises", "4 sets", "12-15 reps", "45s"),
                    ex("Hanging Leg Raises & Russian Twists", "3 sets", "15-20 reps", "45s")));
        } else if (frequency == 4) {
            splits.add(day("Day 1: Monday", "Upper Body Power & Chest/Back Focus", duration,
                    ex(isGym ? "Barbell Bench Press" : "Weighted Push-ups", "4 sets", "6-8 reps", "90s"),
                    ex(isGym ? "Barbell Bent Over Row" : "Pull-ups", "4 sets", "6-8 reps", "90s"),
                    ex("Overhead Dumbbell Press", "3 sets", "8-10 reps", "75s"),
                    ex("Incline Dumbbell Chest Flyes", "3 sets", "12 reps", "60s"),
                    ex("Bicep Barbell Curls superset with Skullcrushers", "3 sets", "10-12 reps", "60s")));
            splits.add(day("Day 2: Tuesday", "Lower Body Strength & Posterior Chain", duration,
                    ex(isGym ? "Barbell Back Squats" : "Bulgarian Split Squats", "4 sets", "6-8 reps", "120s"),
                    ex(isGym ? "Romanian Deadlifts" : "Dumbbell Stiff-Leg Deadlifts", "4 sets", "8-10 reps", "90s"),
                    ex(isGym ? "Leg Press" : "Walking Lunges", "3 sets", "10-12 reps", "75s"),
                    ex("Standing Calf Raises", "4 sets", "15 reps", "45s"),
                    ex("Cable Woodchoppers & Ab Wheel Rollouts", "3 sets", "12-15 reps", "45s")));
            splits.add(day("Day 3: Thursday", "Upper Body Hypertrophy & Shoulders/Arms Bias", duration,
                    ex("Incline Dumbbell Press", "4 sets", "8-10 reps", "90s"),
                    ex(isGym ? "Lat Pulldowns" : "Chin-ups", "4 sets", "10-12 reps", "75s"),
                    ex("Dumbbell Lateral Raises (Drop-set on last set)", "4 sets", "12-15 reps", "45s"),
                    ex("Dips on Parallel Bars", "3 sets", "10-12 reps", "60s"),
                    ex("Hammer Curls superset with Overhead Cable Tricep Ext", "3 sets", "12 reps", "45s")));
            splits.add(day("Day 4: Friday", "Lower Body Hypertrophy & Core Conditioning", duration,
                    ex(isGym ? "Front Squats or Hack Squats" : "Goblet Squats with 3s tempo", "4 sets", "8-10 reps", "90s"),
                    ex(isGym ? "Lying Leg Curls" : "Single-Leg Glute Bridges", "4 sets", "12 reps", "60s"),
                    ex("Bulgarian Split Squats", "3 sets", "10 reps / leg", "60s"),
                    ex("Seated Calf Raises", "4 sets", "15 reps", "45s"),
                    ex("Hanging Knee Raises & Plank Variations", "3 sets", "15 reps", "45s")));
        } else {
            splits.add(day("Day 1: Monday", "Push A (Chest, Front/Side Delts, Triceps)", duration,
                    ex("Flat Barbell Bench Press", "4 sets", "6-8 reps", "90s"),
                    ex("Incline Dumbbell Press", "4 sets", "8-10 reps", "75s"),
                    ex("Standing Dumbbell Overhead Press", "3 sets", "8-10 reps", "75s"),
                    ex("Lateral Raises (Cable or Dumbbell)", "4 sets", "12-15 reps", "45s"),
                    ex("Tricep Rope Pushdowns", "3 sets", "12-15 reps", "45s")));
            splits.add(day("Day 2: Tuesday", "Pull A (Lats, Upper Back, Rear Delts, Biceps)", duration,
                    ex("Deadlift or Weighted Pull-ups", "4 sets", "6 reps", "120s"),
                    ex("Chest-Supported Row", "4 sets", "8-10 reps", "90s"),
                    ex("Single-Arm Dumbbell Row", "3 sets", "10-12 reps", "60s"),
                    ex("Face Pulls (Rear Delts)", "4 sets", "15 reps", "45s"),
                    ex("Incline Dumbbell Bicep Curls", "3 sets", "10-12 reps", "45s")));
            splits.add(day("Day 3: Wednesday", "Legs & Abs A (Quads, Hamstrings, Calves)", duration,
                    ex("Barbell Back Squat", "4 sets", "6-8 reps", "120s"),
                    ex("Romanian Deadlift", "4 sets", "8-10 reps", "90s"),
                    ex("Leg Press or Walking Lunges", "3 sets", "12 reps", "60s"),
                    ex("Leg Extension & Curl Superset", "3 sets", "12-15 reps", "60s"),
                    ex("Hanging Leg Raises", "3 sets", "15 reps", "45s")));
            splits.add(day("Day 4: Thursday (or Friday)", "Push B (Incline Focus & Tricep Overload)", duration,
                    ex("Incline Barbell Bench Press", "4 sets", "6-8 reps", "90s"),
                    ex("Weighted Dips / Chest Flyes", "4 sets", "10-12 reps", "75s"),
                    ex("Seated Dumbbell Shoulder Press", "3 sets", "10 reps", "75s"),
                    ex("Lean-Away Lateral Raises", "4 sets", "15 reps", "45s"),
                    ex("Overhead Skullcrushers", "3 sets", "10-12 reps", "45s")));
            splits.add(day("Day 5: Friday (or Saturday)", "Pull B & Arms Focus (Width & Density)", duration,
                    ex("Wide-Grip Lat Pulldowns", "4 sets", "8-10 reps", "90s"),
                    ex("Barbell Bent-Over Row (Underhand)", "4 sets", "8-10 reps", "75s"),
                    ex("Cable Straight-Arm Pulldowns", "3 sets", "12-15 reps", "60s"),
                    ex("Hammer Curls (Forearms & Brachialis)", "4 sets", "10-12 reps", "45s"),
                    ex("Preacher Curls / Concentration Curls", "3 sets", "12 reps", "45s")));
        }
        return splits;
    }

    private record Applied(List<TrainingDay> split, List<String> log) {}

    private static Applied applyConstraints(List<TrainingDay> splits, AnalysisResult.Contraindications contra) {
        List<String> excluded = contra.excludedPatterns().stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();
        String mode = contra.equipmentMode() != null ? contra.equipmentMode() : "gym";
        int cap = contra.maxExercisesPerSession() > 0 ? contra.maxExercisesPerSession() : 6;
        boolean noImpact = contra.noHighImpact();
        List<String> log = new ArrayList<>();

        // pattern -> substrings to match in an exercise name
        java.util.Map<String, List<String>> triggers = new java.util.HashMap<>();
        triggers.put("loaded spinal flexion", List.of("jefferson curl", "weighted sit-up", "weighted crunch"));
        triggers.put("heavy conventional deadlift", List.of("deadlift"));
        triggers.put("good morning", List.of("good morning"));
        triggers.put("bent-over barbell row", List.of("bent-over", "bent over"));
        triggers.put("heavy loaded carries", List.of("farmer carry", "loaded carry"));
        triggers.put("behind-the-neck press", List.of("behind the neck", "behind-the-neck"));
        triggers.put("heavy barbell shrug", List.of("shrug"));
        triggers.put("barbell overhead press", List.of("overhead press", "military press", "overhead barbell"));
        triggers.put("upright row", List.of("upright row"));
        triggers.put("behind-the-neck", List.of("behind the neck", "behind-the-neck"));
        triggers.put("deep weighted dip", List.of("dip"));
        triggers.put("wide-grip bench", List.of("wide-grip bench", "wide grip bench"));
        triggers.put("skullcrusher", List.of("skullcrusher", "skull crusher"));
        triggers.put("heavy close-grip bench", List.of("close-grip bench", "close grip bench"));
        triggers.put("weighted dip", List.of("dip"));
        triggers.put("heavy barbell curl", List.of("barbell curl"));
        triggers.put("wide-stance squat", List.of("sumo squat", "wide stance"));
        triggers.put("heavy deadlift", List.of("deadlift"));
        triggers.put("deep loaded knee flexion", List.of("sissy squat"));
        triggers.put("plyometric jump", List.of("plyometric", "box jump", "depth jump", "jump squat"));
        triggers.put("sissy squat", List.of("sissy squat"));
        triggers.put("leg extension (heavy)", List.of("leg extension"));
        triggers.put("high-impact running", List.of("running", "sprint", "jog"));
        triggers.put("jump rope", List.of("jump rope", "skipping"));

        java.util.Map<String, PlannedExercise> swaps = new java.util.HashMap<>();
        swaps.put("deadlift", ex("Hip Thrust / Glute Bridge", "3 sets", "10-12 reps", "75s"));
        swaps.put("good morning", ex("Back Extension (bodyweight)", "3 sets", "12-15 reps", "60s"));
        swaps.put("bent-over", ex("Chest-Supported Row", "4 sets", "10-12 reps", "75s"));
        swaps.put("barbell row", ex("Chest-Supported Row", "4 sets", "10-12 reps", "75s"));
        swaps.put("overhead press", ex("Landmine Press / Incline DB Press", "3 sets", "10-12 reps", "75s"));
        swaps.put("behind-the-neck", ex("Front Lat Pulldown", "4 sets", "10-12 reps", "60s"));
        swaps.put("upright row", ex("Cable Lateral Raise", "4 sets", "12-15 reps", "45s"));
        swaps.put("dip", ex("Machine / Band Chest Press", "3 sets", "12 reps", "60s"));
        swaps.put("skullcrusher", ex("Rope Tricep Pushdown", "3 sets", "12-15 reps", "45s"));
        swaps.put("close-grip bench", ex("Rope Tricep Pushdown", "3 sets", "12-15 reps", "45s"));
        swaps.put("plyometric", ex("Tempo Goblet Squat", "3 sets", "10 reps", "60s"));
        swaps.put("sissy squat", ex("Leg Press (controlled range)", "3 sets", "12-15 reps", "60s"));
        swaps.put("running", ex("Cycling / Rowing intervals", "1 block", "15-20 min", "-"));
        swaps.put("jump", ex("Sled Push / Cycling sprint", "4 sets", "20s", "90s"));
        swaps.put("squat", ex("Leg Press / Goblet Squat", "4 sets", "10-12 reps", "90s"));

        List<PlannedExercise> fillers = List.of(
                ex("Machine Chest Press", "3 sets", "10-12 reps", "60s"),
                ex("Seated Cable Row", "3 sets", "10-12 reps", "60s"),
                ex("Leg Press", "3 sets", "12-15 reps", "60s"),
                ex("Plank", "3 sets", "45s hold", "45s"));

        List<TrainingDay> out = new ArrayList<>();
        for (TrainingDay d : splits) {
            List<PlannedExercise> kept = new ArrayList<>();
            for (PlannedExercise e : d.exercises()) {
                String n = e.name().toLowerCase(Locale.ROOT);
                String hit = null;
                outer:
                for (String pattern : excluded) {
                    for (String sub : triggers.getOrDefault(pattern, List.of(pattern))) {
                        if (!sub.isEmpty() && n.contains(sub)) {
                            hit = pattern;
                            break outer;
                        }
                    }
                }
                if (hit == null && noImpact
                        && (n.contains("jump") || n.contains("plyo") || n.contains("sprint") || n.contains("running"))) {
                    hit = "high-impact";
                }
                if (hit == null) {
                    kept.add(e);
                    continue;
                }
                PlannedExercise sub = null;
                for (var entry : swaps.entrySet()) {
                    if (n.contains(entry.getKey())) {
                        sub = entry.getValue();
                        break;
                    }
                }
                if (sub != null) {
                    log.add("\"" + e.name() + "\" -> \"" + sub.name() + "\" (contraindication: " + hit + ")");
                    kept.add(sub);
                } else {
                    log.add("\"" + e.name() + "\" removed (contraindication: " + hit + ")");
                }
            }
            int fi = 0;
            while (kept.size() < 3 && fi < fillers.size()) {
                kept.add(fillers.get(fi++));
            }
            if (kept.size() > cap) {
                log.add("\"" + d.day() + "\" reduced to " + cap + " exercises (time limit)");
                kept = new ArrayList<>(kept.subList(0, cap));
            }
            String title = "gym".equals(mode) ? d.title() : d.title() + " - " + capitalize(mode) + " adapted";
            out.add(new TrainingDay(d.day(), title, d.duration(), kept));
        }
        return new Applied(out, log.stream().distinct().toList());
    }


    private static final java.util.Map<Integer, List<String>> MEAL_NAMES = java.util.Map.of(
            2, List.of("Meal 1 (Power Brunch / Post-Fast)", "Meal 2 (Nutrient Dense Dinner)"),
            3, List.of("Meal 1: Breakfast / Morning Kickoff", "Meal 2: Lunch / Sustained Energy", "Meal 3: Dinner / Recovery Fuel"),
            4, List.of("Meal 1: High Protein Breakfast", "Meal 2: Anabolic Lunch", "Meal 3: Pre-Workout Power Meal", "Meal 4: Dinner & Overnight Recovery"),
            5, List.of("Meal 1: Breakfast", "Meal 2: Mid-Morning Fuel", "Meal 3: Core Lunch", "Meal 4: Pre-Workout Boost", "Meal 5: Dinner & Casein Bedtime Snack"),
            6, List.of("Meal 1: Breakfast", "Meal 2: Morning Snack", "Meal 3: Lunch", "Meal 4: Pre-Workout", "Meal 5: Post-Workout Fuel", "Meal 6: Evening Dinner"));

    private static final java.util.Map<String, List<String>> DIET_SUGGESTIONS = java.util.Map.of(
            "high_protein", List.of(
                    "Oatmeal with whey isolate, blueberries, chia seeds, and 3 whole scrambled eggs.",
                    "Grilled chicken breast with brown rice, steamed broccoli, and avocado slices.",
                    "Greek yogurt with a handful of almonds, honey, and rice cakes with peanut butter.",
                    "Lean beef sirloin / Salmon fillet with roasted sweet potatoes and asparagus."),
            "vegetarian", List.of(
                    "Tofu scramble with spinach, whole grain toast, and protein chia pudding.",
                    "Lentil and chickpea curry with quinoa, pumpkin seeds, and mixed greens.",
                    "Cottage cheese / Greek yogurt with walnuts, banana, and hemp hearts.",
                    "Grilled tempeh or seitan with brown rice, edamame, and tahini salad."),
            "keto", List.of(
                    "4 eggs fried in butter with avocado, bacon, and sauteed spinach.",
                    "Grilled salmon with creamy cauliflower mash and olive oil salad.",
                    "Macadamia nuts, string cheese, and protein collagen shake.",
                    "Ribeye steak with garlic butter and grilled asparagus."),
            "balanced", List.of(
                    "Whole wheat toast with scrambled eggs, turkey ham, and fruit smoothie.",
                    "Tuna or turkey wrap with hummus, lettuce, tomato, and sweet potato fries.",
                    "Apple slices with almond butter and a scoop of protein shake.",
                    "Baked chicken thigh with wild rice, zucchini, and olive oil dressing."));

    private static List<Meal> generateMealPlan(int mealsPerDay, String dietPref, int totalCals,
                                               int protein, int carbs, int fats) {
        int calPerMeal = Math.round((float) totalCals / mealsPerDay);
        int proteinPerMeal = Math.round((float) protein / mealsPerDay);
        int carbPerMeal = Math.round((float) carbs / mealsPerDay);
        int fatPerMeal = Math.round((float) fats / mealsPerDay);

        List<String> names = MEAL_NAMES.getOrDefault(mealsPerDay, MEAL_NAMES.get(3));
        List<String> foods = DIET_SUGGESTIONS.getOrDefault(
                DIET_SUGGESTIONS.containsKey(dietPref) ? dietPref : "high_protein",
                DIET_SUGGESTIONS.get("high_protein"));

        List<Meal> meals = new ArrayList<>();
        for (int i = 0; i < mealsPerDay; i++) {
            String name = i < names.size() ? names.get(i) : "Meal " + (i + 1);
            String food = foods.get(i % foods.size());
            meals.add(new Meal(i + 1, name, calPerMeal, proteinPerMeal, carbPerMeal, fatPerMeal, food));
        }
        return meals;
    }


    private static Guidelines generateGuidelines() {
        return new Guidelines(
                "Drink at least 500ml of water right after waking up and 750ml during workouts.",
                "Target 7.5 to 8.5 hours of uninterrupted sleep for optimal growth hormone release and CNS recovery.",
                List.of("Creatine Monohydrate (5g daily) for intracellular ATP and power output",
                        "Whey / Plant Protein Isolate for convenient post-workout macro hits",
                        "Omega-3 Fish Oil (2000mg EPA/DHA) for joint lubrication & reduced inflammation",
                        "Vitamin D3 + K2 (5000 IU) & Magnesium Glycinate before bed for deep sleep"),
                "Track your daily habits on the dashboard every evening to build long-term momentum.");
    }


    private static String bmiCategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 24.9) return "Healthy Weight (Optimal)";
        if (bmi < 29.9) return "Overweight";
        return "Obese (High Body Mass)";
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return "";
    }

    private static String capitalize(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static double round1(double d) {
        return Math.round(d * 10.0) / 10.0;
    }
}
