package ru.leti.wise.task.task.logic;

import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.task.TaskGrpc;
import ru.leti.wise.task.task.error.BusinessException;
import ru.leti.wise.task.task.mapper.CatalogMapper;
import ru.leti.wise.task.task.repository.CatalogRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetCatalogOperation {

    private final CatalogMapper catalogMapper;
    private final CatalogRepository catalogRepository;

    public TaskGrpc.GetCatalogResponse activate(UUID id) {
        var catalog =catalogRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        Status.NOT_FOUND,
                        "Каталог с id '%s' не найден".formatted(id)
                ));

        return TaskGrpc.GetCatalogResponse.newBuilder()
                .setCatalog(catalogMapper.toCatalog(catalog))
                .build();
    }
}
