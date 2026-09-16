package com.powerfitness.Repository;

import com.powerfitness.Entity.Program;
import com.powerfitness.Entity.ProgramStatus;
import com.powerfitness.Entity.ProgramVersion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramVersionRepository extends JpaRepository<ProgramVersion, Long> {

    List<ProgramVersion> findByProgramOrderByVersionNoAsc(Program program);

    Optional<ProgramVersion> findTopByProgramOrderByVersionNoDesc(Program program);

    Optional<ProgramVersion> findTopByProgramAndStatusOrderByVersionNoDesc(Program program, ProgramStatus status);

    List<ProgramVersion> findByProgramAndStatusIn(Program program, List<ProgramStatus> statuses);

    List<ProgramVersion> findTop10ByStatusOrderByApprovedAtDesc(ProgramStatus status);
}
