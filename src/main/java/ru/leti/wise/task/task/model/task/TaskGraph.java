package ru.leti.wise.task.task.model.task;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "task_graph")
@EqualsAndHashCode(callSuper = true)
public class TaskGraph extends Task {

    @Column(name = "is_hidden_mistake")
    private Boolean isHiddenMistake;

    @Column(name = "graph_id")
    private UUID graphId;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<PluginInfo> condition;

    @JdbcTypeCode(SqlTypes.JSON)
    private Rule rule;

    @Column(name = "graph_type")
    @Enumerated(value = EnumType.STRING)
    private GraphType graphType;

    @Data
    public static class PluginInfo {
        private UUID pluginId;
        private String value;
        private String mistakeText;
        private String sign;
        private PluginType pluginType;
        private Integer order;
    }

    @Data
    public static class Rule {
        private Boolean isColor;
        private Boolean isEdit;
        private Boolean isMove;
        private Boolean isDelete;
    }
}
