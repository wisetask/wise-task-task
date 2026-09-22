package ru.leti.wise.task.task.logic;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.pagination.Pagination;
import ru.leti.wise.task.task.TaskGrpc.GetAllTaskSolutionsRequest;
import ru.leti.wise.task.task.TaskGrpc.GetAllTaskSolutionsResponse;
import ru.leti.wise.task.task.mapper.SolutionMapper;
import ru.leti.wise.task.task.model.solution.Solution;
import ru.leti.wise.task.task.repository.SolutionRepository;

import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.fromString;

@Component
@RequiredArgsConstructor
public class GetTaskSolutionsOperation {

    private final SolutionMapper solutionMapper;
    private final SolutionRepository solutionRepository;

    public GetAllTaskSolutionsResponse activate(GetAllTaskSolutionsRequest request) {
        var pagination = request.getPagination();
        var pageable = PageRequest.of(pagination.getPage(), pagination.getPageSize());
        var solutionPage = solutionRepository.findAll(
                byFilter(request),
                pageable
        );
        var solutions = solutionMapper.toSolutions(solutionPage.getContent());
        var paginationResponse = Pagination.PaginationResponse.newBuilder()
                .setPage(solutionPage.getNumber())
                .setPageSize(solutionPage.getSize())
                .setTotalCount(solutionPage.getTotalElements())
                .setTotalPages(solutionPage.getTotalPages())
                .setHasNext(solutionPage.hasNext())
                .setHasPrevious(solutionPage.hasPrevious())
                .build();
        return GetAllTaskSolutionsResponse.newBuilder()
                .addAllItems(solutions)
                .setPagination(paginationResponse)
                .build();
    }

    public Specification<Solution> byFilter(GetAllTaskSolutionsRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!request.getTaskId().isBlank())
                predicates.add(cb.equal(root.get("taskId"), fromString(request.getTaskId())));
            if (!request.getAuthorId().isBlank())
                predicates.add(cb.equal(root.get("authorId"), fromString(request.getAuthorId())));
            // is_correct не помечен в proto как optional, поэтому false трактуется как "фильтр не задан"
            if (request.getIsCorrect())
                predicates.add(cb.equal(root.get("isCorrect"), true));

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
