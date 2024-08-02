package com.mocicarazvan.kanbanservice.services;

import com.mocicarazvan.kanbanservice.dtos.columns.KanbanColumnBody;
import com.mocicarazvan.kanbanservice.dtos.columns.KanbanColumnResponse;
import com.mocicarazvan.kanbanservice.dtos.columns.ReindexKanbanColumnsList;
import com.mocicarazvan.kanbanservice.mappers.KanbanColumnMapper;
import com.mocicarazvan.kanbanservice.models.KanbanColumn;
import com.mocicarazvan.kanbanservice.repositories.KanbanColumnRepository;
import com.mocicarazvan.templatemodule.services.ManyToOneUserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface KanbanColumnService extends ManyToOneUserService
        <KanbanColumn, KanbanColumnBody, KanbanColumnResponse, KanbanColumnRepository, KanbanColumnMapper> {

    Flux<KanbanColumnResponse> getAllByUserId(String userId);

    Mono<Void> reindex(ReindexKanbanColumnsList reindexKanbanColumnsList, String userId);


}
