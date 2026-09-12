package ru.leti.wise.task.task.service.grpc.graph;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.graph.GraphGrpc;
import ru.leti.wise.task.graph.GraphGrpc.GenerateGraphRequest;
import ru.leti.wise.task.graph.GraphOuterClass.Graph;
import ru.leti.wise.task.graph.GraphServiceGrpc;

import java.util.UUID;


@Component
@Observed
@RequiredArgsConstructor
public class GraphGrpcService {

    private final GraphServiceGrpc.GraphServiceBlockingStub graphService;

    public Graph getGraph(int edgeCount, int vertexCount, boolean isDirect) {
        var request = GenerateGraphRequest.newBuilder()
                .setEdgeCount(edgeCount)
                .setVertexCount(vertexCount)
                .setIsDirect(isDirect)
                .build();

        return graphService.generateRandomGraph(request).getGraph();
    }

    public GraphGrpc.CreateGraphResponse createGraph(Graph graph) {
        var request = GraphGrpc.CreateGraphRequest.newBuilder()
                .setGraph(graph)
                .build();
        return graphService.createGraph(request);
    }

    public Graph getGraphById(UUID id) {
        var request = GraphGrpc.GetGraphByIdRequest.newBuilder()
                .setId(id.toString())
                .build();

        return graphService.getGraphById(request).getGraph();
    }
}
