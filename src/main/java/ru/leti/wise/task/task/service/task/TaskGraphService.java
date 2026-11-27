package ru.leti.wise.task.task.service.task;

import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.graph.GraphOuterClass;
import ru.leti.wise.task.plugin.PluginOuterClass;
import ru.leti.wise.task.task.TaskGrpc;
import ru.leti.wise.task.task.TaskOuterClass;
import ru.leti.wise.task.task.error.BusinessException;
import ru.leti.wise.task.task.mapper.SolutionMapper;
import ru.leti.wise.task.task.model.solution.SolutionGraph;
import ru.leti.wise.task.task.model.task.PluginType;
import ru.leti.wise.task.task.model.task.TaskGraph;
import ru.leti.wise.task.task.model.task.TaskType;
import ru.leti.wise.task.task.repository.SolutionRepository;
import ru.leti.wise.task.task.repository.TaskRepository;
import ru.leti.wise.task.task.service.grpc.graph.GraphGrpcService;
import ru.leti.wise.task.task.service.grpc.plugin.PluginGrpcService;

import java.util.HashMap;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TaskGraphService {

    private static final String TRUE_PROPERTY = "Выполняется";
    private static final String FALSE_PROPERTY = "Не выполняется";

    private final TaskRepository taskRepository;
    private final SolutionMapper solutionMapper;
    private final GraphGrpcService graphGrpcService;
    private final SolutionRepository solutionRepository;
    private final PluginGrpcService pluginGrpcService;

    public TaskGrpc.SolveTaskResponse process(TaskGrpc.SolveTaskRequest request) {
        if (!request.hasSolution()) {
            throw new BusinessException(Status.INVALID_ARGUMENT, "В запросе отсутствует решение");
        }
        var solution = request.getSolution();
        if (solution.hasSolutionGraph()) {
            return processTaskGraph(solution);
        } else if (solution.hasSolutionImplementation()) {
            processTaskImplementation(solution);
        }
        throw new BusinessException(Status.INVALID_ARGUMENT, "Неизвестный тип задачи. Доступные типы задач: " +
                TaskType.getStringTypes()
        );
        var graph = request.getSolution().getSolutionGraph().getGraph();
        var authorId = request.getSolution().getAuthorId();
        graph.toBuilder().setAuthorId(authorId).build();
        HashMap<UUID, String> handWrittenAnswer = new HashMap<>();
        var solutionGraph = request.getSolution().getSolutionGraph();
        solutionGraph.getPluginStringResultList()
                .stream()
                .map(pluginStringInput -> handWrittenAnswer
                        .put(UUID.fromString(pluginStringInput.getPluginId()), pluginStringInput.getHandWrittenAnswer()));

        graphGrpcService.createGraph(graph);
        var pluginResults = task.getCondition()
                .stream()
                .map(condition -> handWrittenAnswer.containsKey(condition.getPluginId())
                        ? getPluginResult(condition, graph, task, handWrittenAnswer.get(condition.getPluginId()))
                        : getPluginResult(condition, graph, task, ""))
                .toList();

        solution.setResult(pluginResults);
        boolean isCorrect = true;
        for (SolutionGraph.PluginResult pluginResult : solution.getResult()) {
            if (!pluginResult.getIsCorrect()) {
                isCorrect = false;
                break;
            }
        }
        solution.setIsCorrect(isCorrect);
        solutionRepository.save(solution);
        return TaskGrpc.SolveTaskResponse.newBuilder()
                .setSolution(solutionMapper.toSolution(solution))
                .build();
    }

    private TaskGrpc.SolveTaskResponse processTaskGraph(TaskOuterClass.Solution solution) {
        SolutionGraph solutionEntity = solutionMapper.toSolutionGraph(solution);
        TaskGraph task = (TaskGraph) taskRepository.findById(solutionEntity.getTaskId())
                .orElseThrow(() -> new BusinessException(
                        Status.NOT_FOUND,
                        "Задача c id %s не существует".formatted(solution.getTaskId())));
        var graph = solution.getSolutionGraph().getGraph();
        var authorId = solution.getAuthorId();
        graph.toBuilder().setAuthorId(authorId).build();
        var graphResponse = graphGrpcService.createGraph(graph).getGraph();
        var graphId = UUID.fromString(graphResponse.getId());
        var pluginResults = task.getCondition().stream()
                .map(pluginInfo -> getPluginResult(pluginInfo, graphResponse))
                .toList();
        boolean isCorrect = pluginResults.stream()
                .allMatch(SolutionGraph.PluginResult::getIsCorrect);
        solutionEntity.setGraphId(graphId);
        solutionEntity.setResult(pluginResults);
        solutionEntity.setIsCorrect(isCorrect);
        solutionRepository.save(solutionEntity);
        var solutionResponse = solutionMapper.toSolution(solutionEntity);
        return TaskGrpc.SolveTaskResponse.newBuilder()
                .setSolution(solutionResponse).build();
    }

    private void processTaskImplementation(TaskOuterClass.Solution solution) {

    }

    private SolutionGraph.PluginResult getPluginResult(TaskGraph.PluginInfo pluginInfo,
                                                       GraphOuterClass.Graph graph) {
        var response = pluginGrpcService.checkPluginSolution(String.valueOf(pluginInfo.getPluginId()), graph);
        var isCorrect = isCorrect(pluginInfo, response);
        return SolutionGraph.PluginResult.builder()
                .pluginId(pluginInfo.getPluginId())
                .isCorrect(isCorrect)
                .trueValue(pluginInfo.getValue())
                .value(response)
                .pluginMessage(pluginInfo.getMistakeText())
                .build();
    }

    private boolean isCorrect(TaskGraph.PluginInfo pluginInfo, String response) {
        var isCorrect = false;
        if (pluginInfo.getPluginType() == PluginType.GRAPH_CHARACTERISTIC) {
            isCorrect = validateCharacteristic(
                    Integer.parseInt(response),
                    Integer.parseInt(pluginInfo.getValue()),
                    pluginInfo.getSign());
        } else {
            if (response.equals("true") && TRUE_PROPERTY.equals(pluginInfo.getValue()) ||
                    response.equals("false") && FALSE_PROPERTY.equals(pluginInfo.getValue())) {
                isCorrect = true;
            }
        }
        return isCorrect;
    }

    private Boolean validateCharacteristic(Integer response, Integer value, String sign) {
        return switch (sign) {
            case ">" -> response > value;
            case "<" -> response < value;
            case "=" -> response.equals(value);
            case ">=" -> response >= value;
            case "<=" -> response <= value;
            default -> false;
        };
    }
}
