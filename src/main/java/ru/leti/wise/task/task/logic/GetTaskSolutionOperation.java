package ru.leti.wise.task.task.logic;

import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.task.TaskGrpc;
import ru.leti.wise.task.task.error.BusinessException;
import ru.leti.wise.task.task.mapper.SolutionMapper;
import ru.leti.wise.task.task.repository.SolutionRepository;

import static java.util.UUID.fromString;

@Component
@RequiredArgsConstructor
public class GetTaskSolutionOperation {

    private final SolutionMapper solutionMapper;
    private final SolutionRepository solutionRepository;

    public TaskGrpc.GetTaskSolutionResponse activate(TaskGrpc.GetTaskSolutionRequest request) {
        var solution = solutionRepository.findById(fromString(request.getId()))
                .orElseThrow(() -> new BusinessException(Status.NOT_FOUND,
                        "Решение с id '%s' не найдено".formatted(request.getId())));
        return TaskGrpc.GetTaskSolutionResponse.newBuilder()
                .setSolution(solutionMapper.toSolution(solution))
                .build();
    }
}
