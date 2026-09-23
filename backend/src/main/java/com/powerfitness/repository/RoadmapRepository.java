package com.powerfitness.repository;

import com.powerfitness.entity.CoachStatus;
import com.powerfitness.entity.Roadmap;
import com.powerfitness.entity.RoadmapStatus;
import com.powerfitness.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    Optional<Roadmap> findFirstByUserAndStatusAndCoachStatusOrderByCreatedAtDesc(
            User user, RoadmapStatus status, CoachStatus coachStatus);

    Optional<Roadmap> findFirstByProgramVersionIdIn(List<Long> programVersionIds);

    List<Roadmap> findByStatusAndCoachStatusOrderByCreatedAtAsc(RoadmapStatus status, CoachStatus coachStatus);

    long countByStatusAndCoachStatus(RoadmapStatus status, CoachStatus coachStatus);
}
