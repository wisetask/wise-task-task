package ru.leti.wise.task.task.logic;

import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.task.TaskGrpc.SolveTaskRequest;
import ru.leti.wise.task.task.TaskGrpc.SolveTaskResponse;
import ru.leti.wise.task.task.error.BusinessException;
import ru.leti.wise.task.task.model.task.TaskType;
import ru.leti.wise.task.task.service.task.TaskGraphService;
import ru.leti.wise.task.task.service.task.TaskImplementationService;


@Component
@RequiredArgsConstructor
public class SolveTaskOperation {

    private final TaskGraphService taskGraphService;
    private final TaskImplementationService taskImplementationService;

    public SolveTaskResponse activate(SolveTaskRequest request) {
        if (request.getSolution().hasSolutionImplementation()) {
            return taskImplementationService.process(request);
        } else if (request.getSolution().hasSolutionGraph()) {
            return taskGraphService.process(request);
        }
        throw new BusinessException(Status.INVALID_ARGUMENT, "Неизвестный тип задачи. Допступные типы задач: " +
                TaskType.getStringTypes()
        );
    }
}
