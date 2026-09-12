package ru.leti.wise.task.task.repository;

import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import ru.leti.wise.task.task.model.solution.Solution;

import java.util.List;
import java.util.UUID;

@Observed
public interface SolutionRepository extends JpaRepository<Solution, UUID> {

    List<Solution> findSolutionEntitiesByAuthorIdAndTaskId(UUID authorId, UUID taskId);
    void deleteByTaskId(UUID taskId);
    List<Solution> findSolutionEntitiesByAuthorId(UUID authorId);
}
