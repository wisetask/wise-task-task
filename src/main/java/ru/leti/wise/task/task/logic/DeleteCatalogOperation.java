package ru.leti.wise.task.task.logic;

import com.google.protobuf.Empty;
import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.leti.wise.task.task.error.BusinessException;
import ru.leti.wise.task.task.repository.CatalogRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeleteCatalogOperation {

    private final CatalogRepository catalogRepository;

    @Transactional
    public Empty activate(UUID id) {
        if(catalogRepository.findById(id).isEmpty()){
            throw new BusinessException(
                    Status.NOT_FOUND,
                    "Каталог с id '%s' не найден".formatted(id));
        }
        catalogRepository.deleteById(id);
        //todo возможно нужно удаление task_catalog, также проверка
        return Empty.newBuilder().build();
    }
}
