package ru.leti.wise.task.task.repository;

import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.leti.wise.task.task.model.solution.Solution;

import java.util.UUID;

@Observed
public interface SolutionRepository extends JpaRepository<Solution, UUID>, JpaSpecificationExecutor<Solution> {

    void deleteByTaskId(UUID taskId);
}
