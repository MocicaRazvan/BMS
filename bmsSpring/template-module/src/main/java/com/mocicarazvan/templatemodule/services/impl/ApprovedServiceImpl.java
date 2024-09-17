package com.mocicarazvan.templatemodule.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
import com.mocicarazvan.templatemodule.dtos.generic.TitleBodyDto;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserLikesAndDislikes;
import com.mocicarazvan.templatemodule.enums.FileType;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.Approve;
import com.mocicarazvan.templatemodule.repositories.ApprovedRepository;
import com.mocicarazvan.templatemodule.services.ApprovedService;
import com.mocicarazvan.templatemodule.services.RabbitMqApprovedSenderWrapper;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jooq.lambda.function.Function4;
import org.jooq.lambda.function.Function5;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.function.Function;

public abstract class ApprovedServiceImpl<MODEL extends Approve, BODY extends TitleBodyDto, RESPONSE extends ApproveDto,
        S extends ApprovedRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>>
        extends TitleBodyImagesServiceImpl<MODEL, BODY, RESPONSE, S, M>
        implements ApprovedService<MODEL, BODY, RESPONSE, S, M> {

    protected final ApprovedServiceCacheHandler<MODEL, BODY, RESPONSE> approvedServiceCacheHandler;
    private final RabbitMqApprovedSenderWrapper<RESPONSE> rabbitMqApprovedSenderWrapper;

    public ApprovedServiceImpl(S modelRepository, M modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, String modelName, List<String> allowedSortingFields, EntitiesUtils entitiesUtils, FileClient fileClient, ObjectMapper objectMapper, ApprovedServiceCacheHandler<MODEL, BODY, RESPONSE> approvedServiceCacheHandler, RabbitMqApprovedSenderWrapper<RESPONSE> rabbitMqApprovedSenderWrapper) {
        super(modelRepository, modelMapper, pageableUtils, userClient, modelName, allowedSortingFields, entitiesUtils, fileClient, objectMapper, approvedServiceCacheHandler);
        this.approvedServiceCacheHandler = approvedServiceCacheHandler;
        this.rabbitMqApprovedSenderWrapper = rabbitMqApprovedSenderWrapper;
    }

    @Override
    public Mono<ResponseWithUserDto<RESPONSE>> approveModel(Long id, String userId, boolean approved) {
        return
                approvedServiceCacheHandler.approveModelInvalidate.apply(
                        userClient.getUser("", userId)
                                .flatMap(authUser -> getModel(id)
                                        .flatMap(model -> {
                                                    if (model.isApproved() && approved) {
                                                        return Mono.error(new IllegalActionException(modelName + " with id " + id + " is already approved!"));
                                                    }
                                                    model.setApproved(approved);
                                                    return modelRepository.save(model).flatMap(m -> getModelGuardWithUser(authUser, m, !m.isApproved()));
                                                }

                                        ).doOnSuccess(r -> rabbitMqApprovedSenderWrapper.sendMessage(approved, r, authUser))
                                ), id, userId, approved);

    }

//    protected Mono<ResponseWithUserDto<RESPONSE>> approveModelWithCallback(Long id, String userId, boolean approved, BiFunction<ResponseWithUserDto<RESPONSE>, UserDto, Void> successCallback) {
//        return
//                approvedServiceCacheHandler.approveModelInvalidate.apply(
//                        userClient.getUser("", userId)
//                                .flatMap(authUser -> getModel(id)
//                                        .flatMap(model -> {
//                                                    if (model.isApproved() && approved) {
//                                                        return Mono.error(new IllegalActionException(modelName + " with id " + id + " is already approved!"));
//                                                    }
//                                                    model.setApproved(approved);
//                                                    return modelRepository.save(model).flatMap(m -> getModelGuardWithUser(authUser, m, !m.isApproved()));
//                                                }
//
//                                        ).doOnSuccess(r -> successCallback.apply(r, authUser))
//                                ), id, userId, approved);
//
//    }

    @Override
    public Flux<PageableResponse<RESPONSE>> getModelsApproved(PageableBody pageableBody, String userId) {
        return getModelsTitle(null, true, pageableBody, userId);
    }

    @Override
    public Flux<PageableResponse<RESPONSE>> getModelsApproved(String title, PageableBody pageableBody, String userId) {
        return getModelsTitle(title, true, pageableBody, userId);
    }

    @Override
    public Flux<PageableResponse<ResponseWithUserDto<RESPONSE>>> getModelsWithUser(String title, PageableBody pageableBody, String userId, boolean approved) {
        return getModelsTitle(title, approved, pageableBody, userId)
                .concatMap(this::getPageableWithUser);
    }

    public Flux<PageableResponse<RESPONSE>> getModelsTitle(String title, boolean approved, PageableBody pageableBody, String userId) {

        final String newTitle = title == null ? "" : title.trim();

        return
                protectRoute(approved, pageableBody, userId)
                        .flatMapMany(
                                pr ->
                                        approvedServiceCacheHandler.getModelsTitlePersist.apply(
                                                pageableUtils.createPageableResponse(
                                                        modelRepository.findAllByTitleContainingIgnoreCaseAndApproved(newTitle, approved, pr).map(modelMapper::fromModelToResponse),
                                                        modelRepository.countAllByTitleContainingIgnoreCaseAndApproved(newTitle, approved),
                                                        pr), newTitle, approved, pageableBody, userId)
                        );
    }


    @Override
    public Flux<PageableResponse<RESPONSE>> getAllModels(String title, PageableBody pageableBody, String userId) {
        final String newTitle = title == null ? "" : title.trim();
        return
                approvedServiceCacheHandler.getAllModelsTitlePersist.apply(
                        pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                                .then(pageableUtils.createPageRequest(pageableBody))
                                .flatMapMany(pr -> pageableUtils.createPageableResponse(
                                                modelRepository.findAllByTitleContainingIgnoreCase(newTitle, pr).map(modelMapper::fromModelToResponse),
                                                modelRepository.countAllByTitleContainingIgnoreCase(newTitle),
                                                pr
                                        )
                                ), newTitle, pageableBody, userId);
    }


    @Override
    public Mono<RESPONSE> getModelById(Long id, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser ->
                        approvedServiceCacheHandler.getModelByIdPersist.apply(
                                getModel(id)
                                        .flatMap(model -> getResponseGuard(authUser, model, !model.isApproved())),
                                authUser, id
                        )
                );
    }


    @Override
    public Mono<ResponseWithUserDto<RESPONSE>> getModelByIdWithUser(Long id, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser ->
                        approvedServiceCacheHandler.getModelByIdWithUserPersist.apply(
                                getModel(id)
                                        .flatMap(model -> getModelGuardWithUser(authUser, model, !model.isApproved())), authUser, id)
                );

    }


    protected Mono<PageRequest> protectRoute(boolean approved, PageableBody pageableBody, String userId) {
        return userClient.getUser("", userId).flatMap(
                        u -> {
                            if (!u.getRole().equals(Role.ROLE_ADMIN) && !approved) {
                                return Mono.error(new PrivateRouteException());
                            }
                            return Mono.just(u);
                        }
                )
                .then(pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields))
                .then(pageableUtils.createPageRequest(pageableBody));
    }

    @Override
    public Mono<ResponseWithUserLikesAndDislikes<RESPONSE>> getModelByIdWithUserLikesAndDislikes(Long id, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser ->
                        approvedServiceCacheHandler.getModelByIdWithUserLikesAndDislikesPersist.apply(
                                getModel(id)
                                        .flatMap(model -> getModelGuardWithLikesAndDislikes(authUser, model, !model.isApproved()))
                                , authUser, id));
    }


    @Override
    public Flux<PageableResponse<RESPONSE>> getModelsTrainer(String title, Long trainerId, PageableBody pageableBody, String userId, Boolean approved) {
        String newTitle = title == null ? "" : title.trim();
        return getModelsAuthor(trainerId, pageableBody, userId, pr -> (pageableUtils.createPageableResponse(
                (approved == null ? modelRepository.findAllByUserIdAndTitleContainingIgnoreCase(trainerId, newTitle, pr)
                        : modelRepository.findAllByUserIdAndTitleContainingIgnoreCaseAndApproved(trainerId, newTitle, approved, pr)
                ).map(modelMapper::fromModelToResponse),
                approved == null ? modelRepository.countAllByUserIdAndTitleContainingIgnoreCase(trainerId, newTitle)
                        : modelRepository.countAllByUserIdAndTitleContainingIgnoreCaseAndApproved(trainerId, newTitle, approved),
                pr
        )));
    }

    protected Flux<PageableResponse<RESPONSE>> getModelsAuthor(Long trainerId, PageableBody pageableBody, String userId,
                                                               Function<PageRequest, Flux<PageableResponse<RESPONSE>>> getResponse) {
        return userClient.existsTrainerOrAdmin("/exists", trainerId)
                .then(pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields))
                .then(pageableUtils.createPageRequest(pageableBody))
                .flatMapMany(pr -> userClient.getUser("", userId)
                        .flatMapMany(authUser -> privateRoute(true, authUser, trainerId))
                        .thenMany(getResponse.apply(pr)));
    }

    public Mono<Tuple2<RESPONSE, UserDto>> getModelByIdWithOwner(Long id, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> getModel(id)
                        .flatMap(model -> getResponseGuard(authUser, model, !model.isApproved())
                                .flatMap(response -> userClient.getUser("", model.getUserId().toString())
                                        .map(user -> Tuples.of(response, user))
                                )
                        )

                );


    }

    @Override
    public Mono<Pair<RESPONSE, Boolean>> updateModelWithImagesGetOriginalApproved(Flux<FilePart> images, Long id, BODY body, String userId, String clientId) {
        return
                approvedServiceCacheHandler.updateModelGetOriginalApprovedInvalidate.apply(
                        updateModelWithSuccessGeneral(id, userId, model -> {
                                    Boolean originalApproved = model.isApproved();
                                    return fileClient.deleteFiles(model.getImages())
                                            .then(uploadFiles(images, FileType.IMAGE, clientId)
                                                    .flatMap(fileUploadResponse ->

                                                            modelMapper.updateModelFromBody(body, model)
                                                                    .map(m -> {
                                                                        m.setImages(fileUploadResponse.getFiles());
                                                                        return m;
                                                                    })
                                                    )
                                            ).flatMap(modelRepository::save)
                                            .map(modelMapper::fromModelToResponse).map(r -> Pair.of(r, originalApproved));
                                }
                        ), id, body, userId);

    }

    @Override
    public Mono<Pair<RESPONSE, Boolean>> updateModelGetOriginalApproved(Long id, BODY body, String userId) {
        return
                approvedServiceCacheHandler.updateModelGetOriginalApprovedInvalidate.apply(
                        updateModelWithSuccessGeneral(id, userId, model -> modelMapper.updateModelFromBody(body, model).flatMap(modelRepository::save)
                                .map(modelMapper::fromModelToResponse).map(r -> Pair.of(r, model.isApproved()))), id, body, userId);
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
//    @SuperBuilder
    @AllArgsConstructor
    public static class ApprovedServiceCacheHandler<MODEL extends Approve, BODY extends TitleBodyDto, RESPONSE extends WithUserDto>
            extends TitleBodyImagesServiceImpl.TitleBodyImagesServiceCacheHandler<MODEL, BODY, RESPONSE> {
        Function4<Mono<ResponseWithUserDto<RESPONSE>>, Long, String, Boolean, Mono<ResponseWithUserDto<RESPONSE>>> approveModelInvalidate;
        Function5<Flux<PageableResponse<RESPONSE>>, String, Boolean, PageableBody, String, Flux<PageableResponse<RESPONSE>>> getModelsTitlePersist;
        Function4<Flux<PageableResponse<RESPONSE>>, String, PageableBody, String, Flux<PageableResponse<RESPONSE>>> getAllModelsTitlePersist;
        Function4<Mono<Pair<RESPONSE, Boolean>>, Long, BODY, String, Mono<Pair<RESPONSE, Boolean>>> updateModelGetOriginalApprovedInvalidate;

        public ApprovedServiceCacheHandler() {
            super();
            this.approveModelInvalidate = (model, id, userId, approved) -> model;
            this.getModelsTitlePersist = (models, title, approved, pageableBody, userId) -> models;
            this.getAllModelsTitlePersist = (models, title, pageableBody, userId) -> models;
            this.updateModelGetOriginalApprovedInvalidate = (model, id, body, userId) -> model;
        }

    }
}
