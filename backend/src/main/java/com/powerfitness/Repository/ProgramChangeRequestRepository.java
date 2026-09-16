package com.powerfitness.Repository;

import com.powerfitness.Entity.Program;
import com.powerfitness.Entity.ProgramChangeRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramChangeRequestRepository extends JpaRepository<ProgramChangeRequest, Long> {

    Optional<ProgramChangeRequest> findFirstByProgramAndResolvedFalseOrderByIdDesc(Program program);
}
