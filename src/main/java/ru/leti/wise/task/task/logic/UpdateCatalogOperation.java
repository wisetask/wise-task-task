package ru.leti.wise.task.task.logic;

import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.task.TaskGrpc;
import ru.leti.wise.task.task.error.BusinessException;
import ru.leti.wise.task.task.mapper.CatalogMapper;
import ru.leti.wise.task.task.repository.CatalogRepository;

@Component
@RequiredArgsConstructor
public class UpdateCatalogOperation {

    private final CatalogMapper catalogMapper;
    private final CatalogRepository catalogRepository;

    public TaskGrpc.UpdateCatalogResponse activate(TaskGrpc.UpdateCatalogRequest request) {
        var catalog = catalogMapper.toCatalog(request.getCatalog());
        catalogRepository.findById(catalog.getId())
                .orElseThrow(() -> new BusinessException(
                        Status.NOT_FOUND,
                        "Каталог с id '%s' не найден".formatted(catalog.getId())
                ));
        catalogRepository.save(catalog);
        return TaskGrpc.UpdateCatalogResponse.newBuilder()
                .setCatalog(request.getCatalog())
                .build();
    }
}
