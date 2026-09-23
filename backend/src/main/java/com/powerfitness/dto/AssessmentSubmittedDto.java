package com.powerfitness.dto;

public record AssessmentSubmittedDto(
        Long assessmentId,
        Long analysisId,
        Long programId,
        Long versionId,
        String programStatus,
        String roadmapTitle) {}
