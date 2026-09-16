package com.powerfitness.Repository;

import com.powerfitness.Entity.CoachStatus;
import com.powerfitness.Entity.Roadmap;
import com.powerfitness.Entity.RoadmapStatus;
import com.powerfitness.Entity.User;
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
