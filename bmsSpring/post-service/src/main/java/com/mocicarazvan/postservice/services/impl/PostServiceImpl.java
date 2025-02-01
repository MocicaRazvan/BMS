package com.mocicarazvan.postservice.services.impl;


import com.mocicarazvan.postservice.clients.CommentClient;
import com.mocicarazvan.postservice.dtos.PostBody;
import com.mocicarazvan.postservice.dtos.PostResponse;
import com.mocicarazvan.postservice.dtos.PostResponseWithSimilarity;
import com.mocicarazvan.postservice.dtos.comments.CommentResponse;
import com.mocicarazvan.postservice.mappers.PostMapper;
import com.mocicarazvan.postservice.models.Post;
import com.mocicarazvan.postservice.repositories.PostExtendedRepository;
import com.mocicarazvan.postservice.repositories.PostRepository;
import com.mocicarazvan.postservice.services.PostService;
import com.mocicarazvan.rediscache.annotation.RedisReactiveApprovedCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveApprovedCacheEvict;
import com.mocicarazvan.rediscache.annotation.RedisReactiveCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveCacheEvict;
import com.mocicarazvan.rediscache.enums.BooleanEnum;
import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.services.RabbitMqApprovedSender;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.impl.ApprovedServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;


@Service
@Slf4j
public class PostServiceImpl extends ApprovedServiceImpl<Post, PostBody, PostResponse, PostRepository, PostMapper, PostServiceImpl.PostServiceRedisCacheWrapper>
        implements PostService {

    private final CommentClient commentClient;
    private final PostExtendedRepository postExtendedRepository;
    private final RabbitMqApprovedSender<PostResponse> rabbitMqSender;
    private final TransactionalOperator transactionalOperator;
    private final PostEmbedServiceImpl postEmbedServiceImpl;


    public PostServiceImpl(PostRepository modelRepository, PostMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils, FileClient fileClient, CommentClient commentClient, PostExtendedRepository postExtendedRepository, RabbitMqApprovedSender<PostResponse> rabbitMqSender, PostServiceRedisCacheWrapper self, TransactionalOperator transactionalOperator, PostEmbedServiceImpl postEmbedServiceImpl, RabbitMqUpdateDeleteService<Post> rabbitMqUpdateDeleteService) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "post", List.of("id", "userId", "title", "createdAt", "updatedAt", "approved"),
                entitiesUtils, fileClient, rabbitMqSender, self, rabbitMqUpdateDeleteService
        );
        this.commentClient = commentClient;
        this.postExtendedRepository = postExtendedRepository;
        this.rabbitMqSender = rabbitMqSender;
        this.transactionalOperator = transactionalOperator;
        this.postEmbedServiceImpl = postEmbedServiceImpl;
    }

    @Override
    public Mono<List<String>> seedEmbeddings() {
        return modelRepository.findAll()
                .flatMap(post -> postEmbedServiceImpl.saveEmbedding(post.getId(), post.getTitle()).then(Mono.just("Seeded embeddings for post: " + post.getId())))
                .collectList()
                .as(transactionalOperator::transactional);
    }

    @Override
    @RedisReactiveCacheEvict(key = CACHE_KEY_PATH, id = "#id")
    public Mono<PostResponse> reactToModel(Long id, String type, String userId) {
        return super.reactToModel(id, type, userId);
    }

    @Override
    @RedisReactiveCacheEvict(key = CACHE_KEY_PATH, id = "#id")
    public Mono<PostResponse> deleteModel(Long id, String userId) {
        return
                userClient.getUser("", userId)
                        .flatMap(authUser -> getModel(id)
                                .flatMap(model ->
                                        transactionalOperator.transactional(privateRoute(true, authUser, model.getUserId())
                                                        .then(commentClient.deleteCommentsByPostId(id.toString(), userId))
                                                        .then(postEmbedServiceImpl.deleteEmbedding(id))
                                                        .then(modelRepository.delete(model)))
                                                .doOnSuccess(_ -> rabbitMqUpdateDeleteService.sendDeleteMessage(model))
                                                .then(Mono.fromCallable(() -> modelMapper.fromModelToResponse(model))

                                                )
                                )
                        );
    }

    @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "id")
    @Override
    public Flux<PostResponseWithSimilarity> getSimilarPosts(Long id, int limit, Double minSimilarity) {
        return
                existsByIdAndApprovedIsTrue(id).thenMany(
                        modelRepository.getSimilarPosts(id, limit, minSimilarity)
                                .map(modelMapper::fromPostWithSimilarityToResponse)

                );
    }

    @Override
    public Post cloneModel(Post post) {
        return post.clone();
    }

    @Override
    public Mono<Pair<PostResponse, Boolean>> deleteModelGetOriginalApproved(Long id, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> getModel(id)
                        .flatMap(model -> {
                                    Boolean originalApproved = model.isApproved();
                                    return transactionalOperator.transactional(privateRoute(true, authUser, model.getUserId())
                                                    .then(commentClient.deleteCommentsByPostId(id.toString(), userId))
                                                    .then(postEmbedServiceImpl.deleteEmbedding(id))
                                                    .then(modelRepository.delete(model)))
                                            .doOnSuccess(_ -> rabbitMqUpdateDeleteService.sendDeleteMessage(model))
                                            .then(Mono.fromCallable(() -> modelMapper.fromModelToResponse(model)))
                                            .map(r -> Pair.of(r, originalApproved))
                                            .flatMap(self::updateDeleteInvalidate);

                                }
                        )
                );
    }

    @Override
    public Mono<Void> existsByIdAndApprovedIsTrue(Long id) {
        return
                self.existByIdApproved(id)
                        .doOnNext(e -> log.error("existsByIdAndApprovedIsTrue: " + id))
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(new NotFoundEntity("post", id))
                        ).then();

    }

    @Override
    public Mono<ResponseWithChildList<PostResponse, ResponseWithUserDto<CommentResponse>>> getPostWithComments(Long id, boolean approved) {
        return modelRepository.findByApprovedAndId(approved, id)
                .switchIfEmpty(Mono.error(new NotFoundEntity("post", id)))
                .flatMap(post -> commentClient.getCommentsByPost(post.getId().toString())
                        .collectList()
                        .map(comments -> new ResponseWithChildList<>(modelMapper.fromModelToResponse(post), comments))
                );
    }


    @Override
    public Flux<PageableResponse<ResponseWithUserDto<PostResponse>>> getPostsFilteredWithUser(String title, PageableBody pageableBody, String userId,
                                                                                              Boolean approved, List<String> tags, Boolean liked, Boolean admin, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                              LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound) {

        return getPostsFiltered(title, pageableBody, userId, approved, tags, liked, admin,
                createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound
        ).flatMapSequential(this::getPageableWithUser);
    }

    @Override
    public Flux<PageableResponse<PostResponse>> getPostsFiltered(String title, PageableBody pageableBody, String userId, Boolean approved,
                                                                 List<String> tags, Boolean liked, Boolean admin,
                                                                 LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                 LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound
    ) {
        final String finalTitle = title == null ? "" : title;
        final Long likedUserId = (liked != null && liked) ? Long.valueOf(userId) : null;

        final boolean approvedNotNull = approved != null;
        return protectRoute(approvedNotNull, pageableBody, userId)
                .flatMapMany(
                        pr ->
                                self.getPostFilteredBase(finalTitle, approved, tags, likedUserId, admin, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound, pr)
                );
    }

    @Override
    public Flux<PageableResponse<PostResponse>> getModelsTrainer(String title, Long trainerId, PageableBody pageableBody, String userId, Boolean approved, List<String> tags,
                                                                 LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                 LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound) {
        String newTitle = title == null ? "" : title.trim();
        return getModelsAuthor(trainerId, pageableBody, userId, pr ->

                self.getPostFilteredTrainerBase(newTitle, approved, tags, trainerId, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound, pr)


        );
    }

    @Override
    public Mono<PostResponse> createModel(Flux<FilePart> images, PostBody postBody, String userId, String clientId) {
        return
                super.createModel(images, postBody, userId, clientId)
                        .flatMap(r -> postEmbedServiceImpl.saveEmbedding(r.getId(), r.getTitle()).then(Mono.just(r))).as(transactionalOperator::transactional);
    }

    @Override
    public Mono<Pair<PostResponse, Boolean>> updateModelWithImagesGetOriginalApproved(Flux<FilePart> images, Long id, PostBody postBody, String userId, String clientId) {
        return super.updateModelWithImagesGetOriginalApproved(images, id, postBody, userId, clientId, (b, origTitle, n) ->
                postEmbedServiceImpl.updateEmbeddingWithZip(b.getTitle(), origTitle, n.getId(), modelRepository.save(n))
        );
    }

    @Getter
    @Component
    public static class PostServiceRedisCacheWrapper extends ApprovedServiceRedisCacheWrapper<Post, PostBody, PostResponse, PostRepository, PostMapper> {

        private final PostExtendedRepository postExtendedRepository;

        public PostServiceRedisCacheWrapper(PostRepository modelRepository, PostMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, PostExtendedRepository postExtendedRepository) {
            super(modelRepository, modelMapper, "post", pageableUtils, userClient);
            this.postExtendedRepository = postExtendedRepository;
        }

        @Override
        @RedisReactiveApprovedCache(key = CACHE_KEY_PATH, idPath = "entity.id", approved = BooleanEnum.NULL, forWhom = "0")
        public Flux<MonthlyEntityGroup<PostResponse>> getModelGroupedByMonthBase(int month, UserDto userDto) {
            return super.getModelGroupedByMonthBase(month, userDto);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<Post> getModel(Long id) {
            return super.getModel(id);
        }

        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<Post> getModelInternal(Long id) {
            return super.getModel(id);
        }


        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "id")
        public Flux<Post> findAllById(List<Long> ids) {
            return super.findAllById(ids);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<ResponseWithUserDto<PostResponse>> getModelByIdWithUserBase(UserDto authUser, Long id) {
            return super.getModelByIdWithUserBase(authUser, id);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<ResponseWithUserLikesAndDislikes<PostResponse>> getModelByIdWithUserLikesAndDislikesBase(Long id, UserDto authUser) {
            return super.getModelByIdWithUserLikesAndDislikesBase(id, authUser);
        }

        @Override
        @RedisReactiveApprovedCache(key = CACHE_KEY_PATH, approvedArgumentPath = "#approved", idPath = "content.id")
        public Flux<PageableResponse<PostResponse>> getModelsTitleBase(boolean approved, PageRequest pr, String newTitle) {
            return super.getModelsTitleBase(approved, pr, newTitle);
        }

        @Override
        @RedisReactiveApprovedCacheEvict(key = CACHE_KEY_PATH, forWhomPath = "#r.userId")
        protected Mono<Pair<PostResponse, Boolean>> createInvalidate(PostResponse r) {
            return super.createInvalidate(r);
        }

        @Override
        @RedisReactiveApprovedCacheEvict(key = CACHE_KEY_PATH, id = "#p.getFirst().getId()", forWhomPath = "#p.getFirst().getUserId()")
        protected Mono<Pair<PostResponse, Boolean>> updateDeleteInvalidate(Pair<PostResponse, Boolean> p) {
            return super.updateDeleteInvalidate(p);
        }

        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        protected Mono<Boolean> existByIdApproved(Long id) {
            return modelRepository.existsByIdAndApprovedIsTrue(id);
        }

        @RedisReactiveApprovedCache(key = CACHE_KEY_PATH, idPath = "content.id", approvedArgumentPath = "#approved", forWhom = "#admin?0:-1")
        public Flux<PageableResponse<PostResponse>> getPostFilteredBase(String finalTitle,
                                                                        Boolean approved, List<String> tags, Long likedUserId, Boolean admin,
                                                                        LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                        LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                        PageRequest pr) {
            return
                    pageableUtils.createPageableResponse(
                            postExtendedRepository.getPostsFiltered(finalTitle, approved, tags, likedUserId, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound, pr)
                                    .map(modelMapper::fromModelToResponse),
                            postExtendedRepository.countPostsFiltered(finalTitle, approved, tags,
                                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                                    likedUserId)
                            , pr
                    );
        }

        @RedisReactiveApprovedCache(key = CACHE_KEY_PATH, idPath = "content.id", approvedArgumentPath = "#approved", forWhom = "#trainerId")
        public Flux<PageableResponse<PostResponse>> getPostFilteredTrainerBase(String newTitle,
                                                                               Boolean approved, List<String> tags, Long trainerId,
                                                                               LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                               LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                               PageRequest pr) {
            return pageableUtils.createPageableResponse(
                    postExtendedRepository.getPostsFilteredTrainer(newTitle, approved, tags, trainerId,
                            createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                            pr).map(modelMapper::fromModelToResponse),
                    postExtendedRepository.countPostsFilteredTrainer(newTitle, approved, trainerId, tags,
                            createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound),
                    pr);
        }

    }


}
