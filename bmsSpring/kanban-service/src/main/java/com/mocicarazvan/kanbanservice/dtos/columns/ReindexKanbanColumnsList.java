package com.mocicarazvan.kanbanservice.dtos.columns;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReindexKanbanColumnsList {

    @NotNull(message = "The columns should not be null.")
    @NotEmpty(message = "The columns should not be empty.")
    private List<KanbanColumnResponse> columns;

}
