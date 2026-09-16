package com.powerfitness.Repository;

import com.powerfitness.Entity.Program;
import com.powerfitness.Entity.ProgramReviewEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramReviewEventRepository extends JpaRepository<ProgramReviewEvent, Long> {

    List<ProgramReviewEvent> findByProgramOrderByCreatedAtDesc(Program program);
}
