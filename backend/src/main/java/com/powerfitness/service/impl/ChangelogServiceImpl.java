package com.powerfitness.service.impl;

import com.powerfitness.service.ChangelogService;
import com.powerfitness.common.domain.PlanContent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ChangelogServiceImpl implements ChangelogService {

    @Override
    public List<String> compute(PlanContent oldContent, PlanContent newContent) {
        Set<String> out = new LinkedHashSet<>();

        PlanContent.Metrics om = oldContent != null ? oldContent.metrics() : null;
        PlanContent.Metrics nm = newContent != null ? newContent.metrics() : null;
        if (om != null && nm != null) {
            diffMetric(out, "Calorie target", om.targetCalories(), nm.targetCalories(), " kcal");
            diffMetric(out, "Protein", om.proteinG(), nm.proteinG(), " g");
            diffMetric(out, "Carbs", om.carbsG(), nm.carbsG(), " g");
            diffMetric(out, "Fats", om.fatsG(), nm.fatsG(), " g");
            diffMetric(out, "Water target", om.waterLiters(), nm.waterLiters(), " L");

            if (om.roadmapTitle() != null && nm.roadmapTitle() != null && !om.roadmapTitle().equals(nm.roadmapTitle())) {
                out.add("Plan renamed to “" + nm.roadmapTitle() + "”");
            }
        }

        List<PlanContent.TrainingDay> oldSplit = oldContent != null && oldContent.weeklySplit() != null
                ? oldContent.weeklySplit() : List.of();
        List<PlanContent.TrainingDay> newSplit = newContent != null && newContent.weeklySplit() != null
                ? newContent.weeklySplit() : List.of();
        if (oldSplit.size() != newSplit.size()) {
            out.add("Training days: " + oldSplit.size() + " → " + newSplit.size() + " per week");
        }

        int days = Math.min(oldSplit.size(), newSplit.size());
        for (int d = 0; d < days; d++) {
            Set<String> oldExercises = exerciseNames(oldSplit.get(d));
            Set<String> newExercises = exerciseNames(newSplit.get(d));
            String dayLabel = newSplit.get(d).day() != null ? newSplit.get(d).day() : "Day " + (d + 1);
            for (String removed : oldExercises) {
                if (!newExercises.contains(removed)) out.add(dayLabel + ": removed “" + titleCase(removed) + "”");
            }
            for (String added : newExercises) {
                if (!oldExercises.contains(added)) out.add(dayLabel + ": added “" + titleCase(added) + "”");
            }
        }

        PlanContent.Guidelines og = oldContent != null ? oldContent.guidelines() : null;
        PlanContent.Guidelines ng = newContent != null ? newContent.guidelines() : null;
        diffGuideline(out, "Hydration", og != null ? og.hydration() : null, ng != null ? ng.hydration() : null);
        diffGuideline(out, "Sleep", og != null ? og.sleep() : null, ng != null ? ng.sleep() : null);
        diffGuideline(out, "Consistency rule", og != null ? og.consistencyRule() : null, ng != null ? ng.consistencyRule() : null);

        return new ArrayList<>(out);
    }

    private static void diffMetric(Set<String> out, String label, double oldValue, double newValue, String unit) {
        if (oldValue != newValue) {
            out.add(label + ": " + fmt(oldValue) + unit + " → " + fmt(newValue) + unit);
        }
    }

    private static String fmt(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    private static Set<String> exerciseNames(PlanContent.TrainingDay day) {
        Set<String> names = new LinkedHashSet<>();
        if (day.exercises() != null) {
            for (PlanContent.PlannedExercise ex : day.exercises()) {
                if (ex.name() != null) names.add(ex.name().trim().toLowerCase(Locale.ROOT));
            }
        }
        return names;
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

    private static void diffGuideline(Set<String> out, String label, String oldValue, String newValue) {
        String o = oldValue == null ? "" : oldValue;
        String n = newValue == null ? "" : newValue;
        if (!o.equals(n) && !n.isEmpty()) out.add(label + " guidance updated");
    }
}
