package com.mocicarazvan.commentservice.services.impl;

import com.mocicarazvan.commentservice.dtos.CommentBody;
import com.mocicarazvan.commentservice.dtos.CommentResponse;
import com.mocicarazvan.commentservice.enums.CommentReferenceType;
import com.mocicarazvan.commentservice.mappers.CommentMapper;
import com.mocicarazvan.commentservice.models.Comment;
import com.mocicarazvan.commentservice.repositories.CommentRepository;
import com.mocicarazvan.commentservice.services.CommentService;
import com.mocicarazvan.templatemodule.adapters.CacheChildFilteredToHandlerAdapter;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheChildFilterKey;
import com.mocicarazvan.templatemodule.clients.ReferenceClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.services.impl.TitleBodyServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function4;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class CommentServiceImpl extends TitleBodyServiceImpl<Comment, CommentBody, CommentResponse, CommentRepository, CommentMapper>
        implements CommentService {

    private final Map<CommentReferenceType, ReferenceClient> referenceClients;
    private final CommentServiceCacheHandler commentServiceCacheHandler;


    public CommentServiceImpl(CommentRepository modelRepository, CommentMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils, Map<CommentReferenceType, ReferenceClient> referenceClients, CommentServiceCacheHandler commentServiceCacheHandler) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "comment", List.of("id", "userId", "postId", "title", "createdAt"), entitiesUtils, commentServiceCacheHandler);
        this.referenceClients = referenceClients;
        this.commentServiceCacheHandler = commentServiceCacheHandler;
    }

    private ReferenceClient getClient(CommentReferenceType referenceType) {
        ReferenceClient client = referenceClients.get(referenceType);
        if (client == null) {
            throw new IllegalArgumentException("Reference type not supported");
        }
        return client;
    }


    @Override
    public Mono<CommentResponse> createModel(Long postId, CommentBody commentBody, String userId, CommentReferenceType referenceType) {
        return
                commentServiceCacheHandler.getCreateModelInvalidate().apply(
                        getClient(referenceType).existsApprovedReference(postId.toString())
                                .then(Mono.defer(() -> {
                                    Comment comment = modelMapper.fromBodyToModel(commentBody);
                                    comment.setReferenceId(postId);
                                    comment.setUserId(Long.valueOf(userId));
                                    comment.setUserDislikes(new ArrayList<>());
                                    comment.setUserLikes(new ArrayList<>());
                                    comment.setCreatedAt(LocalDateTime.now());
                                    comment.setUpdatedAt(LocalDateTime.now());
                                    comment.setReferenceType(referenceType);
                                    return modelRepository.save(comment)
                                            .map(modelMapper::fromModelToResponse);
                                })), commentBody, userId);
    }

    @Override
    public Flux<PageableResponse<CommentResponse>> getCommentsByReference(Long postId, PageableBody pageableBody, CommentReferenceType referenceType) {
        return getClient(referenceType).existsApprovedReference(postId.toString())
                .thenMany(pageableUtils.createPageRequest(pageableBody)
                        .flatMapMany(pr ->
                                commentServiceCacheHandler.getCommentsByReferencePersist.apply(
                                        pageableUtils.createPageableResponse(
                                                modelRepository.findAllByReferenceIdAndReferenceType(postId, referenceType, pr).map(modelMapper::fromModelToResponse),
                                                modelRepository.countAllByReferenceIdAndReferenceType(postId, referenceType),
                                                pr
                                        ), postId, pageableBody, referenceType))

                );

    }

    @Override
    public Flux<PageableResponse<ResponseWithUserDto<CommentResponse>>> getCommentsWithUserByReference(Long postId, PageableBody pageableBody, CommentReferenceType referenceType) {
//        return getClient(referenceType).existsApprovedReference(postId.toString())
//                .thenMany(pageableUtils.createPageRequest(pageableBody)
////                        .doOnNext(pr -> log.info("Page request: {}", pr))
//                                .flatMapMany(pr ->
//                                        pageableUtils.createPageableResponse(
//                                                modelRepository.findAllByReferenceIdAndReferenceType(postId, referenceType, pr)
////                                                        .doOnNext(c -> log.error("service " + c.getCreatedAt().toString()))
//                                                        .concatMap(c ->
//                                                                userClient.getUser("", c.getUserId().toString())
//                                                                        .map(user -> ResponseWithUserDto.<CommentResponse>builder()
//                                                                                .user(user)
//                                                                                .model(modelMapper.fromModelToResponse(c))
//                                                                                .build()
//                                                                        )
//                                                        ),
//                                                modelRepository.countAllByReferenceIdAndReferenceType(postId, referenceType),
//                                                pr
//                                        ))
//                );
        return getCommentsByReference(postId, pageableBody, referenceType)
                .concatMap(this::getPageableWithUser);
    }


    @Override
    public Flux<ResponseWithUserDto<CommentResponse>> getCommentsByReference(Long postId, CommentReferenceType referenceType) {
        return getClient(referenceType).existsApprovedReference(postId.toString())
                .thenMany(
                        modelRepository.findAllByReferenceIdAndReferenceType(postId, referenceType).concatMap(c ->
                                userClient.getUser("", c.getUserId().toString())
                                        .map(user -> ResponseWithUserDto.<CommentResponse>builder()
                                                .user(user)
                                                .model(modelMapper.fromModelToResponse(c))
                                                .build()
                                        ))
                );
    }

    @Override
    public Flux<PageableResponse<CommentResponse>> getModelByUser(Long userId, PageableBody pageableBody, CommentReferenceType referenceType) {

        return pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(userClient.getUser("", userId.toString()))
                .thenMany(pageableUtils.createPageRequest(pageableBody)
                        .flatMapMany(pr ->
                                pageableUtils.createPageableResponse(
                                        (referenceType == null ? modelRepository.findAllByUserId(userId, pr)
                                                : modelRepository.findAllByUserIdAndReferenceType(userId, referenceType, pr))
                                                .map(modelMapper::fromModelToResponse),
                                        (
                                                referenceType == null ? modelRepository.countAllByUserId(userId)
                                                        : modelRepository.countAllByUserIdAndReferenceType(userId, referenceType)
                                        ),
                                        pr
                                )));
    }

    @Override
    public Mono<Void> deleteCommentsByReference(Long referenceId, String userId, Role role, CommentReferenceType referenceType) {
        if (role.equals(Role.ROLE_USER)) {
            return Mono.error(new IllegalArgumentException("User can't delete comments"));
        }

        return
                commentServiceCacheHandler.deleteCommentsByReferenceInvalidate.apply(
                        userClient.getUser("", userId)
                                .flatMap(user -> {
                                    log.info("User role: {}", user.getRole());
                                    if (role.equals(Role.ROLE_ADMIN) && !user.getRole().equals(Role.ROLE_ADMIN)) {
                                        return Mono.error(new PrivateRouteException());
                                    }
                                    return
                                            getClient(referenceType).getReferenceById(referenceId.toString(), userId, ApproveDto.class)
                                                    .flatMap(post -> {
                                                        if (role.equals(Role.ROLE_TRAINER) && (
                                                                !user.getRole().equals(Role.ROLE_ADMIN) && !post.getUserId().toString().equals(userId)
                                                        )) {
                                                            return Mono.error(new PrivateRouteException());
                                                        }
                                                        return modelRepository.deleteAllByReferenceIdEqualsAndReferenceType(referenceId, referenceType);
                                                    });
                                })
                                .then(), referenceId);
    }

    @Override
    public Mono<CommentResponse> deleteModel(Long id, String userId) {
        return
                commentServiceCacheHandler.getDeleteModelInvalidate().apply(
                        userClient.getUser("", userId)
                                .flatMap(authUser -> getModel(id)
                                        .flatMap(model -> isNotAuthor(model, authUser)
                                                .map(notAuthor -> {
                                                    if (notAuthor && authUser.getRole() == Role.ROLE_ADMIN) {
                                                        return Mono.error(new PrivateRouteException());
                                                    }
                                                    return Mono.empty();
                                                })
                                                .then(modelRepository.delete(model))
                                                .thenReturn(modelMapper.fromModelToResponse(model))
                                        )
                                ), id, userId);
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    @Component
    public static class CommentServiceCacheHandler
            extends TitleBodyServiceImpl.TitleBodyServiceCacheHandler<Comment, CommentBody, CommentResponse> {
        private final FilteredListCaffeineCacheChildFilterKey<CommentResponse> cacheFilter;
        private final CommentMapper commentMapper;
        private Function4<Flux<PageableResponse<CommentResponse>>, Long, PageableBody, CommentReferenceType, Flux<PageableResponse<CommentResponse>>>
                getCommentsByReferencePersist;
        private Function2<Mono<Void>, Long, Mono<Void>> deleteCommentsByReferenceInvalidate;

        public CommentServiceCacheHandler(FilteredListCaffeineCacheChildFilterKey<CommentResponse> cacheFilter, CommentMapper commentMapper) {
            super();
            this.cacheFilter = cacheFilter;
            this.commentMapper = commentMapper;
            CacheChildFilteredToHandlerAdapter.convertToTitleBodyHandler(cacheFilter, this,
                    CommentResponse::getReferenceId, commentMapper::fromModelToResponse
            );

//            this.setGetModelGroupedByMonthPersist(((monthlyEntityGroupFlux, userDto, month) ->
//                    cacheFilter.getExtraUniqueCacheForAdmin(
//                            EntitiesUtils.getListOfNotNullObjects(userDto, month),
//                            "getModelGroupedByMonthPersist" + month,
//                            rm -> rm.getEntity().getId(),
//                            cacheFilter.getDefaultMap(),
//                            monthlyEntityGroupFlux
//                    )
//            ));
//
//            this.setGetModelByIdPersist((mono, authUser, id) ->
//                            mono.flatMap(
//                                    model -> cacheFilter.getExtraUniqueMonoCacheIndependent(
//                                            EntitiesUtils.getListOfNotNullObjects(id),
//                                            "getModelByIdPersist" + id,
//                                            IdGenerateDto::getId,
////                                    model.getReferenceId(),
//                                            Mono.just(model)
//                                    )
//                            )
//            );
//
//            this.setGetByIdInternalPersist((mono, id) ->
//                            mono.flatMap(
//                                    model -> cacheFilter.getExtraUniqueMonoCacheIndependent(
//                                            EntitiesUtils.getListOfNotNullObjects(id),
//                                            "getByIdInternalPersist" + id,
//                                            IdGenerated::getId,
////                                    model.getReferenceId(),
//                                            Mono.just(model)
//                                    )
//                            )
//            );
//
//            this.setGetModelByIdWithUserPersist((mono, authUser, id) ->
//                            mono.flatMap(
//                                    model -> cacheFilter.getExtraUniqueMonoCacheIndependent(
//                                            EntitiesUtils.getListOfNotNullObjects(id),
//                                            "getModelByIdWithUserPersist" + id,
//                                            m -> m.getModel().getId(),
////                                    model.getModel().getReferenceId(),
//                                            Mono.just(model)
//                                    )
//                            )
//            );

            this.getCommentsByReferencePersist = (flux, postId, pageableBody, referenceType) ->
                    cacheFilter.getExtraUniqueFluxCacheForMasterIndependentOfRouteType(
                            EntitiesUtils.getListOfNotNullObjects(postId, pageableBody, referenceType),
                            "getCommentsByReferencePersist" + postId,
                            c -> c.getContent().getId(),
                            postId,
                            flux
                    );

            this.deleteCommentsByReferenceInvalidate = (mono, postId) ->
                    cacheFilter.invalidateByWrapper(mono, cacheFilter.byMasterAndIds(postId));
        }
    }
}
