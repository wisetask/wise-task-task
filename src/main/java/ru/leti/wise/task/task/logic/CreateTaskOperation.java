package ru.leti.wise.task.task.logic;

import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.plugin.PluginOuterClass;
import ru.leti.wise.task.task.TaskGrpc.CreateTaskRequest;
import ru.leti.wise.task.task.TaskGrpc.CreateTaskResponse;
import ru.leti.wise.task.task.TaskOuterClass;
import ru.leti.wise.task.task.error.BusinessException;
import ru.leti.wise.task.task.mapper.TaskMapper;
import ru.leti.wise.task.task.model.task.TaskType;
import ru.leti.wise.task.task.repository.TaskRepository;
import ru.leti.wise.task.task.service.grpc.graph.GraphGrpcService;
import ru.leti.wise.task.task.service.grpc.plugin.PluginGrpcService;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CreateTaskOperation {

    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;
    private final GraphGrpcService graphGrpcService;
    private final PluginGrpcService pluginGrpcService;

    public CreateTaskResponse activate(CreateTaskRequest request) {
        var task = request.getTask();
        if (task.hasTaskGraph()) {
            return createTaskGraph(task);
        } else if (task.hasTaskImplementation()) {
            return createTaskImplementation(task);
        }
        throw new BusinessException(Status.INVALID_ARGUMENT, "Неизвестный тип задачи. Доступные типы задач: " +
                TaskType.getStringTypes()
        );

    }

    private CreateTaskResponse createTaskGraph(TaskOuterClass.Task task) {
        var taskGraph = task.getTaskGraph();
        var pluginIds = taskGraph.getConditionList().stream().map(
                TaskOuterClass.PluginInfo::getPluginId
        ).toList();
        checkPlugins(pluginIds, taskGraph.getGraphType());
        if (taskGraph.hasGraph()) {
            graphGrpcService.createGraph(taskGraph.getGraph());
        }
        var taskEntity = taskMapper.toTaskGraph(task);
        taskRepository.save(taskEntity);
        return CreateTaskResponse.newBuilder()
                .setTask(task).build();
    }

    private CreateTaskResponse createTaskImplementation(TaskOuterClass.Task task) {
        var taskImplement = task.getTaskImplementation();
        checkPlugins(List.of(taskImplement.getPluginId()), null);
        var taskEntity = taskMapper.toTaskImplementation(task);
        taskRepository.save(taskEntity);
        return CreateTaskResponse.newBuilder()
                .setTask(task).build();
    }

    public void checkPlugins(List<String> pluginIds, PluginOuterClass.GraphType graphType) {
        var pluginsMap = pluginGrpcService.getPluginsByIds(pluginIds)
                .stream().collect(Collectors.toMap(
                        PluginOuterClass.Plugin::getId,
                        Function.identity()
                ));

        pluginIds.forEach(pluginId -> {
                    if (pluginsMap.containsKey(pluginId)) {
                        throw new BusinessException(Status.INVALID_ARGUMENT, "Плагин с id: %s не найден".formatted(pluginId));
                    }
                    var plugin = pluginsMap.get(pluginId);
                    if (graphType != null && !plugin.getGraphType().equals(graphType)) {
                        throw new BusinessException(Status.INVALID_ARGUMENT,
                                "Плагин c id: %s имеет не корректный тип графа".formatted(pluginId));
                    }
                }
        );
    }
}
