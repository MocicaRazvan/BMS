package com.mocicarazvan.kanbanservice.services.impl;

import com.mocicarazvan.kanbanservice.dtos.tasks.GroupedKanbanTask;
import com.mocicarazvan.kanbanservice.dtos.tasks.KanbanTaskBody;
import com.mocicarazvan.kanbanservice.dtos.tasks.KanbanTaskResponse;
import com.mocicarazvan.kanbanservice.mappers.KanbanTaskMapper;
import com.mocicarazvan.kanbanservice.models.KanbanTask;
import com.mocicarazvan.kanbanservice.repositories.KanbanColumnRepository;
import com.mocicarazvan.kanbanservice.repositories.KanbanTaskRepository;
import com.mocicarazvan.kanbanservice.services.KanbanColumnService;
import com.mocicarazvan.kanbanservice.services.KanbanTaskService;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KanbanTaskServiceImpl
        extends ManyToOneUserServiceImpl<KanbanTask, KanbanTaskBody, KanbanTaskResponse, KanbanTaskRepository, KanbanTaskMapper>
        implements KanbanTaskService {


    private final EntitiesUtils entitiesUtils;
    private final KanbanColumnService kanbanColumnService;


    public KanbanTaskServiceImpl(KanbanTaskRepository modelRepository, KanbanTaskMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils, KanbanColumnService kanbanColumnService) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "kanbanTask", List.of("id", "userId", "createdAt", "updatedAt", "orderIndex"));
        this.entitiesUtils = entitiesUtils;
        this.kanbanColumnService = kanbanColumnService;
    }


    @Override
    public Flux<KanbanTaskResponse> getByColumnId(Long columnId) {
        Sort sort = Sort.by(Sort.Direction.ASC, "orderIndex");

        return modelRepository.findAllByColumnId(columnId, sort)
                .map(modelMapper::fromModelToResponse);
    }

//    @Override
//    public Mono<Void> reindex(GroupedKanbanTask groupedKanbanTask, String userId) {
//        return Flux.fromIterable(groupedKanbanTask.getGroupedTasks()
//                .entrySet().stream().map(
//                        (entry) ->
//                                kanbanColumnService.getModel(entry.getKey())
//                                        .map(c -> entitiesUtils.checkOwner(c, userId))
//                                        .thenReturn(
//                                                Flux.fromIterable(entry.getValue().stream().map(tr ->
//                                                        getModel(tr.getId())
//                                                                .flatMap(t -> entitiesUtils.checkOwner(t, userId)
//                                                                        .thenReturn(t))
//                                                                .map(t -> {
//                                                                    t.setOrderIndex(tr.getOrderIndex());
//                                                                    t.setUpdatedAt(LocalDateTime.now());
//                                                                    t.setColumnId(entry.getKey());
//                                                                    return modelRepository.save(t);
//                                                                })
//                                                ).toList())
//                                        )).toList()
//        ).then();
//    }

    @Override
    public Mono<Void> reindex(GroupedKanbanTask groupedKanbanTask, String userId) {
        return Flux.fromIterable(groupedKanbanTask.getGroupedTasks().entrySet())
                .flatMap(entry ->
                        kanbanColumnService.getModel(entry.getKey())
                                .map(c -> entitiesUtils.checkOwner(c, userId)
                                )
                                .flatMapMany(c -> Flux.fromIterable(entry.getValue()))
                                .flatMap(tr ->
                                        getModel(tr.getId())
                                                .flatMap(t ->
                                                        entitiesUtils.checkOwner(t, userId)
                                                                .then(Mono.fromCallable(() -> {
                                                                    t.setOrderIndex(tr.getOrderIndex());
                                                                    t.setUpdatedAt(LocalDateTime.now());
                                                                    t.setColumnId(entry.getKey());
                                                                    return t;
                                                                }))
                                                )
                                                .flatMap(modelRepository::save)
                                )
                )
                .then();
    }

}
