package com.mocicarazvan.kanbanservice.dtos.tasks;

import com.mocicarazvan.kanbanservice.enums.KanbanTaskType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class KanbanTaskBody {
    @NotNull(message = "The title should not be null.")
    @NotEmpty(message = "The title should not be empty.")
    private String content;

    @NotNull(message = "The columnId should not be null.")
    @Positive(message = "The columnId should be positive.")
    private Long columnId;

    @NotNull(message = "The type should not be null.")
    private KanbanTaskType type;

    @NotNull(message = "The order index should not be null.")
    private int orderIndex;
}
