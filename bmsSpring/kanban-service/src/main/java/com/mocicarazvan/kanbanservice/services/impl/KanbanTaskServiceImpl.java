package com.mocicarazvan.kanbanservice.services.impl;

import com.mocicarazvan.kanbanservice.dtos.columns.KanbanColumnResponse;
import com.mocicarazvan.kanbanservice.dtos.tasks.GroupedKanbanTask;
import com.mocicarazvan.kanbanservice.dtos.tasks.KanbanTaskBody;
import com.mocicarazvan.kanbanservice.dtos.tasks.KanbanTaskResponse;
import com.mocicarazvan.kanbanservice.mappers.KanbanColumnMapper;
import com.mocicarazvan.kanbanservice.mappers.KanbanTaskMapper;
import com.mocicarazvan.kanbanservice.models.KanbanTask;
import com.mocicarazvan.kanbanservice.repositories.KanbanColumnRepository;
import com.mocicarazvan.kanbanservice.repositories.KanbanTaskRepository;
import com.mocicarazvan.kanbanservice.services.KanbanColumnService;
import com.mocicarazvan.kanbanservice.services.KanbanTaskService;
import com.mocicarazvan.templatemodule.adapters.CacheChildFilteredToHandlerAdapter;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheChildFilterKey;
import com.mocicarazvan.templatemodule.cache.keys.ChildFilterKey;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.generic.IdGenerateDto;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jooq.lambda.function.Function2;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Service
@Getter
public class KanbanTaskServiceImpl
        extends ManyToOneUserServiceImpl<KanbanTask, KanbanTaskBody, KanbanTaskResponse, KanbanTaskRepository, KanbanTaskMapper>
        implements KanbanTaskService {


    private final EntitiesUtils entitiesUtils;
    private final KanbanColumnService kanbanColumnService;
    private final KanbanTaskServiceCacheHandler kanbanTaskServiceCacheHandler;


    public KanbanTaskServiceImpl(KanbanTaskRepository modelRepository, KanbanTaskMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils, KanbanColumnService kanbanColumnService, KanbanTaskServiceCacheHandler kanbanTaskServiceCacheHandler) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "kanbanTask", List.of("id", "userId", "createdAt", "updatedAt", "orderIndex"), kanbanTaskServiceCacheHandler);
        this.entitiesUtils = entitiesUtils;
        this.kanbanColumnService = kanbanColumnService;
        this.kanbanTaskServiceCacheHandler = kanbanTaskServiceCacheHandler;
    }


    @Override
    public Flux<KanbanTaskResponse> getByColumnId(Long columnId) {
        Sort sort = Sort.by(Sort.Direction.ASC, "orderIndex");

        return
                kanbanTaskServiceCacheHandler.getByColumnIdPersist.apply(
                        modelRepository.findAllByColumnId(columnId, sort)
                                .map(modelMapper::fromModelToResponse), columnId);
    }

    @Override
    public Mono<Void> reindex(GroupedKanbanTask groupedKanbanTask, String userId) {
        return
                kanbanTaskServiceCacheHandler.reindexInvalidate.apply(
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
                                                                        )
                                                                        .flatMap(modelRepository::save)
                                                        )
                                        ), groupedKanbanTask)
                        .then();
    }

    ;


    @EqualsAndHashCode(callSuper = true)
    @Data
    @Component
    public static class KanbanTaskServiceCacheHandler extends
            ManyToOneUserServiceImpl.ManyToOneUserServiceCacheHandler<KanbanTask, KanbanTaskBody, KanbanTaskResponse> {
        private final FilteredListCaffeineCacheChildFilterKey<KanbanTaskResponse> cacheFilter;
        private final KanbanTaskMapper kanbanTaskMapper;

        Function2<Flux<KanbanTaskResponse>, Long, Flux<KanbanTaskResponse>> getByColumnIdPersist;
        Function2<Flux<KanbanTask>, GroupedKanbanTask, Flux<KanbanTask>> reindexInvalidate;
        Function2<Mono<KanbanColumnResponse>, Long, Mono<KanbanColumnResponse>> updateByColumnInvalidate;
        Function2<Mono<KanbanColumnResponse>, Long, Mono<KanbanColumnResponse>> deleteByColumnInvalidate;

        public KanbanTaskServiceCacheHandler(FilteredListCaffeineCacheChildFilterKey<KanbanTaskResponse> cacheFilter, KanbanTaskMapper kanbanTaskMapper) {
            super();
            this.cacheFilter = cacheFilter;
            this.kanbanTaskMapper = kanbanTaskMapper;

            CacheChildFilteredToHandlerAdapter.convertToManyUserHandler(cacheFilter, this,
                    KanbanTaskResponse::getColumnId, kanbanTaskMapper::fromModelToResponse
            );

            this.getByColumnIdPersist = (flux, columnId) -> cacheFilter.getExtraUniqueFluxCacheForMasterIndependentOfRouteType(
                    EntitiesUtils.getListOfNotNullObjects(columnId),
                    "getByColumnIdPersist" + columnId,
                    IdGenerateDto::getId,
                    columnId,
                    flux
            );
            this.reindexInvalidate = (flux, groupedKanbanTask) -> {
//                Set<Long> colId=groupedKanbanTask.getGroupedTasks().keySet();
//                Predicate<ChildFilterKey> predicate= key->false;
//                for (Long id:colId){
//                    predicate=predicate.or(cacheFilter.byMasterAndIds(id));
//                }
                Predicate<ChildFilterKey> predicate = groupedKanbanTask.getGroupedTasks().keySet()
                        .parallelStream()
                        .reduce(k -> false,
                                (p, c) -> p.or(cacheFilter.byMasterAndIds(c)),
                                Predicate::or
                        );
                return cacheFilter.invalidateByWrapper(flux, predicate);
            };

            this.updateByColumnInvalidate = (mono, ingredientId) ->
                    cacheFilter.invalidateByWrapper(mono, cacheFilter.byMasterPredicate(ingredientId));

            this.deleteByColumnInvalidate = (mono, ingredientId) ->
                    cacheFilter.invalidateByWrapper(mono, cacheFilter.byMasterAndIds(ingredientId));
        }
    }

}
