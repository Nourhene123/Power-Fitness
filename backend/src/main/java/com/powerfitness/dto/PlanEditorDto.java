package com.powerfitness.dto;

import com.powerfitness.common.domain.PlanContent;
import java.util.List;

public record PlanEditorDto(
        Long versionId,
        Long programId,
        int versionNo,
        String status,
        boolean locked,
        String coachNote,
        PlanContent content,
        List<String> liveChangelog,
        ClientProfileDto client,
        List<ExerciseSummaryDto> safeExercises) {}
