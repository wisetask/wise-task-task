package ru.leti.wise.task.task.service.task;

import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.plugin.PluginOuterClass;
import ru.leti.wise.task.task.TaskGrpc;
import ru.leti.wise.task.task.error.BusinessException;
import ru.leti.wise.task.task.mapper.SolutionMapper;
import ru.leti.wise.task.task.model.solution.SolutionImplementation;
import ru.leti.wise.task.task.model.task.TaskImplementation;
import ru.leti.wise.task.task.repository.SolutionRepository;
import ru.leti.wise.task.task.repository.TaskRepository;
import ru.leti.wise.task.task.service.grpc.plugin.PluginGrpcService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TaskImplementationService {

    private final TaskRepository taskRepository;
    private final PluginGrpcService pluginGrpcService;
    private final SolutionMapper solutionMapper;
    private final SolutionRepository solutionRepository;

    public TaskGrpc.SolveTaskResponse process(TaskGrpc.SolveTaskRequest request) {
        var taskId = UUID.fromString(request.getSolution().getTaskId());
        var task = (TaskImplementation) taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(
                        Status.NOT_FOUND,
                        "Задача с id '%s' не найдена".formatted(taskId)
                ));
        PluginOuterClass.ImplementationResult result = pluginGrpcService.
                checkPluginImplementation(request.getSolution().getSolutionImplementation().getCode(),
                        task.getPluginId().toString());

        SolutionImplementation solutionImplementation = new SolutionImplementation();
        solutionImplementation.setCode(request.getSolution().getSolutionImplementation().getCode());
        solutionImplementation.setTaskId(task.getId());
        solutionImplementation.setAuthorId(task.getAuthorId());
        solutionImplementation.setIsCorrect(result.getResult());
        List<SolutionImplementation.GraphResult> graphResults = new ArrayList<>();
        for (int i = 0; i < result.getGraphTaskResultList().size(); i++) {
            var res = result.getGraphTaskResultList().get(i);
            SolutionImplementation.GraphResult graphResult = new SolutionImplementation.GraphResult();
            graphResult.setId(UUID.fromString(res.getGraphId()));
            graphResult.setOriginalResult(res.getOriginalResult());
            graphResult.setOriginalTimeResult((double) res.getOriginalTimeResult());
            graphResult.setResult(res.getResult());
            graphResult.setTimeResult((double) res.getTimeResult());

            graphResults.add(graphResult);
        }
        solutionImplementation.setId(UUID.randomUUID());
        solutionImplementation.setImplementationResult(graphResults);
        solutionRepository.save(solutionImplementation);
        return TaskGrpc.SolveTaskResponse.newBuilder()
                .setSolution(solutionMapper.toSolutionImplementation(solutionImplementation))
                .build();

    }
}
