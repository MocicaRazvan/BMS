package com.mocicarazvan.commentservice.controllers;

import com.mocicarazvan.commentservice.dtos.CommentBody;
import com.mocicarazvan.commentservice.dtos.CommentResponse;
import com.mocicarazvan.commentservice.enums.CommentReferenceType;
import com.mocicarazvan.commentservice.hateos.CommentReactiveResponseBuilder;
import com.mocicarazvan.commentservice.mappers.CommentMapper;
import com.mocicarazvan.commentservice.models.Comment;
import com.mocicarazvan.commentservice.repositories.CommentRepository;
import com.mocicarazvan.commentservice.services.CommentService;
import com.mocicarazvan.templatemodule.controllers.TitleBodyController;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController implements TitleBodyController<Comment, CommentBody, CommentResponse, CommentRepository,
        CommentMapper, CommentService> {

    private final CommentService commentService;
    private final CommentReactiveResponseBuilder commentReactiveResponseBuilder;
    private final RequestsUtils requestsUtils;

    @Override
    @DeleteMapping(value = "/delete/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<CommentResponse>>> deleteModel(
            @PathVariable Long id, ServerWebExchange exchange) {
        return commentService.deleteModel(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> commentReactiveResponseBuilder.toModel(m, CommentController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<CommentResponse>>> getModelById(@PathVariable Long id, ServerWebExchange exchange) {
        return commentService.getModelById(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> commentReactiveResponseBuilder.toModel(m, CommentController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/withUser/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<CommentResponse>>> getModelByIdWithUser(@PathVariable Long id, ServerWebExchange exchange) {
        return commentService.getModelByIdWithUser(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> commentReactiveResponseBuilder.toModelWithUser(m, CommentController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PutMapping(value = "/update/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<CommentResponse>>> updateModel(@Valid @RequestBody CommentBody commentBody, @PathVariable Long id, ServerWebExchange exchange) {
        return commentService.updateModel(id, commentBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> commentReactiveResponseBuilder.toModel(m, CommentController.class))
                .map(ResponseEntity::ok);
    }


    @Override
    @PatchMapping(value = "/like/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<CommentResponse>>> likeModel(@PathVariable Long id, ServerWebExchange exchange) {
        return commentService.reactToModel(id, "like", requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> commentReactiveResponseBuilder.toModel(m, CommentController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PatchMapping(value = "/dislike/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<CommentResponse>>> dislikeModel(@PathVariable Long id, ServerWebExchange exchange) {
        return commentService.reactToModel(id, "dislike", requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> commentReactiveResponseBuilder.toModel(m, CommentController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/withUser/withReactions/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserLikesAndDislikesEntity<CommentResponse>>> getModelsWithUserAndReaction(@PathVariable Long id, ServerWebExchange exchange) {
        return commentService.getModelByIdWithUserLikesAndDislikes(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> commentReactiveResponseBuilder.toModelWithUserLikesAndDislikes(m, CommentController.class))
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/create/{referenceType}/{referenceId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<CommentResponse>>> createModel(
            @PathVariable Long referenceId,
            @PathVariable String referenceType,
            @Valid @RequestBody CommentBody commentBody,
            ServerWebExchange exchange
    ) {
        CommentReferenceType commentReferenceType = CommentReferenceType.valueOf(referenceType.toUpperCase());
        return commentService.createModel(referenceId, commentBody, requestsUtils.extractAuthUser(exchange), commentReferenceType)
                .flatMap(m -> commentReactiveResponseBuilder.toModel(m, CommentController.class))
                .map(ResponseEntity::ok);
    }


    @PatchMapping(value = "/{referenceType}/{referenceId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<CommentResponse>>> getCommentsByReference(
            @PathVariable Long referenceId,
            @Valid @RequestBody PageableBody pageableBody,
            @PathVariable String referenceType
    ) {
        CommentReferenceType commentReferenceType = CommentReferenceType.valueOf(referenceType.toUpperCase());
        return commentService.getCommentsByReference(referenceId, pageableBody, commentReferenceType)
                .flatMapSequential(m -> commentReactiveResponseBuilder.toModelPageable(m, CommentController.class));
    }

    @PatchMapping(value = "/user/{referenceType}/{userId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<CommentResponse>>> getCommentsByUser(
            @PathVariable Long userId,
            @Valid @RequestBody PageableBody pageableBody,
            @PathVariable String referenceType

    ) {
        CommentReferenceType commentReferenceType = CommentReferenceType.valueOf(referenceType.toUpperCase());
        return commentService.getModelByUser(userId, pageableBody, commentReferenceType)
                .flatMapSequential(m -> commentReactiveResponseBuilder.toModelPageable(m, CommentController.class));
    }

    @PatchMapping(value = "/byIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<CommentResponse>>> getModelsByIdIn(@Valid @RequestBody PageableBody pageableBody,
                                                                                      @RequestParam List<Long> ids) {
        return commentService.getModelsByIdInPageable(ids, pageableBody)
                .flatMapSequential(m -> commentReactiveResponseBuilder.toModelPageable(m, CommentController.class));
    }

    @Override
    public Mono<ResponseEntity<CustomEntityModel<CommentResponse>>> createModel(CommentBody commentBody, ServerWebExchange exchange) {
        return null;
    }

    @Override
    @GetMapping("/admin/groupedByMonth")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyEntityGroup<CustomEntityModel<CommentResponse>>> getModelGroupedByMonth(@RequestParam int month, ServerWebExchange exchange) {
        return commentService.getModelGroupedByMonth(month, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> commentReactiveResponseBuilder.toModelMonthlyEntityGroup(m, CommentController.class));
    }

    @PatchMapping(value = "/withUser/{referenceType}/{referenceId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithUserDtoEntity<CommentResponse>>> getCommentsWithUserByReference(
            @PathVariable Long referenceId,
            @Valid @RequestBody PageableBody pageableBody,
            @PathVariable String referenceType
    ) {
        CommentReferenceType commentReferenceType = CommentReferenceType.valueOf(referenceType.toUpperCase());

        return commentService.getCommentsWithUserByReference(referenceId, pageableBody, commentReferenceType)
                .doOnNext(c -> log.error("controller " + c.getContent().getModel().getCreatedAt().toString()))
                .flatMapSequential(m -> commentReactiveResponseBuilder.toModelWithUserPageable(m, CommentController.class));
    }

    @DeleteMapping(value = "/admin/delete/{referenceType}/{referenceId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<Void>> deleteCommentsByReferenceAdmin(@PathVariable Long referenceId,
                                                                     @PathVariable String referenceType,
                                                                     ServerWebExchange exchange) {
        CommentReferenceType commentReferenceType = CommentReferenceType.valueOf(referenceType.toUpperCase());
        return commentService.deleteCommentsByReference(referenceId, requestsUtils.extractAuthUser(exchange), Role.ROLE_ADMIN, commentReferenceType)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @DeleteMapping(value = "/internal/{referenceType}/{referenceId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<Void>> deleteCommentsByReferenceInternal(@PathVariable Long referenceId, @PathVariable String referenceType, ServerWebExchange exchange) {
        CommentReferenceType commentReferenceType = CommentReferenceType.valueOf(referenceType.toUpperCase());
        return commentService.deleteCommentsByReference(referenceId, requestsUtils.extractAuthUser(exchange), Role.ROLE_TRAINER, commentReferenceType)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @GetMapping(value = "/internal/{referenceType}/{referenceId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Flux<ResponseWithUserDto<CommentResponse>> getCommentsByReferenceInternal(@PathVariable Long referenceId, @PathVariable String referenceType) {
        CommentReferenceType commentReferenceType = CommentReferenceType.valueOf(referenceType.toUpperCase());
        return commentService.getCommentsByReference(referenceId, commentReferenceType);
    }

}
