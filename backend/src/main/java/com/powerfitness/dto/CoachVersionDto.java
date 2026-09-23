package com.powerfitness.dto;

import com.powerfitness.common.domain.PlanContent;
import java.util.List;

public record CoachVersionDto(
        Long versionId,
        Long programId,
        Long clientUserId,
        int versionNo,
        String status,
        boolean locked,
        String coachNote,
        PlanContent content,
        List<String> liveChangelog) {}
