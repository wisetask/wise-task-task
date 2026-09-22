package ru.leti.wise.task.task.logic;

import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.leti.wise.task.task.error.BusinessException;
import ru.leti.wise.task.task.repository.SolutionRepository;
import ru.leti.wise.task.task.repository.TaskRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeleteTaskOperation {

    private final TaskRepository taskRepository;
    private final SolutionRepository solutionRepository;

    @Transactional
    public void activate(UUID id) {
        if(taskRepository.findById(id).isEmpty()){
            throw new BusinessException(
                    Status.NOT_FOUND,
                    "Задача с id '%s' не найдена".formatted(id)
            );
        }
        solutionRepository.deleteByTaskId(id);
        // todo удаление задач из каталогов
        taskRepository.deleteById(id);
    }
}
