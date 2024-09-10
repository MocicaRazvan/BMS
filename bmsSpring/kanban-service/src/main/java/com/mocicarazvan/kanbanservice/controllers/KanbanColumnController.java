package com.mocicarazvan.kanbanservice.controllers;

import com.mocicarazvan.kanbanservice.dtos.columns.KanbanColumnBody;
import com.mocicarazvan.kanbanservice.dtos.columns.KanbanColumnResponse;
import com.mocicarazvan.kanbanservice.dtos.columns.ReindexKanbanColumnsList;
import com.mocicarazvan.kanbanservice.hateos.KanbanColumnReactiveResponseBuilder;
import com.mocicarazvan.kanbanservice.mappers.KanbanColumnMapper;
import com.mocicarazvan.kanbanservice.models.KanbanColumn;
import com.mocicarazvan.kanbanservice.repositories.KanbanColumnRepository;
import com.mocicarazvan.kanbanservice.services.KanbanColumnService;
import com.mocicarazvan.templatemodule.controllers.ManyToOneUserController;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/kanban/column")
@RequiredArgsConstructor
public class KanbanColumnController implements ManyToOneUserController
        <KanbanColumn, KanbanColumnBody, KanbanColumnResponse, KanbanColumnRepository, KanbanColumnMapper, KanbanColumnService> {

    private final KanbanColumnService kanbanColumnService;
    private final KanbanColumnReactiveResponseBuilder kanbanColumnReactiveResponseBuilder;
    private final RequestsUtils requestsUtils;

    @Override
    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<CustomEntityModel<KanbanColumnResponse>>> deleteModel(@PathVariable Long id, ServerWebExchange exchange) {
        return kanbanColumnService.deleteModel(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(c -> kanbanColumnReactiveResponseBuilder.toModel(c, KanbanColumnController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CustomEntityModel<KanbanColumnResponse>>> getModelById(@PathVariable Long id, ServerWebExchange exchange) {
        return kanbanColumnService.getModelById(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(c -> kanbanColumnReactiveResponseBuilder.toModel(c, KanbanColumnController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/withUser/{id}")
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<KanbanColumnResponse>>> getModelByIdWithUser(@PathVariable Long id, ServerWebExchange exchange) {
        return kanbanColumnService.getModelByIdWithUser(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(c -> kanbanColumnReactiveResponseBuilder.toModelWithUser(c, KanbanColumnController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PutMapping("/update/{id}")
    public Mono<ResponseEntity<CustomEntityModel<KanbanColumnResponse>>> updateModel(@Valid @RequestBody KanbanColumnBody kanbanColumnBody,
                                                                                     @PathVariable Long id, ServerWebExchange exchange) {
        return kanbanColumnService.updateModel(id, kanbanColumnBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(c -> kanbanColumnReactiveResponseBuilder.toModel(c, KanbanColumnController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/byIds")
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<KanbanColumnResponse>>> getModelsByIdIn(@Valid @RequestBody PageableBody pageableBody, @RequestParam List<Long> ids) {
        return kanbanColumnService.getModelsByIdInPageable(ids, pageableBody)
                .flatMap(c -> kanbanColumnReactiveResponseBuilder.toModelPageable(c, KanbanColumnController.class));
    }

    @Override
    @PostMapping("/create")
    public Mono<ResponseEntity<CustomEntityModel<KanbanColumnResponse>>> createModel(@Valid @RequestBody KanbanColumnBody kanbanColumnBody, ServerWebExchange exchange) {
        return kanbanColumnService.createModel(kanbanColumnBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(c -> kanbanColumnReactiveResponseBuilder.toModel(c, KanbanColumnController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/admin/groupedByMonth")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyEntityGroup<CustomEntityModel<KanbanColumnResponse>>> getModelGroupedByMonth(@RequestParam int month, ServerWebExchange exchange) {
        return kanbanColumnService.getModelGroupedByMonth(month, requestsUtils.extractAuthUser(exchange))
                .flatMap(c -> kanbanColumnReactiveResponseBuilder.toModelMonthlyEntityGroup(c, KanbanColumnController.class));
    }

    @GetMapping("/byUserId")
    @ResponseStatus(HttpStatus.OK)
    public Flux<CustomEntityModel<KanbanColumnResponse>> getAllByUserId(ServerWebExchange exchange) {
        return kanbanColumnService.getAllByUserId(requestsUtils.extractAuthUser(exchange))
                .flatMap(c -> kanbanColumnReactiveResponseBuilder.toModel(c, KanbanColumnController.class));
    }

    @PostMapping("/reindex")
    public Mono<ResponseEntity<Void>> reindex(@Valid @RequestBody ReindexKanbanColumnsList reindexKanbanColumnsList, ServerWebExchange exchange) {
        return kanbanColumnService.reindex(reindexKanbanColumnsList, requestsUtils.extractAuthUser(exchange))
                .then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }
}
