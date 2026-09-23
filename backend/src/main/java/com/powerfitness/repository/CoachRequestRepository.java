package com.powerfitness.repository;

import com.powerfitness.entity.CoachRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachRequestRepository extends JpaRepository<CoachRequest, Long> {

    List<CoachRequest> findAllByOrderByCreatedAtDesc();

    long countByStatus(String status);
}
