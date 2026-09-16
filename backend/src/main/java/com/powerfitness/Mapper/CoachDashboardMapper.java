package com.powerfitness.Mapper;

import com.powerfitness.DTO.CoachDashboardDto;
import com.powerfitness.Entity.Program;
import com.powerfitness.Entity.ProgramVersion;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper
public interface CoachDashboardMapper {

    @Mapping(target = "programId", source = "p.id")
    @Mapping(target = "versionId", source = "version.id")
    @Mapping(target = "userId", source = "p.user.id")
    @Mapping(target = "userName", source = "p.user.name")
    @Mapping(target = "userEmail", source = "p.user.email")
    @Mapping(target = "primaryGoal", source = "p.primaryGoal")
    @Mapping(target = "experienceLevel", source = "p.assessment.experienceLevel")
    @Mapping(target = "workoutFrequency", source = "p.assessment.workoutFrequency")
    @Mapping(target = "submittedAt", source = "p.createdAt")
    @Mapping(target = "userJoinedAt", source = "p.user.createdAt")
    CoachDashboardDto.PendingReview toPendingReview(Program p, ProgramVersion version);

    @Mapping(target = "programId", source = "v.program.id")
    @Mapping(target = "userId", source = "v.program.user.id")
    @Mapping(target = "userName", source = "v.program.user.name")
    @Mapping(target = "userEmail", source = "v.program.user.email")
    @Mapping(target = "primaryGoal", source = "v.program.primaryGoal")
    CoachDashboardDto.ApprovedRoadmap toApprovedRoadmap(ProgramVersion v);

    @Mapping(target = "userId", source = "p.user.id")
    @Mapping(target = "userName", source = "p.user.name")
    @Mapping(target = "userEmail", source = "p.user.email")
    @Mapping(target = "primaryGoal", source = "p.primaryGoal")
    @Mapping(target = "atRisk", source = "atRisk")
    @Mapping(target = "riskReasons", source = "riskReasons")
    CoachDashboardDto.RosterEntry toRosterEntry(Program p, boolean atRisk, List<String> riskReasons);
}
