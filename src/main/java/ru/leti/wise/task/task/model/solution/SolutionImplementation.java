package ru.leti.wise.task.task.model.solution;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "solution_implementation")
@EqualsAndHashCode(callSuper = true)
public class SolutionImplementation extends Solution {
    private String code;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<GraphResult> implementationResult;

    @Data
    public static class GraphResult {
        private UUID id;
        private Double originalTimeResult;
        private Double timeResult;
        private String originalResult;
        private String result;
    }
}

