package com.mocicarazvan.postservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.postservice.dtos.PostBody;
import com.mocicarazvan.postservice.dtos.PostResponse;
import com.mocicarazvan.postservice.dtos.comments.CommentResponse;
import com.mocicarazvan.postservice.hateos.PostReactiveResponseBuilder;
import com.mocicarazvan.postservice.mappers.PostMapper;
import com.mocicarazvan.postservice.models.Post;
import com.mocicarazvan.postservice.repositories.PostRepository;
import com.mocicarazvan.postservice.services.PostService;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.controllers.ApproveController;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController implements ApproveController
        <Post, PostBody, PostResponse,
                PostRepository, PostMapper,
                PostService> {

    private final PostService postService;
    private final PostReactiveResponseBuilder postReactiveResponseBuilder;
    private final RequestsUtils requestsUtils;
    private final ObjectMapper objectMapper;

    @Override
    @PatchMapping(value = "/approved", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<PostResponse>>> getModelsApproved(
            @RequestParam(required = false) String title, @Valid @RequestBody PageableBody pageableBody, ServerWebExchange exchange
    ) {
        return postService.getModelsApproved(title, pageableBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> postReactiveResponseBuilder.toModelPageable(m, PostController.class));
    }

    @Override
    @PatchMapping("/withUser")
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithUserDtoEntity<PostResponse>>> getModelsWithUser(@RequestParam(required = false) String title,
                                                                                             @RequestParam(name = "approved", required = false, defaultValue = "true") boolean approved,
                                                                                             @Valid @RequestBody PageableBody pageableBody,
                                                                                             @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
                                                                                             ServerWebExchange exchange) {

        log.error("approved: " + approved);
        return postService.
                getModelsWithUser(title, pageableBody, requestsUtils.extractAuthUser(exchange), approved)
                .concatMap(m -> postReactiveResponseBuilder.toModelWithUserPageable(m, PostController.class));
    }

    @PatchMapping("/tags/withUser")
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithUserDtoEntity<PostResponse>>> getPostsFilteredWithUser(@RequestParam(required = false) String title,
                                                                                                    @RequestParam(name = "approved", required = false) Boolean approved,
                                                                                                    @RequestParam(required = false) List<String> tags,
                                                                                                    @RequestParam(required = false) Boolean liked,
                                                                                                    @Valid @RequestBody PageableBody pageableBody,
                                                                                                    @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
                                                                                                    ServerWebExchange exchange) {

        log.error("admin: " + admin);
        FilterKeyType.KeyRouteType keyRouteType = Boolean.TRUE.equals(admin) ? FilterKeyType.KeyRouteType.createForAdmin() : FilterKeyType.KeyRouteType.createForPublic();
        return

                postService.
                        getPostsFilteredWithUser(title, pageableBody, requestsUtils.extractAuthUser(exchange), approved, tags, liked, admin)
//                .delayElements(Duration.ofSeconds(3))
                        .concatMap(m -> postReactiveResponseBuilder.toModelWithUserPageable(m, PostController.class))
                ;
    }

    @PatchMapping("/tags")
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<PostResponse>>> getPostsFiltered(@RequestParam(required = false) String title,
                                                                                    @RequestParam(name = "approved", required = false) Boolean approved,
                                                                                    @RequestParam(required = false) List<String> tags,
                                                                                    @RequestParam(required = false) Boolean liked,
                                                                                    @Valid @RequestBody PageableBody pageableBody,
                                                                                    @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
                                                                                    ServerWebExchange exchange) {

        log.error("admin: " + admin);
        FilterKeyType.KeyRouteType keyRouteType = Boolean.TRUE.equals(admin) ? FilterKeyType.KeyRouteType.createForAdmin() : FilterKeyType.KeyRouteType.createForPublic();

        return
                postService.
                        getPostsFiltered(title, pageableBody, requestsUtils.extractAuthUser(exchange), approved, tags, liked, admin)
//                .delayElements(Duration.ofSeconds(3))
                        .flatMap(m -> postReactiveResponseBuilder.toModelPageable(m, PostController.class));
    }

    @Override
    @PatchMapping(value = "/trainer/{trainerId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<PostResponse>>> getModelsTrainer(@RequestParam(required = false) String title,
                                                                                    @RequestParam(required = false) Boolean approved,
                                                                                    @Valid @RequestBody PageableBody pageableBody,
                                                                                    @PathVariable Long trainerId,
                                                                                    ServerWebExchange exchange) {
        return postService.getModelsTrainer(title, trainerId, pageableBody, requestsUtils.extractAuthUser(exchange), approved)
                .flatMap(m -> postReactiveResponseBuilder.toModelPageable(m, PostController.class));
    }

    @PatchMapping(value = "/trainer/tags/{trainerId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<PostResponse>>> getModelsTrainerTags(@RequestParam(required = false) String title,
                                                                                        @RequestParam(required = false) Boolean approved,
                                                                                        @RequestParam(required = false) List<String> tags,
                                                                                        @Valid @RequestBody PageableBody pageableBody,
                                                                                        @PathVariable Long trainerId,
                                                                                        ServerWebExchange exchange) {
        return
                postService.getModelsTrainer(title, trainerId, pageableBody, requestsUtils.extractAuthUser(exchange), approved, tags)
                        .flatMap(m -> postReactiveResponseBuilder.toModelPageable(m, PostController.class));
    }

    @Override
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PostResponse>>> createModel(@Valid @RequestBody PostBody body, ServerWebExchange exchange) {
        return postService.createModel(body, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> postReactiveResponseBuilder.toModel(m, PostController.class))
                .map(ResponseEntity::ok)
                ;
    }


    @Override
    @GetMapping("/admin/groupedByMonth")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyEntityGroup<CustomEntityModel<PostResponse>>> getModelGroupedByMonth(@RequestParam int month, ServerWebExchange exchange) {
        return
                postService.getModelGroupedByMonth(month, requestsUtils.extractAuthUser(exchange))
                        .flatMap(m -> postReactiveResponseBuilder.toModelMonthlyEntityGroup(m, PostController.class));
    }

    @Override
    @PatchMapping(value = "/admin/approve/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<PostResponse>>> approveModel(@PathVariable Long id,
                                                                                      @RequestParam boolean approved,
                                                                                      ServerWebExchange exchange) {
        return postService.approveModel(id, requestsUtils.extractAuthUser(exchange), approved)
                .flatMap(m -> postReactiveResponseBuilder.toModelWithUser(m, PostController.class))
                .map(ResponseEntity::ok);
    }


    @Override
    @PatchMapping(value = "/admin", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<PostResponse>>> getAllModelsAdmin(@RequestParam(required = false) String title,
                                                                                     @Valid @RequestBody PageableBody pageableBody,
                                                                                     ServerWebExchange exchange) {
        return postService.getAllModels(title, pageableBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> postReactiveResponseBuilder.toModelPageable(m, PostController.class));
    }

    @Override
    @DeleteMapping(value = "/delete/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PostResponse>>> deleteModel(@PathVariable Long id, ServerWebExchange exchange) {
        return postService.deleteModelGetOriginalApproved(id, requestsUtils.extractAuthUser(exchange))
                .map(Pair::getFirst)
                .flatMap(m -> postReactiveResponseBuilder.toModel(m, PostController.class))
                .map(ResponseEntity::ok);
    }


    @Override
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PostResponse>>> getModelById(@PathVariable Long id, ServerWebExchange exchange) {
        return postService.getModelById(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> postReactiveResponseBuilder.toModel(m, PostController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/withUser/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<PostResponse>>> getModelByIdWithUser(@PathVariable Long id, ServerWebExchange exchange) {
        return
                postService.getModelByIdWithUser(id, requestsUtils.extractAuthUser(exchange))
                        .flatMap(m -> postReactiveResponseBuilder.toModelWithUser(m, PostController.class))
                        .map(ResponseEntity::ok);
    }

    @Override
    @PutMapping(value = "/update/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PostResponse>>> updateModel(@Valid @RequestBody PostBody postBody, @PathVariable Long id, ServerWebExchange exchange) {
        return postService.updateModel(id, postBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> postReactiveResponseBuilder.toModel(m, PostController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PatchMapping(value = "/byIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<PostResponse>>> getModelsByIdIn(@Valid @RequestBody PageableBody pageableBody,
                                                                                   @RequestParam List<Long> ids) {
        return postService.getModelsByIdInPageable(ids, pageableBody)
                .flatMap(m -> postReactiveResponseBuilder.toModelPageable(m, PostController.class));
    }

    // todo id displaying nr likes or dislikes invalidate the cache for id
    @Override
    @PatchMapping(value = "/like/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PostResponse>>> likeModel(@PathVariable Long id, ServerWebExchange exchange) {
        return postService.reactToModel(id, "like", requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> postReactiveResponseBuilder.toModel(m, PostController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PatchMapping(value = "/dislike/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PostResponse>>> dislikeModel(@PathVariable Long id, ServerWebExchange exchange) {
        return postService.reactToModel(id, "dislike", requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> postReactiveResponseBuilder.toModel(m, PostController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/withUser/withReactions/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserLikesAndDislikesEntity<PostResponse>>> getModelsWithUserAndReaction(@PathVariable Long id, ServerWebExchange exchange) {
        return postService.getModelByIdWithUserLikesAndDislikes(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> postReactiveResponseBuilder.toModelWithUserLikesAndDislikes(m, PostController.class))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/internal/existsApproved/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<Void>> existsById(@PathVariable Long id) {
        return
                postService.existsByIdAndApprovedIsTrue(id)
                        .then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }

    @GetMapping(value = "/withComments/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithChildListEntity<PostResponse, ResponseWithUserDto<CommentResponse>>>>
    getPostWithComments(@PathVariable Long id) {
        return postService.getPostWithComments(id, true)
                .map(m -> new ResponseWithChildListEntity<>(CustomEntityModel.of(m.getEntity()), m.getChildren()))
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/createWithImages", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public Mono<ResponseEntity<CustomEntityModel<PostResponse>>> createModelWithImages(
            @RequestPart("files") Flux<FilePart> files,
            @RequestPart("body") String body,
            @RequestParam("clientId") String clientId,
            ServerWebExchange exchange) {
//        PostBody postBody = null;
//        try {
//            postBody = objectMapper.readValue(body, PostBody.class);
//        } catch (JsonProcessingException e) {
//            return Mono.error(e);
//        }

        return requestsUtils.getBodyFromJson(body, PostBody.class, objectMapper)
                .flatMap(postBody -> postService.createModel(files, postBody, requestsUtils.extractAuthUser(exchange), clientId)
                        .flatMap(m -> postReactiveResponseBuilder.toModel(m, PostController.class)))

                .map(ResponseEntity::ok)
                ;
    }

    @PostMapping(value = "/updateWithImages/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public Mono<ResponseEntity<CustomEntityModel<PostResponse>>> updateModelWithImages(
            @RequestPart("files") Flux<FilePart> files,
            @RequestPart("body") String body,
            @RequestParam("clientId") String clientId,
            @PathVariable Long id,
            ServerWebExchange exchange) {
        // getting original approved status of the post
        return
                requestsUtils.getBodyFromJson(body, PostBody.class, objectMapper)
                        .flatMap(postBody -> postService.updateModelWithImagesGetOriginalApproved(files, id, postBody, requestsUtils.extractAuthUser(exchange), clientId)
                                .flatMap(m -> postReactiveResponseBuilder.toModelWithPair(m, PostController.class))
                                .map(Pair::getFirst)
                        )
                        .map(ResponseEntity::ok);
    }


}
