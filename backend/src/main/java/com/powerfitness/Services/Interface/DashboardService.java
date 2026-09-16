package com.powerfitness.Services.Interface;

import com.powerfitness.DTO.DashboardDto;

/** Assembles the client dashboard view-model. Port of {@code config/dashboard_data.php}. */
public interface DashboardService {

    DashboardDto build(Long userId);
}
