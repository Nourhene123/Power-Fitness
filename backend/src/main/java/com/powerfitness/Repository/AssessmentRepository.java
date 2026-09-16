package com.powerfitness.Repository;

import com.powerfitness.Entity.Assessment;
import com.powerfitness.Entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    Optional<Assessment> findByUserAndLatestTrue(User user);

    boolean existsByUser(User user);

    List<Assessment> findByUserOrderByVersionDesc(User user);

    @Query("select coalesce(max(a.version), 0) from Assessment a where a.user = :user")
    int currentVersion(@Param("user") User user);

    @Modifying
    @Query("update Assessment a set a.latest = false where a.user = :user and a.latest = true")
    int clearLatestFlag(@Param("user") User user);
}
