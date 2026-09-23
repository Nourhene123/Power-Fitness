package com.powerfitness.repository;

import com.powerfitness.entity.Exercise;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    List<Exercise> findAllByOrderByCategoryAscNameAsc();

    boolean existsByName(String name);
}
