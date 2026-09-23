package com.powerfitness.mapper;

import com.powerfitness.dto.ProgressLogDto;
import com.powerfitness.dto.WorkoutSessionDto;
import com.powerfitness.entity.ProgressLog;
import com.powerfitness.entity.WorkoutSession;
import org.mapstruct.Mapper;

@Mapper
public interface ProgressMapper {

    WorkoutSessionDto toDto(WorkoutSession session);

    ProgressLogDto toDto(ProgressLog log);
}
