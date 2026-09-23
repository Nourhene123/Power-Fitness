package com.powerfitness.service;

import com.powerfitness.dto.CoachRequestDto;
import com.powerfitness.dto.CoachRequestStatsDto;
import com.powerfitness.dto.CreateCoachRequestDto;
import java.util.List;

public interface CoachRequestService {

    /** Public: record a new "request a coach" submission. */
    CoachRequestDto submit(CreateCoachRequestDto request);

    /** Admin: every request, newest first. */
    List<CoachRequestDto> findAll();

    CoachRequestStatsDto stats();

    CoachRequestDto updateStatus(Long id, String status);

    void delete(Long id);
}
