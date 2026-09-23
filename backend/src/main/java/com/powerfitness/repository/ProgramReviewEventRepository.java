package com.powerfitness.repository;

import com.powerfitness.entity.Program;
import com.powerfitness.entity.ProgramReviewEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramReviewEventRepository extends JpaRepository<ProgramReviewEvent, Long> {

    List<ProgramReviewEvent> findByProgramOrderByCreatedAtDesc(Program program);
}
