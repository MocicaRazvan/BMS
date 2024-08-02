package com.mocicarazvan.kanbanservice.dtos.tasks;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupedKanbanTask {

    @NotNull(message = "The groupedTasks should not be null.")
    @NotEmpty(message = "The groupedTasks should not be empty.")
    private Map<Long, List<KanbanTaskResponse>> groupedTasks;
}
