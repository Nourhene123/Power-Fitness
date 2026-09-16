package com.powerfitness.DTO;

public record AssessmentSubmittedDto(
        Long assessmentId,
        Long analysisId,
        Long programId,
        Long versionId,
        String programStatus,
        String roadmapTitle) {}
