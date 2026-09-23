package com.powerfitness.service;

import com.powerfitness.dto.DashboardDto;

/** Assembles the client dashboard view-model. Port of {@code config/dashboard_data.php}. */
public interface DashboardService {

    DashboardDto build(Long userId);
}
