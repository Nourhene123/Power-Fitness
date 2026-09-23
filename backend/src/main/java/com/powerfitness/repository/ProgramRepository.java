package com.powerfitness.repository;

import com.powerfitness.entity.Program;
import com.powerfitness.entity.ProgramStatus;
import com.powerfitness.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    Optional<Program> findFirstByUserOrderByIdDesc(User user);

    List<Program> findByUserAndStatusNotIn(User user, List<ProgramStatus> statuses);

    long countByStatus(ProgramStatus status);

    @org.springframework.data.jpa.repository.Query(
            "select count(distinct p.user.id) from Program p where p.status = :status")
    long countDistinctClients(ProgramStatus status);

    List<Program> findByStatusOrderByCreatedAtAsc(ProgramStatus status);

    List<Program> findByStatusOrderByUser_NameAsc(ProgramStatus status);
}
