package com.powerfitness.DTO;

import java.time.Instant;
import java.util.List;

public record CoachDashboardDto(
        Stats stats,
        List<PendingReview> pending,
        List<ApprovedRoadmap> recentlyApproved,
        List<RosterEntry> roster) {

    public record Stats(long pending, long approved, long totalClients, long atRisk) {}

    public record PendingReview(
            Long programId,
            Long versionId,
            Long userId,
            String userName,
            String userEmail,
            String primaryGoal,
            String experienceLevel,
            int workoutFrequency,
            Instant submittedAt,
            Instant userJoinedAt) {}

    public record ApprovedRoadmap(
            Long programId,
            Long userId,
            String userName,
            String userEmail,
            String primaryGoal,
            Instant approvedAt) {}

    public record RosterEntry(
            Long userId,
            String userName,
            String userEmail,
            String primaryGoal,
            boolean atRisk,
            List<String> riskReasons) {}
}
