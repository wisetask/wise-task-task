package ru.leti.wise.task.task.model.solution;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "solution")
@Inheritance(strategy = InheritanceType.JOINED)
public class Solution {
    @Id
    private UUID id;

    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "author_id")
    private UUID authorId;

    @Column(name = "is_correct")
    private Boolean isCorrect;
}
