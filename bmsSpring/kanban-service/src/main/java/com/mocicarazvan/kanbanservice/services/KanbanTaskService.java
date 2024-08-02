package com.mocicarazvan.kanbanservice.services;

import com.mocicarazvan.kanbanservice.dtos.tasks.GroupedKanbanTask;
import com.mocicarazvan.kanbanservice.dtos.tasks.KanbanTaskBody;
import com.mocicarazvan.kanbanservice.dtos.tasks.KanbanTaskResponse;
import com.mocicarazvan.kanbanservice.mappers.KanbanTaskMapper;
import com.mocicarazvan.kanbanservice.models.KanbanTask;
import com.mocicarazvan.kanbanservice.repositories.KanbanTaskRepository;
import com.mocicarazvan.templatemodule.services.ManyToOneUserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface KanbanTaskService extends ManyToOneUserService
        <KanbanTask, KanbanTaskBody, KanbanTaskResponse, KanbanTaskRepository, KanbanTaskMapper> {

    Flux<KanbanTaskResponse> getByColumnId(Long columnId);


    Mono<Void> reindex(GroupedKanbanTask groupedKanbanTask, String userId);
}
