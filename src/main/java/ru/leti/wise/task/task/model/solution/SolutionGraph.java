package ru.leti.wise.task.task.model.solution;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "solution_graph")
@EqualsAndHashCode(callSuper = true)
public class SolutionGraph extends Solution {
    @Column(name = "graph_id")
    private UUID graphId;

    @JdbcTypeCode(SqlTypes.JSON)
    List<PluginResult> result;

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    @Builder
    public static class PluginResult {
        private UUID pluginId;
        private Boolean isCorrect;
        private String value;
        private String trueValue;
        private String pluginMessage;
    }
}
