package com.powerfitness.Repository;

import com.powerfitness.Entity.ProgressLog;
import com.powerfitness.Entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressLogRepository extends JpaRepository<ProgressLog, Long> {

    Optional<ProgressLog> findByUserAndLoggedOn(User user, LocalDate loggedOn);

    List<ProgressLog> findByUserOrderByLoggedOnDesc(User user);

    List<ProgressLog> findByUserAndLoggedOnGreaterThanEqualOrderByLoggedOnAsc(User user, LocalDate from);

    Optional<ProgressLog> findFirstByUserAndWeightKgIsNotNullOrderByLoggedOnDesc(User user);
}
