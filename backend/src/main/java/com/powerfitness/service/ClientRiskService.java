package com.powerfitness.service;

import com.powerfitness.entity.User;
import java.util.List;

/**
 * Flags clients whose adherence has slipped — no weigh-in, no workout, or low habit
 * completion recently — so a coach notices without opening every profile.
 */
public interface ClientRiskService {

    record Assessment(boolean atRisk, List<String> reasons) {}

    Assessment assess(User user);
}
