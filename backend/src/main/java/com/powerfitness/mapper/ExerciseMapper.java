package com.powerfitness.mapper;

import com.powerfitness.dto.ExerciseSummaryDto;
import com.powerfitness.entity.Exercise;
import org.mapstruct.Mapper;

@Mapper
public interface ExerciseMapper {

    ExerciseSummaryDto toDto(Exercise exercise);
}
