package com.mocicarazvan.templatemodule.services.impl;

import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
import com.mocicarazvan.templatemodule.dtos.generic.TitleBodyDto;
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
import com.mocicarazvan.templatemodule.services.RabbitMqApprovedSender;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.function.Function3;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Getter
public abstract class ApprovedServiceImpl<MODEL extends Approve, BODY extends TitleBodyDto, RESPONSE extends ApproveDto,
        S extends ApprovedRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>,
        CR extends ApprovedServiceImpl.ApprovedServiceRedisCacheWrapper<MODEL, BODY, RESPONSE, S, M>
        >
        extends TitleBodyImagesServiceImpl<MODEL, BODY, RESPONSE, S, M, CR>
        implements ApprovedService<MODEL, BODY, RESPONSE, S, M> {

    protected final RabbitMqApprovedSender<RESPONSE> rabbitMqApprovedSender;

    public ApprovedServiceImpl(S modelRepository, M modelMapper, PageableUtilsCustom pageableUtils,
                               UserClient userClient, String modelName, List<String> allowedSortingFields,
                               EntitiesUtils entitiesUtils, FileClient fileClient,
                               RabbitMqApprovedSender<RESPONSE> rabbitMqApprovedSender,
                               CR self, RabbitMqUpdateDeleteService<MODEL> rabbitMqUpdateDeleteService) {
        super(modelRepository, modelMapper, pageableUtils, userClient, modelName,
                allowedSortingFields, entitiesUtils, fileClient, self, rabbitMqUpdateDeleteService);
        this.rabbitMqApprovedSender = rabbitMqApprovedSender;
    }


    @Override
    public Mono<ResponseWithUserDto<RESPONSE>> approveModel(Long id, String userId, boolean approved) {

        return
                userClient.getUser("", userId)
                        .flatMap(authUser -> getModel(id)
                                        .flatMap(model -> {
                                                    if (model.isApproved() && approved) {
                                                        return Mono.error(new IllegalActionException(modelName + " with id " + id + " is already approved!"));
                                                    }
                                                    model.setApproved(approved);
                                                    return modelRepository.save(model)
                                                            .flatMap(m -> getModelGuardWithUser(authUser, m, !m.isApproved()))
                                                            .flatMap(r -> self.updateDeleteInvalidate(Pair.of(r.getModel(), true))
//                                                            .flatMap(_ -> Mono.fromRunnable(() -> rabbitMqApprovedSender.sendMessage(approved, r, authUser)))
                                                                            .thenReturn(r)
                                                            );
                                                }

                                        )
                                        .doOnSuccess(r -> rabbitMqApprovedSender.sendMessage(approved, r, authUser))
                        );

    }


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
                .flatMapSequential(this::getPageableWithUser);
    }

    public Flux<PageableResponse<RESPONSE>> getModelsTitle(String title, boolean approved, PageableBody pageableBody, String userId) {

        final String newTitle = title == null ? "" : title.trim();


        return
                self.protectRoute(approved, pageableBody, userId, allowedSortingFields)
                        .flatMapMany(
                                pr ->
                                        self.getModelsTitleBase(approved, pr, newTitle)
                        );
    }

    protected Mono<PageRequest> protectRoute(boolean approved, PageableBody pageableBody, String userId) {
        return self.protectRoute(approved, pageableBody, userId, allowedSortingFields);
    }


    @Override
    public Flux<PageableResponse<RESPONSE>> getAllModels(String title, PageableBody pageableBody, String userId) {
        final String newTitle = title == null ? "" : title.trim();

        return
                pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                        .then(pageableUtils.createPageRequest(pageableBody))
                        .flatMapMany(pr -> pageableUtils.createPageableResponse(
                                        modelRepository.findAllByTitleContainingIgnoreCase(newTitle, pr).map(modelMapper::fromModelToResponse),
                                        modelRepository.countAllByTitleContainingIgnoreCase(newTitle),
                                        pr
                                )
                        );
    }


    @Override
    public Mono<RESPONSE> getModelById(Long id, String userId) {

        return userClient.getUser("", userId)
                .flatMap(authUser ->
                        self.getModel(id)
                                .flatMap(model -> getResponseGuard(authUser, model, !model.isApproved()))

                );
    }


    @Override
    public Mono<ResponseWithUserDto<RESPONSE>> getModelByIdWithUser(Long id, String userId) {

        return userClient.getUser("", userId)
                .flatMap(authUser ->
                        self.getModelByIdWithUserBase(authUser, id)
                );

    }


    @Override
    public Mono<ResponseWithUserLikesAndDislikes<RESPONSE>> getModelByIdWithUserLikesAndDislikes(Long id, String userId) {

        return userClient.getUser("", userId)
                .flatMap(authUser ->
                        self.getModelByIdWithUserLikesAndDislikesBase(id, authUser)
                );
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


        return self.getModelsAuthorBase(trainerId, pageableBody, userId, getResponse, allowedSortingFields);
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
                updateModelWithImagesGetOriginalApproved(images, id, body, userId, clientId, (_, _, m) -> modelRepository.save(m));

    }

    public Mono<Pair<RESPONSE, Boolean>> updateModelWithImagesGetOriginalApproved(Flux<FilePart> images, Long id, BODY body, String userId, String clientId,
                                                                                  Function3<BODY, String, MODEL, Mono<MODEL>> callback
    ) {

        return
                updateModelWithSuccessGeneral(id, userId, model -> {
                    Boolean originalApproved = model.isApproved();
                    String origTitle = model.getTitle();
                    return fileClient.deleteFiles(model.getImages())
                            .then(uploadFiles(images, FileType.IMAGE, clientId)
                                    .flatMap(fileUploadResponse ->

                                            modelMapper.updateModelFromBody(body, model)
                                                    .map(m -> {
                                                        m.setImages(fileUploadResponse.getFiles());
                                                        return m;
                                                    })
                                    )
                            ).flatMap(m -> callback.apply(body, origTitle, m))
                            .map(modelMapper::fromModelToResponse)

                            .map(r -> Pair.of(r, originalApproved));
                }).flatMap(self::updateDeleteInvalidate);

    }


    @Override
    public Mono<RESPONSE> createModel(Flux<FilePart> images, BODY body, String userId, String clientId) {
        return super.createModel(images, body, userId, clientId).flatMap(self::createInvalidate)
                .map(Pair::getFirst);
    }


    @Override
    public Mono<RESPONSE> createModel(BODY body, String userId) {
        return super.createModel(body, userId).flatMap(self::createInvalidate)
                .map(Pair::getFirst);
    }

    @Override
    public Mono<Pair<RESPONSE, Boolean>> deleteModelGetOriginalApproved(Long id, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> getModel(id)
                        .flatMap(model ->
                                {
                                    Boolean originalApproved = model.isApproved();
                                    return privateRoute(true, authUser, model.getUserId())
                                            .thenReturn(model)
                                            .flatMap(m -> fileClient.deleteFiles(m.getImages()))
                                            .then(Mono.defer(() -> modelRepository.delete(model)))
                                            .then(Mono.fromCallable(() -> modelMapper.fromModelToResponse(model)))
                                            .doOnSuccess(_ -> rabbitMqUpdateDeleteService.sendDeleteMessage(model))
                                            .map(r -> Pair.of(r, originalApproved))
                                            .flatMap(self::updateDeleteInvalidate);
                                }
                        )
                );
    }

    @Override
    public Mono<Pair<RESPONSE, Boolean>> updateModelGetOriginalApproved(Long id, BODY body, String userId) {

        return
                updateModelWithSuccessGeneral(id, userId, model -> modelMapper.updateModelFromBody(body, model).flatMap(modelRepository::save)
                        .map(modelMapper::fromModelToResponse).map(r -> Pair.of(r, model.isApproved())))
                        .flatMap(self::updateDeleteInvalidate);
    }

    @Getter
    public static class ApprovedServiceRedisCacheWrapper<MODEL extends Approve, BODY extends TitleBodyDto, RESPONSE extends ApproveDto,
            S extends ApprovedRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>>
            extends TitleBodyImagesServiceImpl.TitleBodyImagesServiceRedisCacheWrapper<MODEL, BODY, RESPONSE, S, M> {
        protected final PageableUtilsCustom pageableUtils;

        public ApprovedServiceRedisCacheWrapper(S modelRepository, M modelMapper, String modelName,
                                                PageableUtilsCustom pageableUtils, UserClient userClient) {
            super(modelRepository, modelMapper, modelName, userClient);
            this.pageableUtils = pageableUtils;
        }

        @Override
        public Mono<ResponseWithUserDto<RESPONSE>> getModelByIdWithUserBase(UserDto authUser, Long id) {

            return
                    getModel(id)
                            .flatMap(model -> getModelGuardWithUserBase(authUser, model, !model.isApproved())
                            )
                    ;

        }

        @Override
        public Mono<ResponseWithUserLikesAndDislikes<RESPONSE>> getModelByIdWithUserLikesAndDislikesBase(Long id, UserDto authUser) {

            return
                    getModel(id)
                            .flatMap(model -> getModelGuardWithLikesAndDislikesBase(authUser, model, !model.isApproved())
                            );
        }

        public Flux<PageableResponse<RESPONSE>> getModelsTitleBase(boolean approved, PageRequest pr, String newTitle) {
            return pageableUtils.createPageableResponse(
                    modelRepository.findAllByTitleContainingIgnoreCaseAndApproved(newTitle, approved, pr).map(modelMapper::fromModelToResponse),
                    modelRepository.countAllByTitleContainingIgnoreCaseAndApproved(newTitle, approved),
                    pr);
        }

        // no cache
        public Mono<PageRequest> protectRoute(boolean approved, PageableBody pageableBody, String userId, List<String> allowedSortingFields) {
            return
                    pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                            .then(Mono.defer(() -> userClient.getUser("", userId).flatMap(
                                    u -> {
                                        if (!u.getRole().equals(Role.ROLE_ADMIN) && !approved) {
                                            return Mono.error(new PrivateRouteException());
                                        }
                                        return Mono.just(u);
                                    }
                            )))
                            .flatMap(_ -> pageableUtils.createPageRequest(pageableBody));
        }

        public Flux<PageableResponse<RESPONSE>> getModelsAuthorBase(Long trainerId, PageableBody pageableBody, String userId,
                                                                    Function<PageRequest, Flux<PageableResponse<RESPONSE>>> getResponse,
                                                                    List<String> allowedSortingFields
        ) {
            return
                    pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                            .then(Mono.defer(() -> userClient.existsTrainerOrAdmin("/exists", trainerId)))
                            .then(Mono.defer(() -> pageableUtils.createPageRequest(pageableBody)))
                            .flatMapMany(pr -> userClient.getUser("", userId)
                                    .flatMapMany(authUser -> privateRouteBase(true, authUser, trainerId))
                                    .thenMany(Flux.defer(() -> getResponse.apply(pr))));
        }

        public Mono<Pair<RESPONSE, Boolean>> createInvalidate(RESPONSE r) {
            return Mono.just(Pair.of(r, Boolean.FALSE));
        }

        public Mono<Pair<RESPONSE, Boolean>> updateDeleteInvalidate(Pair<RESPONSE, Boolean> p) {
            return Mono.just(p);
        }
    }


}
