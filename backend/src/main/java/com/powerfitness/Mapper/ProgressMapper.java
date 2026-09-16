package com.powerfitness.Mapper;

import com.powerfitness.DTO.ProgressLogDto;
import com.powerfitness.DTO.WorkoutSessionDto;
import com.powerfitness.Entity.ProgressLog;
import com.powerfitness.Entity.WorkoutSession;
import org.mapstruct.Mapper;

@Mapper
public interface ProgressMapper {

    WorkoutSessionDto toDto(WorkoutSession session);

    ProgressLogDto toDto(ProgressLog log);
}
