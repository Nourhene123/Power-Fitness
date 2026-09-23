package com.powerfitness.dto;

import com.powerfitness.common.domain.PlanContent;
import java.time.Instant;
import java.util.List;

public record ProgramVersionDetailDto(
        Long id,
        int versionNo,
        String status,
        PlanContent content,
        List<String> changelog,
        String coachNote,
        Instant createdAt,
        Instant activatedAt) {}
