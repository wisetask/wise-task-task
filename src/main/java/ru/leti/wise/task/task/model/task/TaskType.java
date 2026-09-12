package ru.leti.wise.task.task.model.task;

import java.util.Arrays;
import java.util.List;

public enum TaskType {
    IMPLEMENTATION,
    GRAPH;

    public static List<String> getStringTypes() {
        return Arrays.stream(TaskType.values()).map(Enum::name).toList();
    }
}
