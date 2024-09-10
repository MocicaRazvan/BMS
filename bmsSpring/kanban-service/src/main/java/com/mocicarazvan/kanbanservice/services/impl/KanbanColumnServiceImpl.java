package com.mocicarazvan.kanbanservice.services.impl;

import com.mocicarazvan.kanbanservice.dtos.columns.KanbanColumnBody;
import com.mocicarazvan.kanbanservice.dtos.columns.KanbanColumnResponse;
import com.mocicarazvan.kanbanservice.dtos.columns.ReindexKanbanColumnsList;
import com.mocicarazvan.kanbanservice.mappers.KanbanColumnMapper;
import com.mocicarazvan.kanbanservice.models.KanbanColumn;
import com.mocicarazvan.kanbanservice.repositories.KanbanColumnRepository;
import com.mocicarazvan.kanbanservice.services.KanbanColumnService;
import com.mocicarazvan.templatemodule.adapters.CacheChildFilteredToHandlerAdapter;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheChildFilterKey;
import com.mocicarazvan.templatemodule.cache.keys.ChildFilterKey;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.generic.IdGenerateDto;
import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.K;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Service
@Slf4j
public class KanbanColumnServiceImpl
        extends ManyToOneUserServiceImpl<KanbanColumn, KanbanColumnBody, KanbanColumnResponse, KanbanColumnRepository, KanbanColumnMapper>
        implements KanbanColumnService {

    private final EntitiesUtils entitiesUtils;
    private final KanbanColumnServiceCacheHandler kanbanColumnServiceCacheHandler;
    private final KanbanTaskServiceImpl.KanbanTaskServiceCacheHandler kanbanTaskServiceCacheHandler;

    public KanbanColumnServiceImpl(KanbanColumnRepository modelRepository, KanbanColumnMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils, KanbanColumnServiceCacheHandler kanbanColumnServiceCacheHandler, KanbanTaskServiceImpl.KanbanTaskServiceCacheHandler kanbanTaskServiceCacheHandler) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "kanbanColumn", List.of("id", "userId", "title", "createdAt", "updatedAt", "orderIndex"), kanbanColumnServiceCacheHandler);
        this.entitiesUtils = entitiesUtils;
        this.kanbanColumnServiceCacheHandler = kanbanColumnServiceCacheHandler;
        this.kanbanTaskServiceCacheHandler = kanbanTaskServiceCacheHandler;
    }

    @Override
    public Mono<KanbanColumnResponse> updateModel(Long id, KanbanColumnBody kanbanColumnBody, String userId) {
        return
                kanbanTaskServiceCacheHandler.getUpdateByColumnInvalidate().apply(
                        super.updateModel(id, kanbanColumnBody, userId), id);
    }

    @Override
    public Mono<KanbanColumnResponse> deleteModel(Long id, String userId) {
        return
                kanbanTaskServiceCacheHandler.getDeleteByColumnInvalidate().apply(
                        super.deleteModel(id, userId), id);
    }

    @Override
    public Flux<KanbanColumnResponse> getAllByUserId(String userId) {
        Sort sort = Sort.by(Sort.Direction.ASC, "orderIndex");

        return
                kanbanColumnServiceCacheHandler.getAllByUserIdPersist.apply(
                        modelRepository.findAllByUserId(Long.valueOf(userId), sort)
                                .map(modelMapper::fromModelToResponse), userId);
    }

    @Override
    public Mono<Void> reindex(ReindexKanbanColumnsList reindexKanbanColumnsList, String userId) {
        Pair<List<Long>, List<Integer>> pair = reindexKanbanColumnsList.getColumns()
                .stream().reduce(
                        Pair.of(new ArrayList<>(), new ArrayList<>()),
                        (p, c) -> {
                            p.getFirst().add(c.getId());
                            p.getSecond().add(c.getOrderIndex());
                            return p;
                        },
                        (p1, p2) -> {
                            p1.getFirst().addAll(p2.getFirst());
                            p1.getSecond().addAll(p2.getSecond());
                            return p1;
                        }
                );
        return
                kanbanColumnServiceCacheHandler.reindexInvalidate.apply(
                                modelRepository.findAllByIdIn(pair.getFirst())
                                        .switchIfEmpty(Mono.error(new RuntimeException("Columns not found")))
                                        .flatMap(c -> entitiesUtils.checkOwner(c, userId)
                                                .then(Mono.fromCallable(() -> {
                                                    log.error("Reindexing column {}", c.getId());
                                                    int orderIndex = pair.getSecond().get(pair.getFirst().indexOf(c.getId()));
                                                    c.setOrderIndex(orderIndex);
                                                    c.setUpdatedAt(LocalDateTime.now());
                                                    return c;
                                                }))
                                                .flatMap(modelRepository::save)), pair.getFirst(), userId)
                        .then();
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    @Component
    public static class KanbanColumnServiceCacheHandler extends ManyToOneUserServiceCacheHandler<KanbanColumn, KanbanColumnBody, KanbanColumnResponse> {

        Function3<Flux<KanbanColumn>, List<Long>, String, Flux<KanbanColumn>> reindexInvalidate;
        Function2<Flux<KanbanColumnResponse>, String, Flux<KanbanColumnResponse>> getAllByUserIdPersist;

        private final FilteredListCaffeineCacheChildFilterKey<KanbanColumnResponse> cacheFilter;
        private final KanbanColumnMapper modelMapper;

        public KanbanColumnServiceCacheHandler(FilteredListCaffeineCacheChildFilterKey<KanbanColumnResponse> cacheFilter, KanbanColumnMapper modelMapper) {
            super();
            this.cacheFilter = cacheFilter;
            this.modelMapper = modelMapper;
            CacheChildFilteredToHandlerAdapter.convertToManyUserHandler(
                    cacheFilter, this,
                    KanbanColumnResponse::getUserId,
                    modelMapper::fromModelToResponse
            );

            this.reindexInvalidate = (flux, ids, userId) ->
                    cacheFilter.invalidateByWrapper(flux, cacheFilter.combinePredicatesOr(
                            cacheFilter.byMasterAndIds(Long.valueOf(userId)),
                            cacheFilter.byIdsList(ids)
                    ));

            this.getAllByUserIdPersist = (flux, userId) ->
                    cacheFilter.getExtraUniqueFluxCacheForMasterIndependentOfRouteType(
                            EntitiesUtils.getListOfNotNullObjects(Long.valueOf(userId)),
                            "getAllByUserIdPersist" + userId,
                            IdGenerateDto::getId,
                            Long.valueOf(userId),
                            flux
                    );
        }
    }
}
