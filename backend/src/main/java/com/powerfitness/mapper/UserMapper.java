package com.powerfitness.mapper;

import com.powerfitness.dto.UserDto;
import com.powerfitness.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {

    @Mapping(target = "hasAssessment", source = "hasAssessment")
    UserDto toDto(User user, boolean hasAssessment);
}
