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
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function3;
import org.jooq.lambda.function.Function4;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@RequiredArgsConstructor
public abstract class ManyToOneUserServiceImpl<MODEL extends ManyToOneUser, BODY, RESPONSE extends WithUserDto,
        S extends ManyToOneUserRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>>
        implements ManyToOneUserService<MODEL, BODY, RESPONSE, S, M> {

    protected final S modelRepository;
    protected final M modelMapper;
    protected final PageableUtilsCustom pageableUtils;
    protected final UserClient userClient;
    protected final String modelName;
    protected final List<String> allowedSortingFields;
    protected final ManyToOneUserServiceCacheHandler<MODEL, BODY, RESPONSE> manyToOneUserServiceCacheHandler;

    @Override
    public Flux<MonthlyEntityGroup<RESPONSE>> getModelGroupedByMonth(int month, String userId) {
        return

                userClient.getUser("", userId)
                        .flatMapMany(
                                userDto ->
                                        manyToOneUserServiceCacheHandler.getModelGroupedByMonthPersist.apply(
                                                Flux.defer(() -> {
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
                                                }), userDto, month)
                        );

    }

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
                manyToOneUserServiceCacheHandler.deleteModelInvalidate.apply(
                        userClient.getUser("", userId)
                                .flatMap(authUser -> getModel(id)
                                        .flatMap(model -> privateRoute(true, authUser, model.getUserId())
                                                .then(modelRepository.delete(model))
                                                .then(Mono.fromCallable(() -> modelMapper.fromModelToResponse(model)))
                                        )
                                ), id, userId);
    }


    @Override
    public Mono<RESPONSE> getModelById(Long id, String userId) {
        return

                userClient.getUser("", userId)
                        .flatMap(authUser -> manyToOneUserServiceCacheHandler.getModelByIdPersist.apply(getModel(id)
                                                .flatMap(model -> getResponseGuard(authUser, model, true)),
                                        authUser, id
                                )
                        );

    }

    public Mono<RESPONSE> getResponseGuard(UserDto authUser, MODEL model, boolean guard) {
        return privateRoute(guard, authUser, model.getUserId())
                .thenReturn(modelMapper.fromModelToResponse(model));
    }

    @Override
    public Flux<PageableResponse<RESPONSE>> getAllModels(PageableBody pageableBody, String userId) {
        return
                manyToOneUserServiceCacheHandler.getAllModelsPersist.apply(
                        pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                                .then(pageableUtils.createPageRequest(pageableBody))
                                .flatMapMany(pr -> pageableUtils.createPageableResponse(
                                                modelRepository.findAllBy(pr).map(modelMapper::fromModelToResponse),
                                                modelRepository.count(),
                                                pr
                                        )

                                ), pageableBody, userId);
    }


    @Override
    public Mono<RESPONSE> updateModel(Long id, BODY body, String userId) {
        return
                manyToOneUserServiceCacheHandler.updateModelInvalidate.apply(
                        updateModelWithSuccess(id, userId, model -> modelMapper.updateModelFromBody(body, model)), id, body, userId);
    }


    protected Mono<RESPONSE> updateModelWithSuccess(Long id, String userId, Function<MODEL, Mono<MODEL>> successCallback) {
//        return userClient.getUser("", userId)
//                .flatMap(authUser -> getModel(id)
//                        .flatMap(model -> isNotAuthor(model, authUser)
//                                .flatMap(isNotAuthor -> {
//                                    if (isNotAuthor) {
//                                        return Mono.error(new PrivateRouteException());
//                                    } else {
//                                        return successCallback.apply(model).flatMap(modelRepository::save)
//                                                .map(modelMapper::fromModelToResponse);
//                                    }
//                                })
//                        )
//                );

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
                                        return successCallback.apply(model);
                                    }
                                })
                        )
                );
    }


    @Override
    public Mono<ResponseWithUserDto<RESPONSE>> getModelByIdWithUser(Long id, String userId) {
        return
                userClient.getUser("", userId)
                        .flatMap(authUser -> manyToOneUserServiceCacheHandler.getModelByIdWithUserPersist.apply(getModel(id)
                                                .flatMap(model -> getModelGuardWithUser(authUser, model, true)
                                                )
                                        , authUser, id
                                )
                        );

    }

    @Override
    public Flux<ResponseWithUserDto<RESPONSE>> getModelsWithUser(List<Long> ids, String userId) {
        return
                userClient.getUser("", userId)
                        .flatMapMany(authUser ->
                                manyToOneUserServiceCacheHandler.getModelsWithUserPersist.apply(
                                        modelRepository.findAllById(ids)
                                                .flatMap(model -> getModelGuardWithUser(authUser, model, false))
                                        , authUser, ids
                                )
                        );
    }


    public Mono<ResponseWithUserDto<RESPONSE>> getModelGuardWithUser(UserDto authUser, MODEL model, boolean guard) {
        return privateRoute(guard, authUser, model.getUserId())
                .then(userClient.getUser("", model.getUserId().toString())
                        .map(user ->
                                ResponseWithUserDto.<RESPONSE>builder()
                                        .model(modelMapper.fromModelToResponse(model))
                                        .user(user)
                                        .build()
                        )

                );
    }


    public Mono<MODEL> getModel(Long id) {
        return
                manyToOneUserServiceCacheHandler.getByIdInternalPersist.apply(
                        modelRepository.findById(id)
                                .switchIfEmpty(Mono.error(new NotFoundEntity(modelName, id))), id);
    }

    public Mono<Boolean> isNotAuthor(MODEL model, UserDto authUser) {
        return Mono.just(
                !model.getUserId().equals(authUser.getId())
        );
    }

    public Mono<Void> privateRoute(boolean guard, UserDto authUser, Long ownerId) {
        return userClient.hasPermissionToModifyEntity(authUser, ownerId)
                .flatMap(perm -> {
                    if (guard && !perm) {
                        return Mono.error(new PrivateRouteException());
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Flux<PageableResponse<RESPONSE>> getModelsByIdInPageable(List<Long> ids, PageableBody pageableBody) {
        return
                manyToOneUserServiceCacheHandler.getModelsByIdInPageablePersist.apply(
                        pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                                .then(pageableUtils.createPageRequest(pageableBody))
                                .flatMapMany(pr -> pageableUtils.createPageableResponse(
                                                modelRepository.findAllByIdIn(ids, pr).map(modelMapper::fromModelToResponse),
                                                modelRepository.countAllByIdIn(ids),
                                                pr
                                        )

                                ), ids, pageableBody);
    }

    @Override
    public Flux<RESPONSE> getModelsByIdIn(List<Long> ids) {
        return
                manyToOneUserServiceCacheHandler.getModelsByIdInPersist.apply(
                        modelRepository.findAllByIdIn(ids).map(modelMapper::fromModelToResponse)
                        , ids
                );
    }

    @Override
    public Mono<RESPONSE> createModel(BODY body, String userId) {
        return
                manyToOneUserServiceCacheHandler.createModelInvalidate.apply(
                        userClient.getUser("", userId)
                                .flatMap(authUser -> {
                                            MODEL model = modelMapper.fromBodyToModel(body);
                                            model.setUserId(authUser.getId());
                                            return modelRepository.save(model)
                                                    .map(modelMapper::fromModelToResponse);
                                        }
                                ), body, userId);
    }

    @Data
//    @SuperBuilder
    @AllArgsConstructor
    public static class ManyToOneUserServiceCacheHandler<MODEL extends ManyToOneUser, BODY, RESPONSE extends WithUserDto> {

        Function3<Flux<MonthlyEntityGroup<RESPONSE>>, UserDto, Integer, Flux<MonthlyEntityGroup<RESPONSE>>> getModelGroupedByMonthPersist;
        Function3<Mono<RESPONSE>, Long, String, Mono<RESPONSE>> deleteModelInvalidate;
        Function3<Mono<RESPONSE>, UserDto, Long, Mono<RESPONSE>> getModelByIdPersist;
        Function2<Mono<MODEL>, Long, Mono<MODEL>> getByIdInternalPersist;
        Function3<Flux<PageableResponse<RESPONSE>>, PageableBody, String, Flux<PageableResponse<RESPONSE>>> getAllModelsPersist;
        Function4<Mono<RESPONSE>, Long, BODY, String, Mono<RESPONSE>> updateModelInvalidate;
        Function3<Mono<ResponseWithUserDto<RESPONSE>>, UserDto, Long, Mono<ResponseWithUserDto<RESPONSE>>> getModelByIdWithUserPersist;
        Function3<Flux<ResponseWithUserDto<RESPONSE>>, UserDto, List<Long>, Flux<ResponseWithUserDto<RESPONSE>>> getModelsWithUserPersist;
        Function3<Flux<PageableResponse<RESPONSE>>, List<Long>, PageableBody, Flux<PageableResponse<RESPONSE>>> getModelsByIdInPageablePersist;
        Function2<Flux<RESPONSE>, List<Long>, Flux<RESPONSE>> getModelsByIdInPersist;
        Function3<Mono<RESPONSE>, BODY, String, Mono<RESPONSE>> createModelInvalidate;

        public ManyToOneUserServiceCacheHandler() {
            this.getModelGroupedByMonthPersist = (flux, authUser, month) -> flux;
            this.deleteModelInvalidate = (mono, id, userId) -> mono;
            this.getModelByIdPersist = (mono, authUser, id) -> mono;
            this.getAllModelsPersist = (flux, pageableBody, userId) -> flux;
            this.updateModelInvalidate = (mono, id, body, userId) -> mono;
            this.getModelByIdWithUserPersist = (mono, authUser, id) -> mono;
            this.getModelsWithUserPersist = (flux, authUser, ids) -> flux;
            this.getModelsByIdInPageablePersist = (flux, ids, pageableBody) -> flux;
            this.getModelsByIdInPersist = (flux, ids) -> flux;
            this.createModelInvalidate = (mono, body, userId) -> mono;
            this.getByIdInternalPersist = (mono, id) -> mono;
        }
    }


}
