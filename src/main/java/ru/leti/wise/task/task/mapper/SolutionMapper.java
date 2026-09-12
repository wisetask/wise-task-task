package ru.leti.wise.task.task.mapper;

import io.grpc.Status;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.leti.wise.task.task.TaskOuterClass;
import ru.leti.wise.task.task.error.BusinessException;
import ru.leti.wise.task.task.model.solution.Solution;
import ru.leti.wise.task.task.model.solution.SolutionGraph;
import ru.leti.wise.task.task.model.solution.SolutionImplementation;
import ru.leti.wise.task.task.model.task.TaskType;

import java.util.List;

@Mapper(componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface SolutionMapper {

    default Solution toSolution(TaskOuterClass.Solution solution) {
        if (solution.hasSolutionGraph()) {
            return toSolutionGraph(solution);
        } else if (solution.hasSolutionImplementation()) {
            return toSolutionImplementation(solution);
        }
        throw new BusinessException(Status.INVALID_ARGUMENT, "Неизвестный тип решения. Доступные типы решения: " +
                TaskType.getStringTypes());
    }

    @Mapping(target = "graphId", source = "solution.solutionGraph.graph.id")
    @Mapping(target = ".", source = "solution.solutionGraph")
    @Mapping(target = "result", ignore = true)
    SolutionGraph toSolutionGraph(TaskOuterClass.Solution solution);

    @Mapping(target = ".", source = "solution.solutionImplementation")
    @Mapping(target = "implementationResult", ignore = true)
    SolutionImplementation toSolutionImplementation(TaskOuterClass.Solution solution);

    default TaskOuterClass.Solution toSolution(Solution solution) {
        if (solution instanceof SolutionGraph) {
            return toSolutionGraph((SolutionGraph) solution);
        } else if (solution instanceof SolutionImplementation) {
            return toSolutionImplementation((SolutionImplementation) solution);
        }
        throw new BusinessException(Status.INVALID_ARGUMENT, "Неизвестный тип решения. Доступные типы решения: " +
                TaskType.getStringTypes());
    }

    @Mapping(target = "solutionGraph.graph.id", source = "graphId")
    @Mapping(target = "solutionGraph.pluginResultsList", source = "result")
    @Mapping(target = "solutionImplementation", ignore = true)
    TaskOuterClass.Solution toSolutionGraph(SolutionGraph solution);

    @Mapping(target = "solutionImplementation", source = ".")
    @Mapping(target = "solutionGraph", ignore = true)
    TaskOuterClass.Solution toSolutionImplementation(SolutionImplementation solution);

    List<TaskOuterClass.Solution> toSolutions(List<Solution> solution);
}
