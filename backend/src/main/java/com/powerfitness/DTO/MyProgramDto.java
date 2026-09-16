package com.powerfitness.DTO;

import com.powerfitness.common.domain.PlanContent;
import java.time.Instant;

public record MyProgramDto(
        String state,          
        String programStatus,
        Integer versionNo,
        Instant submittedAt,
        String coachNote,
        PlanContent content) {

    public static MyProgramDto none() {
        return new MyProgramDto("NONE", null, null, null, null, null);
    }
}
