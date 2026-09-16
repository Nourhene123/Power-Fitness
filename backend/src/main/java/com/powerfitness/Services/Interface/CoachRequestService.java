package com.powerfitness.Services.Interface;

import com.powerfitness.DTO.CoachRequestDto;
import com.powerfitness.DTO.CoachRequestStatsDto;
import com.powerfitness.DTO.CreateCoachRequestDto;
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
