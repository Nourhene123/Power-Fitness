package com.powerfitness.DTO;

import com.powerfitness.common.domain.PlanContent;
import jakarta.validation.constraints.NotNull;

public record SaveDraftRequest(@NotNull PlanContent content, String coachNote) {}
