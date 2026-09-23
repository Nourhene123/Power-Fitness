package com.powerfitness.repository;

import com.powerfitness.entity.Assessment;
import com.powerfitness.entity.AssessmentAnalysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentAnalysisRepository extends JpaRepository<AssessmentAnalysis, Long> {

    Optional<AssessmentAnalysis> findByAssessment(Assessment assessment);
}
