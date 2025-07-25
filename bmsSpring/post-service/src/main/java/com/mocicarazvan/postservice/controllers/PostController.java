package com.mocicarazvan.postservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.postservice.dtos.PostBody;
import com.mocicarazvan.postservice.dtos.PostResponse;
import com.mocicarazvan.postservice.dtos.PostResponseWithSimilarity;
import com.mocicarazvan.postservice.dtos.comments.CommentResponse;
import com.mocicarazvan.postservice.hateos.PostReactiveResponseBuilder;
import com.mocicarazvan.postservice.mappers.PostMapper;
import com.mocicarazvan.postservice.models.Post;
import com.mocicarazvan.postservice.repositories.PostRepository;
import com.mocicarazvan.postservice.services.PostService;
import com.mocicarazvan.templatemodule.controllers.ApproveController;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqSenderImpl;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.util.Pair;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/posts")
@Slf4j
public class PostController implements ApproveController
        <Post, PostBody, PostResponse,
                PostRepository, PostMapper,
                PostService> {

    private final PostService postService;
    private final PostReactiveResponseBuilder postReactiveResponseBuilder;
    private final RequestsUtils requestsUtils;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public PostController(PostService postService, PostReactiveResponseBuilder postReactiveResponseBuilder,
                          RequestsUtils requestsUtils, ObjectMapper objectMapper,
                          RabbitTemplate rabbitTemplate) {
        this.postService = postService;
        this.postReactiveResponseBuilder = postReactiveResponseBuilder;
        this.requestsUtils = requestsUtils;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @PatchMapping(value = "/approved", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<PostResponse>>> getModelsApproved(
            @RequestParam(required = false) String title, @Valid @RequestBody PageableBody pageableBody, ServerWebExchange exchange
    ) {
        return postService.getModelsApproved(title, pageableBody, requestsUtils.extractAuthUser(exchange))
                .flatMapSequential(m -> postReactiveResponseBuilder.toModelPageable(m, PostController.class));
    }

    @Override
    @PatchMapping("/withUser")
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithUserDtoEntity<PostResponse>>> getModelsWithUser(@RequestParam(required = false) String title,
                                                                                             @RequestParam(name = "approved", required = false, defaultValue = "true") boolean approved,
                                                                                             @Valid @RequestBody PageableBody pageableBody,
                                                                                             @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
                                                                                             ServerWebExchange exchange) {


        return postService.
                getModelsWithUser(title, pageableBody, requestsUtils.extractAuthUser(exchange), approved)
                .flatMapSequential(m -> postReactiveResponseBuilder.toModelWithUserPageable(m, PostController.class));
    }

    @PatchMapping("/tags/withUser")
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithUserDtoEntity<PostResponse>>> getPostsFilteredWithUser(@RequestParam(required = false) String title,
                                                                                                    @RequestParam(name = "approved", required = false) Boolean approved,
                                                                                                    @RequestParam(required = false) List<String> tags,
                                                                                                    @RequestParam(required = false) Boolean liked,
                                                                                                    @Valid @RequestBody PageableBody pageableBody,
                                                                                                    @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
                                                                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtLowerBound,
                                                                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtUpperBound,
                                                                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtLowerBound,
                                                                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtUpperBound,
                                                                                                    ServerWebExchange exchange) {


        return

                postService.
                        getPostsFilteredWithUser(title, pageableBody, requestsUtils.extractAuthUser(exchange), approved, tags, liked, admin, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound)
                        .flatMapSequential(m -> postReactiveResponseBuilder.toModelWithUserPageable(m, PostController.class));
    }

    @PatchMapping("/tags")
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<PostResponse>>> getPostsFiltered(@RequestParam(required = false) String title,
                                                                                    @RequestParam(name = "approved", required = false) Boolean approved,
                                                                                    @RequestParam(required = false) List<String> tags,
                                                                                    @RequestParam(required = false) Boolean liked,
                                                                                    @Valid @RequestBody PageableBody pageableBody,
                                                                                    @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
                                                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtLowerBound,
                                                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtUpperBound,
                                                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtLowerBound,
                                                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtUpperBound,
                                                                                    ServerWebExchange exchange) {

        return
                postService.
                        getPostsFiltered(title, pageableBody, requestsUtils.extractAuthUser(exchange), approved, tags, liked, admin, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound)
                        .flatMapSequential(m -> postReactiveResponseBuilder.toModelPageable(m, PostController.class));
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
                .flatMapSequential(m -> postReactiveResponseBuilder.toModelPageable(m, PostController.class));
    }

    @PatchMapping(value = "/trainer/tags/{trainerId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<PostResponse>>> getModelsTrainerTags(@RequestParam(required = false) String title,
                                                                                        @RequestParam(required = false) Boolean approved,
                                                                                        @RequestParam(required = false) List<String> tags,
                                                                                        @Valid @RequestBody PageableBody pageableBody,
                                                                                        @PathVariable Long trainerId,
                                                                                        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtLowerBound,
                                                                                        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtUpperBound,
                                                                                        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtLowerBound,
                                                                                        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtUpperBound,
                                                                                        ServerWebExchange exchange) {
        return
                postService.getModelsTrainer(title, trainerId, pageableBody, requestsUtils.extractAuthUser(exchange), approved, tags, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound)
                        .flatMapSequential(m -> postReactiveResponseBuilder.toModelPageable(m, PostController.class));
    }

    @Override
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PostResponse>>> createModel(@Valid @RequestBody PostBody body, ServerWebExchange exchange) {
        return postService.createModel(body, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> postReactiveResponseBuilder.toModel(m, PostController.class))
                .map(ResponseEntity::ok);
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
                .flatMapSequential(m -> postReactiveResponseBuilder.toModelPageable(m, PostController.class));
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
                .flatMapSequential(m -> postReactiveResponseBuilder.toModelPageable(m, PostController.class));
    }

    @Override
    @PatchMapping(value = "/like/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PostResponse>>> likeModel(@PathVariable Long id, ServerWebExchange exchange) {
        return postService.reactToModelInvalidateApproved(id, "like", requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> postReactiveResponseBuilder.toModel(m.getFirst(), PostController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PatchMapping(value = "/dislike/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PostResponse>>> dislikeModel(@PathVariable Long id, ServerWebExchange exchange) {
        return postService.reactToModelInvalidateApproved(id, "dislike", requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> postReactiveResponseBuilder.toModel(m.getFirst(), PostController.class))
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

    @GetMapping(value = "/seedEmbeddings")
    public Mono<ResponseEntity<List<String>>> seedEmbeddings() {
        return postService.seedEmbeddings()
                .map(ResponseEntity::ok);
    }


    @GetMapping("/testQueue")
    public Mono<String> testQueue(@RequestParam(required = false, defaultValue = "100") Integer nr) {
        var sender = new RabbitMqSenderImpl("post-exchange", "post-update-routing-key", rabbitTemplate, 8);
        var max = Math.ceil(nr / 1000.0);
        IntStream.range(0, (int) max).parallel().forEach(i -> {
            var posts = IntStream.range(0, 1000)
                    .mapToObj(j -> Post.builder().id((long) j).title("title" + j)
                            .body("body" + j)
                            .approved(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .userId(1L)
                            .tags(List.of())
                            .build())
                    .toList();
            sender.sendBatchMessage(posts);
            log.info("sent batch {} of {} ", i, (int) max);
        });


        return Mono.just("ok added: " + nr);
    }

    @GetMapping(value = "/similar/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<CustomEntityModel<PostResponseWithSimilarity>> getSimilarPosts(@PathVariable Long id,
                                                                               @RequestParam(required = false, defaultValue = "4") int limit,
                                                                               @RequestParam(required = false, defaultValue = "0.0") Double minSimilarity
    ) {
        return postService.getSimilarPosts(id, limit, minSimilarity)
                .flatMapSequential(m -> postReactiveResponseBuilder.toModelConvertSetContent(m, PostController.class, m
                ));
    }

    @GetMapping("/invalidateCache/{id}")
    public Mono<?> invalidateCache(@PathVariable Long id) {
        return postService.invalidateCache(id);
    }


    @PatchMapping("/demo/withUser")
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithUserDtoEntity<PostResponse>>> getPostsFilteredWithUserDemo(@RequestParam(required = false) String title,
                                                                                                        @RequestParam(name = "approved", required = false) Boolean approved,
                                                                                                        @RequestParam(required = false) List<String> tags,
                                                                                                        @RequestParam(required = false) Boolean liked,
                                                                                                        @Valid @RequestBody PageableBody pageableBody,
                                                                                                        @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
                                                                                                        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtLowerBound,
                                                                                                        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtUpperBound,
                                                                                                        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtLowerBound,
                                                                                                        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtUpperBound,
                                                                                                        @RequestParam(required = false, defaultValue = "0") Integer delay,
                                                                                                        ServerWebExchange exchange) {


        return

                postService.
                        getPostsFilteredWithUser(title, pageableBody, requestsUtils.extractAuthUser(exchange), approved, tags, liked, admin, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound)
                        .delayElements(Duration.ofSeconds(delay))
                        .flatMapSequential(m -> postReactiveResponseBuilder.toModelWithUserPageable(m, PostController.class));
    }

}
