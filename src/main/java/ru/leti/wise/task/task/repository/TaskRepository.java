package ru.leti.wise.task.task.repository;

import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.leti.wise.task.task.model.task.Task;
import ru.leti.wise.task.task.model.task.TaskGraph;

import java.util.List;
import java.util.UUID;

@Observed
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {
}
