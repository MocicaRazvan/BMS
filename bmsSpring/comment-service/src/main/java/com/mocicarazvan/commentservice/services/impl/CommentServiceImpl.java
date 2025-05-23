package com.mocicarazvan.commentservice.services.impl;

import com.mocicarazvan.commentservice.dtos.CommentBody;
import com.mocicarazvan.commentservice.dtos.CommentResponse;
import com.mocicarazvan.commentservice.enums.CommentReferenceType;
import com.mocicarazvan.commentservice.mappers.CommentMapper;
import com.mocicarazvan.commentservice.models.Comment;
import com.mocicarazvan.commentservice.repositories.CommentRepository;
import com.mocicarazvan.commentservice.services.CommentService;
import com.mocicarazvan.rediscache.annotation.RedisReactiveCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveCacheEvict;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCacheEvict;
import com.mocicarazvan.templatemodule.clients.ReferenceClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserLikesAndDislikes;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.repositories.AssociativeEntityRepository;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.impl.TitleBodyServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
@Getter
public class CommentServiceImpl extends TitleBodyServiceImpl<Comment, CommentBody, CommentResponse, CommentRepository, CommentMapper,
        CommentServiceImpl.CommentServiceRedisCacheWrapper
        >
        implements CommentService {

    private final Map<CommentReferenceType, ReferenceClient> referenceClients;


    public CommentServiceImpl(CommentRepository modelRepository, CommentMapper modelMapper, PageableUtilsCustom pageableUtils,
                              UserClient userClient, EntitiesUtils entitiesUtils, Map<CommentReferenceType, ReferenceClient> referenceClients,
                              CommentServiceRedisCacheWrapper self, RabbitMqUpdateDeleteService<Comment> rabbitMqUpdateDeleteService,
                              TransactionalOperator transactionalOperator, @Qualifier("userLikesRepository") AssociativeEntityRepository userLikesRepository,
                              @Qualifier("userDislikesRepository") AssociativeEntityRepository userDislikesRepository) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "comment", List.of("id", "userId", "postId", "title", "createdAt"),
                entitiesUtils, self, rabbitMqUpdateDeleteService, transactionalOperator, userLikesRepository, userDislikesRepository);
        this.referenceClients = referenceClients;
    }


    private ReferenceClient getClient(CommentReferenceType referenceType) {
        ReferenceClient client = referenceClients.get(referenceType);
        if (client == null) {
            throw new IllegalArgumentException("Reference type not supported");
        }
        return client;
    }


    @Override
    @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.id")
    public Flux<PageableResponse<CommentResponse>> getAllModels(PageableBody pageableBody, String userId) {
        return super.getAllModels(pageableBody, userId);
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, id = "#id", masterPath = "referenceId")
    public Mono<CommentResponse> updateModel(Long id, CommentBody body, String userId) {
        return super.updateModel(id, body, userId);
    }

    @Override
    public Comment cloneModel(Comment comment) {
        return comment.clone();
    }


    @Override
    @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.id")
    public Flux<PageableResponse<CommentResponse>> getModelsByIdInPageable(List<Long> ids, PageableBody pageableBody) {
        return super.getModelsByIdInPageable(ids, pageableBody);
    }

    @Override
    @RedisReactiveCacheEvict(key = CACHE_KEY_PATH, id = "#id")
    public Mono<CommentResponse> reactToModel(Long id, String type, String userId) {
        return super.reactToModel(id, type, userId);
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, masterPath = "referenceId")
    public Mono<CommentResponse> createModel(Long postId, CommentBody commentBody, String userId, CommentReferenceType referenceType) {

        return
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
                        }));
    }

    @Override
    public Flux<PageableResponse<CommentResponse>> getCommentsByReference(Long postId, PageableBody pageableBody, CommentReferenceType referenceType) {


        return self.getCommentsByReference(postId, pageableBody, referenceType, getClient(referenceType));

    }

    @Override
    public Flux<PageableResponse<ResponseWithUserDto<CommentResponse>>> getCommentsWithUserByReference(Long postId, PageableBody pageableBody, CommentReferenceType referenceType) {

        return getCommentsByReference(postId, pageableBody, referenceType)
                .flatMapSequential(this::getPageableWithUser);
    }


    @Override
    public Flux<ResponseWithUserDto<CommentResponse>> getCommentsByReference(Long postId, CommentReferenceType referenceType) {
        return self.getCommentsByReference(postId, referenceType, getClient(referenceType));
    }

    @Override
    public Flux<PageableResponse<CommentResponse>> getModelByUser(Long userId, PageableBody pageableBody, CommentReferenceType referenceType) {

        return pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(userClient.getUser("", userId.toString()))
                .thenMany(self.getCommentsByUserBase(userId, pageableBody, referenceType));
    }


    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, masterId = "#referenceId")
    public Mono<Void> deleteCommentsByReference(Long referenceId, String userId, Role role, CommentReferenceType referenceType) {
        if (role.equals(Role.ROLE_USER)) {
            return Mono.error(new IllegalArgumentException("User can't delete comments"));
        }


        return
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
                                                return
                                                        modelRepository.findAllByReferenceIdAndReferenceType(referenceId, referenceType)
                                                                .collectList()
                                                                .doOnSuccess(rabbitMqUpdateDeleteService::sendBatchDeleteMessage)
                                                                .then(
                                                                        modelRepository.deleteAllByReferenceIdEqualsAndReferenceType(referenceId, referenceType));
                                            });
                        })
                        .then();
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, id = "#id", masterPath = "referenceId")
    public Mono<CommentResponse> deleteModel(Long id, String userId) {

        return
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
                                        .doOnSuccess(_ -> rabbitMqUpdateDeleteService.sendDeleteMessage(model))
                                        .thenReturn(modelMapper.fromModelToResponse(model))
                                )
                        );
    }

    @Getter
    @Component
    public static class CommentServiceRedisCacheWrapper extends TitleBodyServiceImpl.TitleBodyServiceRedisCacheWrapper<Comment, CommentBody, CommentResponse, CommentRepository, CommentMapper> {

        private final CommentRepository modelRepository;
        private final CommentMapper modelMapper;
        private final PageableUtilsCustom pageableUtils;

        public CommentServiceRedisCacheWrapper(CommentRepository modelRepository, CommentMapper modelMapper, UserClient userClient, PageableUtilsCustom pageableUtils) {
            super(modelRepository, modelMapper, "comment", userClient);
            this.modelRepository = modelRepository;
            this.modelMapper = modelMapper;
            this.pageableUtils = pageableUtils;
        }


        @Override
        @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "entity.id")
        public Flux<MonthlyEntityGroup<CommentResponse>> getModelGroupedByMonthBase(int month, UserDto userDto) {
            return super.getModelGroupedByMonthBase(month, userDto);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<Comment> getModel(Long id) {
            return super.getModel(id);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "id")
        public Flux<Comment> findAllById(List<Long> ids) {
            return super.findAllById(ids);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<ResponseWithUserDto<CommentResponse>> getModelByIdWithUserBase(UserDto authUser, Long id) {
            return super.getModelByIdWithUserBase(authUser, id);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<ResponseWithUserLikesAndDislikes<CommentResponse>> getModelByIdWithUserLikesAndDislikesBase(Long id, UserDto authUser) {
            return super.getModelByIdWithUserLikesAndDislikesBase(id, authUser);
        }


        @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "model.id", masterId = "#postId")
        public Flux<ResponseWithUserDto<CommentResponse>> getCommentsByReference(Long postId, CommentReferenceType referenceType,
                                                                                 ReferenceClient referenceClient
        ) {
            return referenceClient.existsApprovedReference(postId.toString())
                    .thenMany(
                            modelRepository.findAllByReferenceIdAndReferenceType(postId, referenceType).flatMapSequential(c ->
                                    userClient.getUser("", c.getUserId().toString())
                                            .map(user -> ResponseWithUserDto.<CommentResponse>builder()
                                                    .user(user)
                                                    .model(modelMapper.fromModelToResponse(c))
                                                    .build()
                                            ))
                    );
        }

        @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.id", masterId = "#postId")
        public Flux<PageableResponse<CommentResponse>> getCommentsByReference(Long postId, PageableBody pageableBody, CommentReferenceType referenceType, ReferenceClient client) {
            return client.existsApprovedReference(postId.toString())
                    .thenMany(pageableUtils.createPageRequest(pageableBody)
                            .flatMapMany(pr ->
                                    pageableUtils.createPageableResponse(
                                            modelRepository.findAllByReferenceIdAndReferenceType(postId, referenceType, pr).map(modelMapper::fromModelToResponse),
                                            modelRepository.countAllByReferenceIdAndReferenceType(postId, referenceType),
                                            pr
                                    ))

                    );
        }

        @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.id")
        public Flux<PageableResponse<CommentResponse>> getCommentsByUserBase(Long userId, PageableBody pageableBody, CommentReferenceType referenceType) {
            return pageableUtils.createPageRequest(pageableBody)
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
                            ));
        }


    }


}
