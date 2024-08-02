package com.mocicarazvan.kanbanservice.mappers;


import com.mocicarazvan.kanbanservice.dtos.columns.KanbanColumnBody;
import com.mocicarazvan.kanbanservice.dtos.columns.KanbanColumnResponse;
import com.mocicarazvan.kanbanservice.models.KanbanColumn;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class KanbanColumnMapper extends DtoMapper<KanbanColumn, KanbanColumnBody, KanbanColumnResponse> {
    @Override
    public KanbanColumnResponse fromModelToResponse(KanbanColumn kanbanColumn) {
        return KanbanColumnResponse.builder()
                .title(kanbanColumn.getTitle())
                .userId(kanbanColumn.getUserId())
                .id(kanbanColumn.getId())
                .createdAt(kanbanColumn.getCreatedAt())
                .updatedAt(kanbanColumn.getUpdatedAt())
                .orderIndex(kanbanColumn.getOrderIndex())
                .build();
    }

    @Override
    public KanbanColumn fromBodyToModel(KanbanColumnBody kanbanColumnBody) {
        return KanbanColumn.builder()
                .title(kanbanColumnBody.getTitle())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .orderIndex(kanbanColumnBody.getOrderIndex())
                .build();
    }

    @Override
    public Mono<KanbanColumn> updateModelFromBody(KanbanColumnBody kanbanColumnBody, KanbanColumn kanbanColumn) {

        kanbanColumn.setTitle(kanbanColumnBody.getTitle());
        kanbanColumn.setUpdatedAt(LocalDateTime.now());
        kanbanColumn.setOrderIndex(kanbanColumnBody.getOrderIndex());
        return Mono.just(kanbanColumn);
    }
}
