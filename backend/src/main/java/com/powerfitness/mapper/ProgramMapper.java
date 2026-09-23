package com.powerfitness.mapper;

import com.powerfitness.dto.MyProgramDto;
import com.powerfitness.entity.Program;
import com.powerfitness.entity.ProgramStatus;
import com.powerfitness.entity.ProgramVersion;
import com.powerfitness.common.domain.PlanContent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ProgramMapper {

    @Mapping(target = "state", expression = "java(stateOf(program.getStatus()))")
    @Mapping(target = "programStatus", source = "program.status")
    @Mapping(target = "versionNo", source = "version.versionNo")
    @Mapping(target = "submittedAt", source = "version.createdAt")
    @Mapping(target = "coachNote", source = "version.coachNote")
    @Mapping(target = "content", source = "content")
    MyProgramDto toDto(Program program, ProgramVersion version, PlanContent content);

    default String stateOf(ProgramStatus status) {
        return switch (status) {
            case ACTIVE -> "ACTIVE";
            case CHANGES_REQUESTED -> "CHANGES_REQUESTED";
            case COMPLETED, ARCHIVED, SUPERSEDED -> "NONE";
            default -> "IN_REVIEW";
        };
    }
}
