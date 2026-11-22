package ru.leti.wise.task.task.service.grpc.plugin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.plugin.PluginGrpc;
import ru.leti.wise.task.plugin.PluginOuterClass;
import ru.leti.wise.task.plugin.PluginServiceGrpc;

import java.util.List;


@Component
@RequiredArgsConstructor
public class PluginGrpcService {

    private final PluginServiceGrpc.PluginServiceBlockingStub pluginService;

    public String checkPluginSolution(PluginOuterClass.Solution solution) {
        var request = PluginGrpc.CheckPluginSolutionRequest.newBuilder()
                .setSolution(solution)
                .build();
       return pluginService.checkPluginSolution(request).getResult();
    }

    public PluginOuterClass.ImplementationResult checkPluginImplementation(String implementationFile, String pluginId) {
        var request = PluginGrpc.CheckPluginImplementationRequest.newBuilder()
                .setId(pluginId)
                .setFile(implementationFile)
                .build();
        return pluginService.checkPluginImplementation(request).getImplementationResult();
    }

    public List<PluginOuterClass.Plugin> getPluginsByIds(List<String> pluginsIds) {
        var request = PluginGrpc.PluginIds.newBuilder().addAllPluginIds(pluginsIds).build();
        return pluginService.getPluginsByIds(request).getPluginsList();
    }
}
