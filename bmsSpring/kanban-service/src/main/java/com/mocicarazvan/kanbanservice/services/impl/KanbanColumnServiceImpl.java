package com.mocicarazvan.kanbanservice.services.impl;

import com.mocicarazvan.kanbanservice.dtos.columns.KanbanColumnBody;
import com.mocicarazvan.kanbanservice.dtos.columns.KanbanColumnResponse;
import com.mocicarazvan.kanbanservice.dtos.columns.ReindexKanbanColumnsList;
import com.mocicarazvan.kanbanservice.mappers.KanbanColumnMapper;
import com.mocicarazvan.kanbanservice.models.KanbanColumn;
import com.mocicarazvan.kanbanservice.repositories.KanbanColumnRepository;
import com.mocicarazvan.kanbanservice.services.KanbanColumnService;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class KanbanColumnServiceImpl
        extends ManyToOneUserServiceImpl<KanbanColumn, KanbanColumnBody, KanbanColumnResponse, KanbanColumnRepository, KanbanColumnMapper>
        implements KanbanColumnService {

    private static final Logger log = LoggerFactory.getLogger(KanbanColumnServiceImpl.class);
    private final EntitiesUtils entitiesUtils;

    public KanbanColumnServiceImpl(KanbanColumnRepository modelRepository, KanbanColumnMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "kanbanColumn", List.of("id", "userId", "title", "createdAt", "updatedAt", "orderIndex"));
        this.entitiesUtils = entitiesUtils;
    }


    @Override
    public Flux<KanbanColumnResponse> getAllByUserId(String userId) {
        Sort sort = Sort.by(Sort.Direction.ASC, "orderIndex");

        return modelRepository.findAllByUserId(Long.valueOf(userId), sort)
                .map(modelMapper::fromModelToResponse);
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
        return modelRepository.findAllByIdIn(pair.getFirst())
                .switchIfEmpty(Mono.error(new RuntimeException("Columns not found")))
                .flatMap(c -> entitiesUtils.checkOwner(c, userId)
                        .then(Mono.fromCallable(() -> {
                            log.error("Reindexing column {}", c.getId());
                            int orderIndex = pair.getSecond().get(pair.getFirst().indexOf(c.getId()));
                            c.setOrderIndex(orderIndex);
                            c.setUpdatedAt(LocalDateTime.now());
                            return c;
                        }))
                        .flatMap(modelRepository::save))
                .then();
    }
}
