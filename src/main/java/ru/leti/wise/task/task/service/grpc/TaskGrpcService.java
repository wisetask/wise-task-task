package ru.leti.wise.task.task.service.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.advice.GrpcAdvice;
import org.springframework.grpc.server.advice.GrpcExceptionHandler;
import org.springframework.grpc.server.service.GrpcService;
import ru.leti.wise.task.task.TaskGrpc;
import ru.leti.wise.task.task.TaskServiceGrpc.TaskServiceImplBase;
import ru.leti.wise.task.task.error.BusinessException;
import ru.leti.wise.task.task.helper.LogInterceptor;
import ru.leti.wise.task.task.logic.*;

import java.util.UUID;

@Slf4j
@Observed
@GrpcService(interceptors = {LogInterceptor.class})
@RequiredArgsConstructor
public class TaskGrpcService extends TaskServiceImplBase {

    private final GetTaskOperation getTaskOperation;
    private final GetTasksOperation getTasksOperation;
    private final SolveTaskOperation solveTaskOperation;
    private final UpdateTaskOperation updateTaskOperation;
    private final DeleteTaskOperation deleteTaskOperation;
    private final CreateTaskOperation createTaskOperation;
    private final GetTaskSolutionOperation getTaskSolutionOperation;
    private final GetTaskSolutionsOperation getTaskSolutionsOperation;
    private final GetUserSolutionStatisticOperation getUserSolutionStatisticOperation;
    private final CreateCatalogOperation createCatalogOperation;
    private final DeleteCatalogOperation deleteCatalogOperation;
    private final GetCatalogsOperation getCatalogsOperation;
    private final GetCatalogOperation getCatalogOperation;
    private final UpdateCatalogOperation updateCatalogOperation;

    @Override
    public void getTask(TaskGrpc.GetTaskRequest request, StreamObserver<TaskGrpc.GetTaskResponse> responseObserver) {
        responseObserver.onNext(getTaskOperation.activate(UUID.fromString(request.getId())));
        responseObserver.onCompleted();
    }

    @Override
    public void getAllTask(TaskGrpc.GetAllTaskRequest request, StreamObserver<TaskGrpc.GetAllTaskResponse> responseObserver) {
        responseObserver.onNext(getTasksOperation.activate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteTask(TaskGrpc.DeleteTaskRequest request, StreamObserver<Empty> responseObserver) {
        deleteTaskOperation.activate(UUID.fromString(request.getId()));
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void createTask(TaskGrpc.CreateTaskRequest request, StreamObserver<TaskGrpc.CreateTaskResponse> responseObserver) {
        responseObserver.onNext(createTaskOperation.activate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void updateTask(TaskGrpc.UpdateTaskRequest request, StreamObserver<TaskGrpc.UpdateTaskResponse> responseObserver) {
        responseObserver.onNext(updateTaskOperation.activate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void solveTask(TaskGrpc.SolveTaskRequest request, StreamObserver<TaskGrpc.SolveTaskResponse> responseObserver) {
        responseObserver.onNext(solveTaskOperation.activate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getTaskSolution(TaskGrpc.GetTaskSolutionRequest request, StreamObserver<TaskGrpc.GetTaskSolutionResponse> responseObserver) {
        responseObserver.onNext(getTaskSolutionOperation.activate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getAllTaskSolutions(TaskGrpc.GetAllTaskSolutionsRequest request, StreamObserver<TaskGrpc.GetAllTaskSolutionsResponse> responseObserver) {
        responseObserver.onNext(getTaskSolutionsOperation.activate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getUserSolutionStatistic(TaskGrpc.GetUserSolutionStatisticRequest request, StreamObserver<TaskGrpc.GetUserSolutionStatisticResponse> responseObserver) {
        responseObserver.onNext(getUserSolutionStatisticOperation.activate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void createCatalog(TaskGrpc.CreateCatalogRequest request, StreamObserver<TaskGrpc.CreateCatalogResponse> responseObserver) {
        responseObserver.onNext(createCatalogOperation.activate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteCatalog(TaskGrpc.DeleteCatalogRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onNext(deleteCatalogOperation.activate(UUID.fromString(request.getId())));
        responseObserver.onCompleted();
    }

    @Override
    public void updateCatalog(TaskGrpc.UpdateCatalogRequest request, StreamObserver<TaskGrpc.UpdateCatalogResponse> responseObserver) {
        responseObserver.onNext(updateCatalogOperation.activate(request));
        responseObserver.onCompleted();
    }

    @Override
    public void getCatalog(TaskGrpc.GetCatalogRequest request, StreamObserver<TaskGrpc.GetCatalogResponse> responseObserver) {
        responseObserver.onNext(getCatalogOperation.activate(UUID.fromString(request.getId())));
        responseObserver.onCompleted();
    }

    @Override
    public void getAllCatalogs(Empty request, StreamObserver<TaskGrpc.GetAllCatalogsResponse> responseObserver) {
        responseObserver.onNext(getCatalogsOperation.activate(request));
        responseObserver.onCompleted();
    }

    @GrpcAdvice
    @RequiredArgsConstructor
    public static class ErrorHandler {

        @GrpcExceptionHandler
        public StatusRuntimeException handleBusinessException(BusinessException e) {
            return e.getStatus().withDescription(e.getMessage()).asRuntimeException();
        }
    }
}
