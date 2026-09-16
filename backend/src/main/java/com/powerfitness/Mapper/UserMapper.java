package com.powerfitness.Mapper;

import com.powerfitness.DTO.UserDto;
import com.powerfitness.Entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {

    @Mapping(target = "hasAssessment", source = "hasAssessment")
    UserDto toDto(User user, boolean hasAssessment);
}
