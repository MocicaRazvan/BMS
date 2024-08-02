package com.mocicarazvan.kanbanservice.dtos.tasks;

import com.mocicarazvan.kanbanservice.enums.KanbanTaskType;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class KanbanTaskResponse extends WithUserDto {
    private String content;
    private Long columnId;
    private KanbanTaskType type;
    private int orderIndex;
}
