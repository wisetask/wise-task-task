package ru.leti.wise.task.task.logic;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.plugin.PluginGrpc;
import ru.leti.wise.task.task.TaskGrpc;
import ru.leti.wise.task.task.TaskGrpc.GetAllTaskResponse;
import ru.leti.wise.task.task.mapper.TaskMapper;
import ru.leti.wise.task.task.model.task.Task;
import ru.leti.wise.task.task.model.task.TaskType;
import ru.leti.wise.task.task.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetTasksOperation {

    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;

    public GetAllTaskResponse activate(TaskGrpc.GetAllTaskRequest request) {
        var pagination = request.getPagination();
        var filter = request.getFilter();
        var pageable = PageRequest.of(pagination.getPage(), pagination.getPageSize());
        var specification = byFilter(
                filter
        );
        var taskPage = taskRepository.findAll(
                specification,
                pageable
        );
        var tasks = taskMapper.toTasks(taskPage.getContent());
        var paginationResponse = TaskGrpc.PaginationResponse.newBuilder()
                .setPage(taskPage.getNumber())
                .setPageSize(taskPage.getSize())
                .setTotalCount(taskPage.getTotalElements())
                .setTotalPages(taskPage.getTotalPages())
                .setHasNext(taskPage.hasNext())
                .setHasPrevious(taskPage.hasPrevious())
                .build();
        return GetAllTaskResponse.newBuilder()
                .addAllItems(tasks)
                .setPagination(paginationResponse)
                .build();
    }


    public Specification<Task> byFilter(TaskGrpc.TaskFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.hasName())
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
            if (filter.hasDescription())
                predicates.add(cb.like(root.get("description"), "%" + filter.getDescription().toLowerCase() + "%"));
            if (filter.hasCategory())
                predicates.add(cb.like(root.get("category"), "%s" + filter.getCategory().toLowerCase() + "%s"));

            if (filter.hasTaskType())
                predicates.add(cb.equal(
                        root.get("taskType"),
                        TaskType.valueOf(filter.getTaskType().name())
                ));

            if (filter.hasAuthorId())
                predicates.add(cb.equal(root.get("authorId"), filter.getAuthorId()));

            if (filter.hasIsPublic())
                predicates.add(cb.equal(
                        root.get("isPublic"),
                        filter.getIsPublic()
                ));

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
