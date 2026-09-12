package ru.leti.wise.task.task.configuration;


import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ImportGrpcClients;
import ru.leti.wise.task.graph.GraphServiceGrpc;
import ru.leti.wise.task.plugin.PluginServiceGrpc;

@Configuration
@ImportGrpcClients(
        target = "${grpc.service.graph.host}:${grpc.service.graph.port}",
        types = GraphServiceGrpc.GraphServiceBlockingStub.class
)
@ImportGrpcClients(
        target = "${grpc.service.plugin.host}:${grpc.service.plugin.port}",
        types = PluginServiceGrpc.PluginServiceBlockingStub.class
)
public class GrpcConfiguration {
}
