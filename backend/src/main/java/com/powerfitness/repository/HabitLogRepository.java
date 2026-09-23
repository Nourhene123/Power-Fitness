package com.powerfitness.repository;

import com.powerfitness.entity.HabitLog;
import com.powerfitness.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {

    Optional<HabitLog> findByUserAndLogDate(User user, LocalDate logDate);

    List<HabitLog> findByUserAndLogDateBetweenOrderByLogDateAsc(User user, LocalDate from, LocalDate to);
}
