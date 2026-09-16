package com.powerfitness.Repository;

import com.powerfitness.Entity.HabitLog;
import com.powerfitness.Entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {

    Optional<HabitLog> findByUserAndLogDate(User user, LocalDate logDate);

    List<HabitLog> findByUserAndLogDateBetweenOrderByLogDateAsc(User user, LocalDate from, LocalDate to);
}
