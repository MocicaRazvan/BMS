package com.mocicarazvan.templatemodule.services.impl;


import com.mocicarazvan.templatemodule.clients.CountInParentClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithEntityCount;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import com.mocicarazvan.templatemodule.services.ManyToOneUserService;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Getter
public abstract class ManyToOneUserServiceImpl<MODEL extends ManyToOneUser, BODY, RESPONSE extends WithUserDto,
        S extends ManyToOneUserRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>,
        CR extends ManyToOneUserServiceImpl.ManyToOneUserServiceRedisCacheWrapper<MODEL, BODY, RESPONSE, S, M>
        >
        implements ManyToOneUserService<MODEL, BODY, RESPONSE, S, M> {
    protected static final String CACHE_KEY_PATH = "#this.modelName";

    protected final S modelRepository;
    protected final M modelMapper;
    protected final PageableUtilsCustom pageableUtils;
    protected final UserClient userClient;
    protected final String modelName;
    protected final List<String> allowedSortingFields;
    protected final CR self;
    protected final RabbitMqUpdateDeleteService<MODEL> rabbitMqUpdateDeleteService;


    protected ManyToOneUserServiceImpl(S modelRepository, M modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, String modelName, List<String> allowedSortingFields, CR self, RabbitMqUpdateDeleteService<MODEL> rabbitMqUpdateDeleteService) {
        this.modelRepository = modelRepository;
        this.modelMapper = modelMapper;
        this.pageableUtils = pageableUtils;
        this.userClient = userClient;
        this.modelName = modelName;
        this.allowedSortingFields = allowedSortingFields;
        this.self = self;
        this.rabbitMqUpdateDeleteService = rabbitMqUpdateDeleteService;
    }


    @Override
    public Flux<MonthlyEntityGroup<RESPONSE>> getModelGroupedByMonth(int month, String userId) {


        return

                userClient.getUser("", userId)
                        .flatMapMany(
                                userDto ->
                                        self.getModelGroupedByMonthBase(month, userDto));

    }


    @Override
    public Mono<PageableResponse<ResponseWithUserDto<RESPONSE>>> getPageableWithUser(PageableResponse<RESPONSE> pr) {
        return userClient.getUser("", String.valueOf(pr.getContent().getUserId()))
                .map(userDto -> ResponseWithUserDto.<RESPONSE>builder()
                        .model(pr.getContent())
                        .user(userDto)
                        .build())
                .map(ru -> PageableResponse.<ResponseWithUserDto<RESPONSE>>builder()
                        .content(ru)
                        .pageInfo(pr.getPageInfo())
                        .build());
    }

    protected Mono<PageableResponse<ResponseWithEntityCount<RESPONSE>>> toResponseWithCount(String userId, CountInParentClient client, PageableResponse<RESPONSE> pr) {
        return client.getCountInParent(pr.getContent().getId(), userId)
                .map(entityCount -> PageableResponse.<ResponseWithEntityCount<RESPONSE>>builder()
                        .content(ResponseWithEntityCount.of(pr.getContent(), entityCount))
                        .pageInfo(pr.getPageInfo())
                        .links(pr.getLinks())
                        .build());
    }


    @Override
    public Mono<RESPONSE> deleteModel(Long id, String userId) {


        return
                userClient.getUser("", userId)
                        .flatMap(authUser -> getModel(id)
                                .flatMap(model -> privateRoute(true, authUser, model.getUserId())
                                        .then(modelRepository.delete(model))
                                        .doOnSuccess(_ -> rabbitMqUpdateDeleteService.sendDeleteMessage(model))
                                        .then(Mono.fromCallable(() -> modelMapper.fromModelToResponse(model)))
                                )
                        );

    }


    @Override
    public Mono<RESPONSE> getModelById(Long id, String userId) {


        return

                userClient.getUser("", userId)
                        .flatMap(authUser -> self.getModel(id)
                                .flatMap(model -> getResponseGuard(authUser, model, true))
                        );

    }

    public Mono<RESPONSE> getResponseGuard(UserDto authUser, MODEL model, boolean guard) {
        return privateRoute(guard, authUser, model.getUserId())
                .thenReturn(modelMapper.fromModelToResponse(model));
    }

    @Override
    public Flux<PageableResponse<RESPONSE>> getAllModels(PageableBody pageableBody, String userId) {


        return
                pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                        .then(pageableUtils.createPageRequest(pageableBody))
                        .flatMapMany(pr -> pageableUtils.createPageableResponse(
                                        modelRepository.findAllBy(pr).map(modelMapper::fromModelToResponse),
                                        modelRepository.count(),
                                        pr
                                )

                        );
    }


    @Override
    public Mono<RESPONSE> updateModel(Long id, BODY body, String userId) {

        return
                updateModelWithSuccess(id, userId, model -> modelMapper.updateModelFromBody(body, model));
    }


    protected Mono<RESPONSE> updateModelWithSuccess(Long id, String userId, Function<MODEL, Mono<MODEL>> successCallback) {


        return updateModelWithSuccessGeneral(id, userId, model -> successCallback.apply(model).flatMap(modelRepository::save)
                .map(modelMapper::fromModelToResponse));
    }

    protected <G> Mono<G> updateModelWithSuccessGeneral(Long id, String userId, Function<MODEL, Mono<G>> successCallback) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> getModel(id)
                        .flatMap(model -> isNotAuthor(model, authUser)
                                .flatMap(isNotAuthor -> {
                                    if (isNotAuthor) {
                                        return Mono.error(new PrivateRouteException());
                                    } else {
                                        return successCallback.apply(model)
                                                .doOnSuccess(_ -> rabbitMqUpdateDeleteService.sendUpdateMessage(model));
                                    }
                                })
                        )
                );
    }


    @Override
    public Mono<ResponseWithUserDto<RESPONSE>> getModelByIdWithUser(Long id, String userId) {

        return userClient.getUser("", userId)
                .flatMap(authUser -> self.getModelByIdWithUserBase(authUser, id)
                );

    }

    @Override
    public Flux<ResponseWithUserDto<RESPONSE>> getModelsWithUser(List<Long> ids, String userId) {

        return
                userClient.getUser("", userId)
                        .flatMapMany(authUser ->
                                self.findAllById(ids)
                                        .flatMap(model -> getModelGuardWithUser(authUser, model, false))


                        );
    }


    public Mono<ResponseWithUserDto<RESPONSE>> getModelGuardWithUser(UserDto authUser, MODEL model, boolean guard) {


        return self.getModelGuardWithUserBase(authUser, model, guard);
    }


    public Mono<MODEL> getModel(Long id) {

        return
                modelRepository.findById(id)
                        .switchIfEmpty(Mono.error(new NotFoundEntity(modelName, id)));

    }

    public Mono<Boolean> isNotAuthor(MODEL model, UserDto authUser) {
        return Mono.just(
                !model.getUserId().equals(authUser.getId())
        );
    }

    public Mono<Void> privateRoute(boolean guard, UserDto authUser, Long ownerId) {

        return self.privateRouteBase(guard, authUser, ownerId);
    }

    @Override
    public Flux<PageableResponse<RESPONSE>> getModelsByIdInPageable(List<Long> ids, PageableBody pageableBody) {

        return
                pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                        .then(pageableUtils.createPageRequest(pageableBody))
                        .flatMapMany(pr -> pageableUtils.createPageableResponse(
                                        modelRepository.findAllByIdIn(ids, pr).map(modelMapper::fromModelToResponse),
                                        modelRepository.countAllByIdIn(ids),
                                        pr
                                )

                        );
    }

    @Override
    public Flux<RESPONSE> getModelsByIdIn(List<Long> ids) {

        return
                self.findAllById(ids).map(modelMapper::fromModelToResponse);
    }

    @Override
    public Mono<RESPONSE> createModel(BODY body, String userId) {

        return

                userClient.getUser("", userId)
                        .flatMap(authUser -> {
                                    MODEL model = modelMapper.fromBodyToModel(body);
                                    model.setUserId(authUser.getId());
                                    return modelRepository.save(model)
                                            .map(modelMapper::fromModelToResponse);
                                }
                        );
    }


    // todo NEVER PUT PRIVATE IN THIS FUCKING CLASS
    // todo never la update delete fol cache
    @Data
    public static class ManyToOneUserServiceRedisCacheWrapper<MODEL extends ManyToOneUser, BODY, RESPONSE extends WithUserDto,
            S extends ManyToOneUserRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>> {
        protected final S modelRepository;
        protected final M modelMapper;
        protected final String modelName;
        protected final UserClient userClient;

        public ManyToOneUserServiceRedisCacheWrapper(S modelRepository, M modelMapper, String modelName, UserClient userClient) {
            this.modelRepository = modelRepository;
            this.modelMapper = modelMapper;
            this.modelName = modelName;
            this.userClient = userClient;
        }

        public Flux<MonthlyEntityGroup<RESPONSE>> getModelGroupedByMonthBase(int month, UserDto userDto) {
            return Flux.defer(() -> {
                        if (!userDto.getRole().equals(Role.ROLE_ADMIN)) {
                            return Mono.error(new PrivateRouteException());
                        }
                        LocalDateTime now = LocalDateTime.now();
                        int year = now.getYear();
                        if (month == 1 && now.getMonthValue() == 1) {
                            year -= 1;
                        }
                        return modelRepository.findModelByMonth(month, year)
                                .map(m -> {
                                            YearMonth ym = YearMonth.from(m.getCreatedAt());
                                            return MonthlyEntityGroup.<RESPONSE>builder()
                                                    .month(ym.getMonthValue())
                                                    .year(ym.getYear())
                                                    .entity(modelMapper.fromModelToResponse(m))
                                                    .build();
                                        }
                                );
                    }
            );


        }


        public Mono<ResponseWithUserDto<RESPONSE>> getModelByIdWithUserBase(UserDto authUser, Long id) {

            return
                    getModel(id)
                            .flatMap(model -> getModelGuardWithUserBase(authUser, model, true)
                            )
                    ;

        }

        public Mono<MODEL> getModel(Long id) {
            return
                    modelRepository.findById(id)
                            .map(m -> {
                                log.info("Model " + m.toString());
                                return m;
                            })
                            .switchIfEmpty(Mono.error(new NotFoundEntity(modelName, id)));
        }

        public Flux<MODEL> findAllById(List<Long> ids) {
            return modelRepository.findAllById(ids);
        }

        /// not cache
        public Mono<ResponseWithUserDto<RESPONSE>> getModelGuardWithUserBase(UserDto authUser, MODEL model, boolean guard) {
            return privateRouteBase(guard, authUser, model.getUserId())
                    .then(userClient.getUser("", model.getUserId().toString())
                            .map(user ->
                                    ResponseWithUserDto.<RESPONSE>builder()
                                            .model(modelMapper.fromModelToResponse(model))
                                            .user(user)
                                            .build()
                            )

                    );
        }

        public Mono<Void> privateRouteBase(boolean guard, UserDto authUser, Long ownerId) {
            return userClient.hasPermissionToModifyEntity(authUser, ownerId)
                    .flatMap(perm -> {
                        if (guard && !perm) {
                            return Mono.error(new PrivateRouteException());
                        }
                        return Mono.empty();
                    });
        }

        public Mono<Boolean> isNotAuthor(MODEL model, UserDto authUser) {
            return Mono.just(
                    !model.getUserId().equals(authUser.getId())
            );
        }


    }


}
