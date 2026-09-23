package com.powerfitness.repository;

import com.powerfitness.entity.Program;
import com.powerfitness.entity.ProgramChangeRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramChangeRequestRepository extends JpaRepository<ProgramChangeRequest, Long> {

    Optional<ProgramChangeRequest> findFirstByProgramAndResolvedFalseOrderByIdDesc(Program program);
}
