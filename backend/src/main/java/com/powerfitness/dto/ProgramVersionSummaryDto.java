package com.powerfitness.dto;

import java.time.Instant;
import java.util.List;

public record ProgramVersionSummaryDto(
        Long id,
        int versionNo,
        String status,
        String createdBy,
        List<String> changelog,
        String coachNote,
        boolean current,
        Instant createdAt,
        Instant activatedAt) {}
