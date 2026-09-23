package com.powerfitness.mapper;

import com.powerfitness.dto.CoachRequestDto;
import com.powerfitness.dto.CreateCoachRequestDto;
import com.powerfitness.entity.CoachRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CoachRequestMapper {

    CoachRequestDto toDto(CoachRequest entity);

    @Mapping(target = "status", constant = "pending")
    @Mapping(target = "experience", source = "experience", defaultValue = "beginner")
    CoachRequest toEntity(CreateCoachRequestDto dto);
}
