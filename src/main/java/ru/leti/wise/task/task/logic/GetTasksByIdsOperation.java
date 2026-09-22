package ru.leti.wise.task.task.logic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.task.TaskGrpc.GetTasksByIdsResponse;
import ru.leti.wise.task.task.TaskGrpc.TaskIds;
import ru.leti.wise.task.task.mapper.TaskMapper;
import ru.leti.wise.task.task.model.task.Task;
import ru.leti.wise.task.task.repository.TaskRepository;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetTasksByIdsOperation {

    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;

    public GetTasksByIdsResponse activate(TaskIds request) {
        var ids = request.getTaskIdsList()
                .stream()
                .filter(id -> !id.isBlank())
                .map(UUID::fromString)
                .toList();
        var tasks = ids.isEmpty() ? List.<Task>of() : taskRepository.findAllById(ids);
        return GetTasksByIdsResponse.newBuilder()
                .addAllTasks(taskMapper.toTasks(tasks))
                .build();
    }
}
