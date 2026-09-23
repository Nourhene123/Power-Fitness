package com.powerfitness.service;

import com.powerfitness.dto.CoachDashboardDto;

/** {@code GET /coach/dashboard} — port of {@code coach/dashboard.php}. */
public interface CoachDashboardService {

    CoachDashboardDto dashboard();
}
