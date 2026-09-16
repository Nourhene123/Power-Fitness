package com.powerfitness.Services.Interface;

import com.powerfitness.DTO.CoachDashboardDto;

/** {@code GET /coach/dashboard} — port of {@code coach/dashboard.php}. */
public interface CoachDashboardService {

    CoachDashboardDto dashboard();
}
