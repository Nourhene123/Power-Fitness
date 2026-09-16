package com.powerfitness.Mapper;

import com.powerfitness.DTO.ExerciseSummaryDto;
import com.powerfitness.Entity.Exercise;
import org.mapstruct.Mapper;

@Mapper
public interface ExerciseMapper {

    ExerciseSummaryDto toDto(Exercise exercise);
}
