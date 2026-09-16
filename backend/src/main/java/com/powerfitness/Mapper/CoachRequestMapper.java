package com.powerfitness.Mapper;

import com.powerfitness.DTO.CoachRequestDto;
import com.powerfitness.DTO.CreateCoachRequestDto;
import com.powerfitness.Entity.CoachRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CoachRequestMapper {

    CoachRequestDto toDto(CoachRequest entity);

    @Mapping(target = "status", constant = "pending")
    @Mapping(target = "experience", source = "experience", defaultValue = "beginner")
    CoachRequest toEntity(CreateCoachRequestDto dto);
}
