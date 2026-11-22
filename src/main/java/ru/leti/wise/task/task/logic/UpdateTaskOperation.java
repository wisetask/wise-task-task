package ru.leti.wise.task.task.logic;

import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.task.TaskGrpc;
import ru.leti.wise.task.task.TaskGrpc.UpdateTaskRequest;
import ru.leti.wise.task.task.TaskGrpc.UpdateTaskResponse;
import ru.leti.wise.task.task.TaskOuterClass;
import ru.leti.wise.task.task.error.BusinessException;
import ru.leti.wise.task.task.mapper.TaskMapper;
import ru.leti.wise.task.task.model.task.Task;
import ru.leti.wise.task.task.model.task.TaskGraph;
import ru.leti.wise.task.task.model.task.TaskImplementation;
import ru.leti.wise.task.task.model.task.TaskType;
import ru.leti.wise.task.task.repository.TaskRepository;
import ru.leti.wise.task.task.service.grpc.graph.GraphGrpcService;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateTaskOperation {

    private final TaskRepository taskRepository;
    private final GraphGrpcService graphGrpcService;
    private final CreateTaskOperation createTaskOperation;
    private final TaskMapper taskMapper;

    public UpdateTaskResponse activate(UpdateTaskRequest request) {
        var task = request.getTask();
        var taskId = UUID.fromString(task.getId());
        var taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(
                        Status.NOT_FOUND,
                        "Задача c id %s не существует".formatted(taskId)
                ));
        if (task.hasTaskGraph()) {
            return updateTaskGraph(task, taskEntity);
        } else if (task.hasTaskImplementation()) {
            return updateTaskImplementation(task, taskEntity);
        }
        throw new BusinessException(Status.INVALID_ARGUMENT, "Неизвестный тип задачи. Доступные типы задач: " +
                TaskType.getStringTypes()
        );
    }

    private UpdateTaskResponse updateTaskGraph(TaskOuterClass.Task task, Task taskEntity) {
        if (taskEntity instanceof TaskGraph taskGraphEntity) {
            var taskGraph = task.getTaskGraph();
            var pluginIds = taskGraph.getConditionList().stream().map(
                    TaskOuterClass.PluginInfo::getPluginId
            ).toList();
            createTaskOperation.checkPlugins(pluginIds, taskGraph.getGraphType());
            if (taskGraph.hasGraph()) {
                if (taskGraphEntity.getGraphId() != null) {
                    graphGrpcService.deleteGraph(taskGraphEntity.getGraphId().toString()); // TODO DELETE THROUGH KAFKA
                }
                graphGrpcService.createGraph(taskGraph.getGraph());
            }
            var newTaskEntity = taskMapper.toTaskGraph(task);
            taskRepository.save(newTaskEntity);
            return TaskGrpc.UpdateTaskResponse.newBuilder()
                    .setTask(task)
                    .build();
        }
        throw new BusinessException(Status.INVALID_ARGUMENT, "Тип задачи изменять нельзя");
    }

    private UpdateTaskResponse updateTaskImplementation(TaskOuterClass.Task task, Task taskEntity) {
        if (taskEntity instanceof TaskImplementation) {
            var taskImplement = task.getTaskImplementation();
            createTaskOperation.checkPlugins(List.of(taskImplement.getPluginId()), null);
            var newTaskEntity = taskMapper.toTaskGraph(task);
            taskRepository.save(newTaskEntity);
            return TaskGrpc.UpdateTaskResponse.newBuilder()
                    .setTask(task)
                    .build();
        }
        throw new BusinessException(Status.INVALID_ARGUMENT, "Тип задачи изменять нельзя");
    }
}
