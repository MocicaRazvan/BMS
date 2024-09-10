package com.mocicarazvan.kanbanservice.controllers;

import com.mocicarazvan.kanbanservice.dtos.tasks.GroupedKanbanTask;
import com.mocicarazvan.kanbanservice.dtos.tasks.KanbanTaskBody;
import com.mocicarazvan.kanbanservice.dtos.tasks.KanbanTaskResponse;
import com.mocicarazvan.kanbanservice.hateos.KanbanTaskReactiveResponseBuilder;
import com.mocicarazvan.kanbanservice.mappers.KanbanTaskMapper;
import com.mocicarazvan.kanbanservice.models.KanbanTask;
import com.mocicarazvan.kanbanservice.repositories.KanbanTaskRepository;
import com.mocicarazvan.kanbanservice.services.KanbanTaskService;
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
@RequestMapping("/kanban/task")
@RequiredArgsConstructor
public class KanbanTaskController
        implements ManyToOneUserController
        <KanbanTask, KanbanTaskBody, KanbanTaskResponse, KanbanTaskRepository, KanbanTaskMapper, KanbanTaskService> {

    private final KanbanTaskService kanbanTaskService;
    private final KanbanTaskReactiveResponseBuilder kanbanTaskReactiveResponseBuilder;
    private final RequestsUtils requestsUtils;

    @Override
    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<CustomEntityModel<KanbanTaskResponse>>> deleteModel(@PathVariable Long id, ServerWebExchange exchange) {
        return kanbanTaskService.deleteModel(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(c -> kanbanTaskReactiveResponseBuilder.toModel(c, KanbanTaskController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CustomEntityModel<KanbanTaskResponse>>> getModelById(@PathVariable Long id, ServerWebExchange exchange) {
        return kanbanTaskService.getModelById(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(c -> kanbanTaskReactiveResponseBuilder.toModel(c, KanbanTaskController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/withUser/{id}")
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<KanbanTaskResponse>>> getModelByIdWithUser(@PathVariable Long id, ServerWebExchange exchange) {
        return kanbanTaskService.getModelByIdWithUser(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(c -> kanbanTaskReactiveResponseBuilder.toModelWithUser(c, KanbanTaskController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PutMapping("/update/{id}")
    public Mono<ResponseEntity<CustomEntityModel<KanbanTaskResponse>>> updateModel(@Valid @RequestBody KanbanTaskBody kanbanTaskBody,
                                                                                   @PathVariable Long id, ServerWebExchange exchange) {
        return kanbanTaskService.updateModel(id, kanbanTaskBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(c -> kanbanTaskReactiveResponseBuilder.toModel(c, KanbanTaskController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/byIds")
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<KanbanTaskResponse>>> getModelsByIdIn(@Valid @RequestBody PageableBody pageableBody, @RequestParam List<Long> ids) {
        return kanbanTaskService.getModelsByIdInPageable(ids, pageableBody)
                .flatMap(c -> kanbanTaskReactiveResponseBuilder.toModelPageable(c, KanbanTaskController.class));
    }

    @Override
    @PostMapping("/create")
    public Mono<ResponseEntity<CustomEntityModel<KanbanTaskResponse>>> createModel(@Valid @RequestBody KanbanTaskBody kanbanTaskBody, ServerWebExchange exchange) {
        return kanbanTaskService.createModel(kanbanTaskBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(c -> kanbanTaskReactiveResponseBuilder.toModel(c, KanbanTaskController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/admin/groupedByMonth")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyEntityGroup<CustomEntityModel<KanbanTaskResponse>>> getModelGroupedByMonth(@RequestParam int month, ServerWebExchange exchange) {
        return kanbanTaskService.getModelGroupedByMonth(month, requestsUtils.extractAuthUser(exchange))
                .flatMap(c -> kanbanTaskReactiveResponseBuilder.toModelMonthlyEntityGroup(c, KanbanTaskController.class));
    }

    @GetMapping("/byColumnId/{columnId}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<CustomEntityModel<KanbanTaskResponse>> getByColumnId(@PathVariable Long columnId) {
        return kanbanTaskService.getByColumnId(columnId)
                .flatMap(c -> kanbanTaskReactiveResponseBuilder.toModel(c, KanbanTaskController.class));
    }

    @PostMapping("/reindex")
    public Mono<ResponseEntity<Void>> reindex(@Valid @RequestBody GroupedKanbanTask groupedKanbanTask, ServerWebExchange exchange) {
        return kanbanTaskService.reindex(groupedKanbanTask, requestsUtils.extractAuthUser(exchange))
                .then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }
}
