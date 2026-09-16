package com.powerfitness.Mapper;

import com.powerfitness.DTO.AssessmentSummaryDto;
import com.powerfitness.Entity.Assessment;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper
public interface AssessmentSummaryMapper {

    @Mapping(target = "equipment", source = "equipment")
    @Mapping(target = "allergies", source = "allergies")
    @Mapping(target = "dislikedFoods", source = "dislikedFoods")
    @Mapping(target = "lovedExercises", source = "lovedExercises")
    @Mapping(target = "hatedExercises", source = "hatedExercises")
    @Mapping(target = "injuries", source = "injuries")
    AssessmentSummaryDto toDto(Assessment a, List<String> equipment, List<AssessmentSummaryDto.InjuryDto> injuries,
                              String allergies, String dislikedFoods, String lovedExercises, String hatedExercises);
}
