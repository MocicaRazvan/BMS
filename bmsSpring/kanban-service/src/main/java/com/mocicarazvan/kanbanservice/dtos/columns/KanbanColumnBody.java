package com.mocicarazvan.kanbanservice.dtos.columns;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class KanbanColumnBody {
    @NotNull(message = "The title should not be null.")
    @NotEmpty(message = "The title should not be empty.")
    private String title;

    @NotNull(message = "The order index should not be null.")
    private int orderIndex;
}
