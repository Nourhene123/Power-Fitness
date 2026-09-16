package com.powerfitness.Repository;

import com.powerfitness.Entity.Assessment;
import com.powerfitness.Entity.AssessmentAnalysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentAnalysisRepository extends JpaRepository<AssessmentAnalysis, Long> {

    Optional<AssessmentAnalysis> findByAssessment(Assessment assessment);
}
