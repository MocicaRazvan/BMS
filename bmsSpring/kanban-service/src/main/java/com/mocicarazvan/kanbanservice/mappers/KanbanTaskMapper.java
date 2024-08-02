package com.mocicarazvan.kanbanservice.mappers;

import com.mocicarazvan.kanbanservice.dtos.tasks.KanbanTaskBody;
import com.mocicarazvan.kanbanservice.dtos.tasks.KanbanTaskResponse;
import com.mocicarazvan.kanbanservice.models.KanbanTask;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class KanbanTaskMapper extends DtoMapper<KanbanTask, KanbanTaskBody, KanbanTaskResponse> {
    @Override
    public KanbanTaskResponse fromModelToResponse(KanbanTask kanbanTask) {
        return KanbanTaskResponse.builder()
                .content(kanbanTask.getContent())
                .columnId(kanbanTask.getColumnId())
                .type(kanbanTask.getType())
                .userId(kanbanTask.getUserId())
                .id(kanbanTask.getId())
                .createdAt(kanbanTask.getCreatedAt())
                .updatedAt(kanbanTask.getUpdatedAt())
                .orderIndex(kanbanTask.getOrderIndex())
                .build();
    }

    @Override
    public KanbanTask fromBodyToModel(KanbanTaskBody kanbanTaskBody) {
        return KanbanTask.builder()
                .content(kanbanTaskBody.getContent())
                .columnId(kanbanTaskBody.getColumnId())
                .type(kanbanTaskBody.getType())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .orderIndex(kanbanTaskBody.getOrderIndex())
                .build();
    }

    @Override
    public Mono<KanbanTask> updateModelFromBody(KanbanTaskBody kanbanTaskBody, KanbanTask kanbanTask) {
        kanbanTask.setContent(kanbanTaskBody.getContent());
        kanbanTask.setColumnId(kanbanTaskBody.getColumnId());
        kanbanTask.setType(kanbanTaskBody.getType());
        kanbanTask.setUpdatedAt(LocalDateTime.now());
        kanbanTask.setOrderIndex(kanbanTaskBody.getOrderIndex());
        return Mono.just(kanbanTask);
    }
}
