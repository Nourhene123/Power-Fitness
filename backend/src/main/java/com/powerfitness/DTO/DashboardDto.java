package com.powerfitness.DTO;

import java.util.List;


public record DashboardDto(
        String state,
        boolean hasAssessment,
        ChangeRequestVm changeRequest,
        PreviewVm preview,
        GoalVm goal,
        PhaseVm phase,
        TodayVm today,
        ProgressVm progress,
        CoachVm coach,
        MilestoneVm milestone) {

    public static DashboardDto none(boolean hasAssessment) {
        return new DashboardDto("NONE", hasAssessment, null, null, null, null, null, null, null, null);
    }

    public record ChangeRequestVm(Long programId, String coachNote, List<ChangeFieldDto> fields) {}

  
    public record PreviewVm(Integer targetCalories, Integer splitDays) {}

    public record GoalVm(
            String line,
            Boolean onTrack,
            double startKg,
            double currentKg,
            double targetKg,
            double toGoKg,
            int progressPct,
            String direction) {}

    public record PhaseVm(
            String name,
            int version,
            int weekNo,
            int totalWeeks,
            String phaseLabel,
            String phaseShort) {}

    public record TodayVm(
            boolean isRest,
            Integer sessionIdx,
            SessionVm session,
            boolean doneToday,
            int calories,
            int proteinG,
            int carbsG,
            int fatsG,
            double waterL,
            HabitsVm habits) {

        public record SessionVm(String title, int exercises, String duration) {}

        public record HabitsVm(boolean water, boolean workout, boolean nutrition, boolean sleep) {}
    }

    public record ProgressVm(
            List<WeightPointVm> weightSeries,
            List<WeekAdherenceVm> weeks,
            int streak,
            double goalBandLow,
            double goalBandHigh) {

        public record WeightPointVm(String date, double value) {}

        public record WeekAdherenceVm(String label, int workout, int adherencePct) {}
    }

    public record CoachVm(String note, String focus, String focusSource, long unread) {}

    public record MilestoneVm(String label, String date, Integer inDays) {}
}
