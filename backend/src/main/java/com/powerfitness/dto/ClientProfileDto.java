package com.powerfitness.dto;

import com.powerfitness.common.domain.AnalysisResult;
import com.powerfitness.common.domain.PlanContent;
import java.time.Instant;
import java.util.List;

public record ClientProfileDto(
        Long userId,
        String name,
        String email,
        Instant joinedAt,
        AssessmentSummaryDto assessment,
        AnalysisResult analysis,
        Long versionId,
        Integer versionNo,
        String versionStatus,
        PlanContent content,
        boolean atRisk,
        List<String> riskReasons) {}
