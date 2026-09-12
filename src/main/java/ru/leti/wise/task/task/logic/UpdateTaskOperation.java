package ru.leti.wise.task.task.logic;

import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.task.TaskGrpc;
import ru.leti.wise.task.task.TaskGrpc.UpdateTaskRequest;
import ru.leti.wise.task.task.TaskGrpc.UpdateTaskResponse;
import ru.leti.wise.task.task.error.BusinessException;
import ru.leti.wise.task.task.repository.TaskRepository;
import ru.leti.wise.task.task.service.grpc.graph.GraphGrpcService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateTaskOperation {

    private final TaskRepository taskRepository;
    private final GraphGrpcService graphGrpcService;

    public UpdateTaskResponse activate(UpdateTaskRequest request) {
        var taskId = UUID.fromString(request.getTask().getId());
        var taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(
                        Status.NOT_FOUND,
                        "Задача c id %s не существует".formatted(taskId)
                ));
        if (request.getTask().hasTaskGraph()) {
            graphGrpcService.createGraph(request.getTask().getTaskGraph().getGraph());
        }
        taskRepository.save(taskEntity);
        return TaskGrpc.UpdateTaskResponse.newBuilder()
                .setTask(request.getTask())
                .build();
    }
}
