package com.powerfitness.Repository;

import com.powerfitness.Entity.User;
import com.powerfitness.Entity.WorkoutSession;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    List<WorkoutSession> findByUserOrderBySessionDateDescIdDesc(User user);

    List<WorkoutSession> findByUserAndSessionDateGreaterThanEqualOrderBySessionDateDesc(User user, LocalDate from);

    boolean existsByUserAndSessionDate(User user, LocalDate sessionDate);
}
