package com.powerfitness.Mapper;

import com.powerfitness.DTO.ClientProgressDto;
import com.powerfitness.Entity.ProgressLog;
import com.powerfitness.Entity.WorkoutSession;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper
public interface ClientProgressMapper {

    @Mapping(target = "date", source = "loggedOn")
    ClientProgressDto.WeightLog toWeightLog(ProgressLog log);

    List<ClientProgressDto.WeightLog> toWeightLogs(List<ProgressLog> logs);

    @Mapping(target = "date", source = "sessionDate")
    ClientProgressDto.WorkoutLog toWorkoutLog(WorkoutSession session);

    List<ClientProgressDto.WorkoutLog> toWorkoutLogs(List<WorkoutSession> sessions);
}
