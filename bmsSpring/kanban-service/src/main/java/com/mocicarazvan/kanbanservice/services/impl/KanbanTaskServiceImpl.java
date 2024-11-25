package com.mocicarazvan.kanbanservice.services.impl;

import com.mocicarazvan.kanbanservice.dtos.tasks.GroupedKanbanTask;
import com.mocicarazvan.kanbanservice.dtos.tasks.KanbanTaskBody;
import com.mocicarazvan.kanbanservice.dtos.tasks.KanbanTaskResponse;
import com.mocicarazvan.kanbanservice.mappers.KanbanTaskMapper;
import com.mocicarazvan.kanbanservice.models.KanbanTask;
import com.mocicarazvan.kanbanservice.repositories.KanbanTaskRepository;
import com.mocicarazvan.kanbanservice.services.KanbanColumnService;
import com.mocicarazvan.kanbanservice.services.KanbanTaskService;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCacheEvict;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Getter;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Getter
public class KanbanTaskServiceImpl
        extends ManyToOneUserServiceImpl<KanbanTask, KanbanTaskBody, KanbanTaskResponse, KanbanTaskRepository, KanbanTaskMapper, KanbanTaskServiceImpl.KanbanTaskServiceRedisCacheWrapper>
        implements KanbanTaskService {


    private final EntitiesUtils entitiesUtils;
    private final KanbanColumnService kanbanColumnService;


    public KanbanTaskServiceImpl(KanbanTaskRepository modelRepository, KanbanTaskMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils, KanbanColumnService kanbanColumnService, KanbanTaskServiceRedisCacheWrapper self) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "kanbanTask", List.of("id", "userId", "createdAt", "updatedAt", "orderIndex"), self);
        this.entitiesUtils = entitiesUtils;
        this.kanbanColumnService = kanbanColumnService;
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, masterId = "#kanbanTaskBody.columnId")
    public Mono<KanbanTaskResponse> createModel(KanbanTaskBody kanbanTaskBody, String userId) {
        return super.createModel(kanbanTaskBody, userId);
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, id = "#id", masterPath = "columnId")
    public Mono<KanbanTaskResponse> deleteModel(Long id, String userId) {
        return super.deleteModel(id, userId);
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, id = "#id", masterPath = "columnId")
    public Mono<KanbanTaskResponse> updateModel(Long id, KanbanTaskBody kanbanTaskBody, String userId) {
        return super.updateModel(id, kanbanTaskBody, userId);
    }

    @Override
    @RedisReactiveChildCache(key = CACHE_KEY_PATH, masterId = "#columnId", idPath = "id")
    public Flux<KanbanTaskResponse> getByColumnId(Long columnId) {
        Sort sort = Sort.by(Sort.Direction.ASC, "orderIndex");

        return modelRepository.findAllByColumnId(columnId, sort)
                .map(modelMapper::fromModelToResponse);
    }

    @Override
    public Mono<Void> reindex(GroupedKanbanTask groupedKanbanTask, String userId) {

        return
                Flux.fromIterable(groupedKanbanTask.getGroupedTasks().entrySet())
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
                                                                        .flatMap(self::reindexInvalidate)
                                                        )
                                                        .flatMap(modelRepository::save)
                                        )
                        )
                        .then();
    }


    @Component
    @Getter
    public static class KanbanTaskServiceRedisCacheWrapper extends ManyToOneUserServiceRedisCacheWrapper<KanbanTask, KanbanTaskBody, KanbanTaskResponse, KanbanTaskRepository, KanbanTaskMapper> {

        public KanbanTaskServiceRedisCacheWrapper(KanbanTaskRepository modelRepository, KanbanTaskMapper modelMapper, UserClient userClient) {
            super(modelRepository, modelMapper, "kanbanTask", userClient);
        }

        @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, masterId = "#kanbanTaskResponse.columnId", id = "#kanbanTaskResponse.id")
        public Mono<KanbanTask> reindexInvalidate(KanbanTask kanbanTaskResponse) {
            return Mono.just(kanbanTaskResponse);
        }
    }


}
