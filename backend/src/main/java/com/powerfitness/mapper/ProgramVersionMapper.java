package com.powerfitness.mapper;

import com.powerfitness.dto.CoachVersionDto;
import com.powerfitness.dto.ProgramVersionDetailDto;
import com.powerfitness.dto.ProgramVersionSummaryDto;
import com.powerfitness.entity.ProgramVersion;
import com.powerfitness.common.domain.PlanContent;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ProgramVersionMapper {

    @Mapping(target = "changelog", source = "parsedChangelog")
    @Mapping(target = "current", expression = "java(v.getId().equals(currentId))")
    ProgramVersionSummaryDto toSummaryDto(ProgramVersion v, List<String> parsedChangelog, Long currentId);

    @Mapping(target = "content", source = "content")
    @Mapping(target = "changelog", source = "parsedChangelog")
    ProgramVersionDetailDto toDetailDto(ProgramVersion v, PlanContent content, List<String> parsedChangelog);

    @Mapping(target = "versionId", source = "v.id")
    @Mapping(target = "programId", source = "v.program.id")
    @Mapping(target = "clientUserId", source = "v.program.user.id")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "liveChangelog", source = "liveChangelog")
    CoachVersionDto toCoachVersionDto(ProgramVersion v, PlanContent content, List<String> liveChangelog);
}
