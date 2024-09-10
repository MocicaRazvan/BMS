package com.mocicarazvan.postservice.services.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.postservice.repositories.PostRepository;
import com.mocicarazvan.postservice.repositories.PostExtendedRepository;
import com.mocicarazvan.templatemodule.adapters.CacheApprovedFilteredToHandlerAdapter;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheApproveFilterKey;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithChildList;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.services.impl.ApprovedServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import com.mocicarazvan.postservice.clients.CommentClient;
import com.mocicarazvan.postservice.dtos.PostBody;
import com.mocicarazvan.postservice.dtos.PostResponse;
import com.mocicarazvan.postservice.dtos.comments.CommentResponse;
import com.mocicarazvan.postservice.mappers.PostMapper;
import com.mocicarazvan.postservice.models.Post;
import com.mocicarazvan.postservice.services.PostService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.function.Function2;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.Function5;
import reactor.function.Function6;
import reactor.function.Function7;

import java.util.List;


@Service
@Slf4j
public class PostServiceImpl extends ApprovedServiceImpl<Post, PostBody, PostResponse, PostRepository, PostMapper>
        implements PostService {

    private final CommentClient commentClient;
    private final PostExtendedRepository postExtendedRepository;
    private final PostServiceCacheHandler postServiceCacheHandler;


    public PostServiceImpl(PostRepository modelRepository, PostMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils, FileClient fileClient, ObjectMapper objectMapper, CommentClient commentClient, PostExtendedRepository postExtendedRepository, PostServiceCacheHandler postServiceCacheHandler) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "post", List.of("id", "userId", "title", "createdAt", "updatedAt", "approved"), entitiesUtils, fileClient, objectMapper, postServiceCacheHandler);
        this.commentClient = commentClient;
        this.postExtendedRepository = postExtendedRepository;
        this.postServiceCacheHandler = postServiceCacheHandler;
    }


    @Override
    public Mono<PostResponse> deleteModel(Long id, String userId) {
        return
                postServiceCacheHandler.getDeleteModelInvalidate().apply(
                        userClient.getUser("", userId)
                                .flatMap(authUser -> getModel(id)
                                        .flatMap(model -> privateRoute(true, authUser, model.getUserId())
                                                .then(commentClient.deleteCommentsByPostId(id.toString(), userId))
                                                .then(modelRepository.delete(model))
                                                .then(Mono.fromCallable(() -> modelMapper.fromModelToResponse(model)))
                                        )
                                ), id, userId);
    }

    @Override
    public Mono<Void> existsByIdAndApprovedIsTrue(Long id) {
        return
                postServiceCacheHandler.existsByIdAndApprovedPersist.apply(
                        modelRepository.existsByIdAndApprovedIsTrue(id)
                                .doOnNext(e -> log.error("existsByIdAndApprovedIsTrue: " + id))
                                .filter(Boolean::booleanValue)
                                .switchIfEmpty(Mono.error(new NotFoundEntity("post", id)))
                        , id).then();

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
    public Flux<PageableResponse<ResponseWithUserDto<PostResponse>>> getPostsFilteredWithUser(String title, PageableBody pageableBody, String userId, Boolean approved, List<String> tags, Boolean liked, Boolean admin) {

        return getPostsFiltered(title, pageableBody, userId, approved, tags, liked, admin).concatMap(this::getPageableWithUser);
    }

    @Override
    public Flux<PageableResponse<PostResponse>> getPostsFiltered(String title, PageableBody pageableBody, String userId, Boolean approved, List<String> tags, Boolean liked, Boolean admin) {
        final String finalTitle = title == null ? "" : title;
        final Long likedUserId = (liked != null && liked) ? Long.valueOf(userId) : null;

        final boolean approvedNotNull = approved != null;
        return protectRoute(approvedNotNull, pageableBody, userId)
                .flatMapMany(
                        pr ->
                                postServiceCacheHandler.getPostsFilteredPersist.apply(
                                        pageableUtils.createPageableResponse(
                                                postExtendedRepository.getPostsFiltered(finalTitle, approved, tags, likedUserId, pr)
                                                        .doOnNext(post -> log.error("post: " + post))
                                                        .map(modelMapper::fromModelToResponse),
                                                postExtendedRepository.countPostsFiltered(finalTitle, approved, tags, likedUserId)
                                                        .doOnNext(count -> log.error("count: " + count))
                                                , pr
                                        ), finalTitle, approved, tags, likedUserId, pageableBody, admin)
                );
    }

    @Override
    public Flux<PageableResponse<PostResponse>> getModelsTrainer(String title, Long trainerId, PageableBody pageableBody, String userId, Boolean approved, List<String> tags) {
        String newTitle = title == null ? "" : title.trim();
        return getModelsAuthor(trainerId, pageableBody, userId, pr ->

                postServiceCacheHandler.getPostsFilteredTrainerPersist.apply(

                        (pageableUtils.createPageableResponse(
                                postExtendedRepository.getPostsFilteredTrainer(newTitle, approved, tags, trainerId, pr).map(modelMapper::fromModelToResponse),
                                postExtendedRepository.countPostsFilteredTrainer(newTitle, approved, trainerId, tags),
                                pr
                        )), newTitle, approved, trainerId, tags, pageableBody
                )

        );
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Component
    public static class PostServiceCacheHandler
            extends ApprovedServiceImpl.ApprovedServiceCacheHandler<Post, PostBody, PostResponse> {
        private final FilteredListCaffeineCacheApproveFilterKey<PostResponse> cacheFilter;

        Function7<Flux<PageableResponse<PostResponse>>, String, Boolean, List<String>, Long, PageableBody, Boolean, Flux<PageableResponse<PostResponse>>> getPostsFilteredPersist;
        Function6<Flux<PageableResponse<PostResponse>>, String, Boolean, Long, List<String>, PageableBody, Flux<PageableResponse<PostResponse>>> getPostsFilteredTrainerPersist;
        Function2<Mono<Boolean>, Long, Mono<Boolean>> existsByIdAndApprovedPersist;


        public PostServiceCacheHandler(FilteredListCaffeineCacheApproveFilterKey<PostResponse> cacheFilter) {
            super();
            this.cacheFilter = cacheFilter;
            CacheApprovedFilteredToHandlerAdapter.convert(cacheFilter, this);

            this.getPostsFilteredPersist = (flux, title, approved, tags, likedUserId, pageableBody, admin) -> {
                FilterKeyType.KeyRouteType keyRouteType = Boolean.TRUE.equals(admin) ? FilterKeyType.KeyRouteType.createForAdmin() : FilterKeyType.KeyRouteType.createForPublic();
                return cacheFilter.getExtraUniqueFluxCache(
                        EntitiesUtils.getListOfNotNullObjects(title, approved, tags, likedUserId, pageableBody),
                        "getPostsFiltered",
                        m -> m.getContent().getId(),
                        keyRouteType,
                        approved, flux
                );
            };


            this.getPostsFilteredTrainerPersist = (flux, title, approved, trainerId, tags, pageableBody) ->
                    cacheFilter.getExtraUniqueCacheForTrainer(
                            EntitiesUtils.getListOfNotNullObjects(title, approved, trainerId, tags, pageableBody),
                            trainerId,
                            "getPostsFilteredTrainer" + trainerId,
                            m -> m.getContent().getId(),
                            approved,
                            flux
                    );


            this.existsByIdAndApprovedPersist = (mono, id) -> cacheFilter.getExtraUniqueMonoCacheIndependent(
                    EntitiesUtils.getListOfNotNullObjects(id),
                    "existsByIdAndApprovedPersist" + id,
                    v -> id,
                    mono
            );

        }
    }

}
