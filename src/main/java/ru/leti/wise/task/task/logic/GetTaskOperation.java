package ru.leti.wise.task.task.logic;

import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.task.TaskGrpc.GetTaskResponse;
import ru.leti.wise.task.task.error.BusinessException;
import ru.leti.wise.task.task.mapper.TaskMapper;
import ru.leti.wise.task.task.repository.TaskRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetTaskOperation {

    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;

    public GetTaskResponse activate(UUID id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        Status.NOT_FOUND,
                        "Задача с id '%s' не найдена".formatted(id)
                ));
        return GetTaskResponse.newBuilder()
                .setTask(taskMapper.toTask(task))
                .build();
    }
}
