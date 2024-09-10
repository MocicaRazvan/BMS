package com.mocicarazvan.templatemodule.services.impl;


import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserLikesAndDislikes;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.TitleBody;
import com.mocicarazvan.templatemodule.repositories.TitleBodyRepository;
import com.mocicarazvan.templatemodule.services.TitleBodyService;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.jooq.lambda.function.Function3;
import org.jooq.lambda.function.Function4;
import reactor.core.publisher.Mono;

import java.util.List;

public abstract class TitleBodyServiceImpl<MODEL extends TitleBody, BODY, RESPONSE extends WithUserDto,
        S extends TitleBodyRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>>
        extends ManyToOneUserServiceImpl<MODEL, BODY, RESPONSE, S, M>
        implements TitleBodyService<MODEL, BODY, RESPONSE, S, M> {


    protected final EntitiesUtils entitiesUtils;
    protected final TitleBodyServiceCacheHandler<MODEL, BODY, RESPONSE> titleBodyServiceCacheHandler;

    public TitleBodyServiceImpl(S modelRepository, M modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, String modelName, List<String> allowedSortingFields, EntitiesUtils entitiesUtils, TitleBodyServiceCacheHandler<MODEL, BODY, RESPONSE> titleBodyServiceCacheHandler) {
        super(modelRepository, modelMapper, pageableUtils, userClient, modelName, allowedSortingFields, titleBodyServiceCacheHandler);
        this.entitiesUtils = entitiesUtils;
        this.titleBodyServiceCacheHandler = titleBodyServiceCacheHandler;
    }

    @Override
    public Mono<RESPONSE> reactToModel(Long id, String type, String userId) {
        return
                titleBodyServiceCacheHandler.reactToModelInvalidate.apply(
                        userClient.getUser("", userId)
                                .flatMap(authUser -> getModel(id)
                                        .flatMap(model -> entitiesUtils.setReaction(model, authUser, type)
                                                .flatMap(modelRepository::save)
                                                .map(modelMapper::fromModelToResponse)
                                        )

                                ), id, type, userId);

    }

    @Override
    public Mono<RESPONSE> createModel(BODY body, String userId) {
        return
                titleBodyServiceCacheHandler.createModelInvalidate.apply(
                        userClient.getUser("", userId)
                                .flatMap(authUser -> {
                                    MODEL model = modelMapper.fromBodyToModel(body);
                                    model.setUserId(authUser.getId());
                                    if (model.getUserDislikes() == null)
                                        model.setUserDislikes(List.of());
                                    if (model.getUserLikes() == null)
                                        model.setUserLikes(List.of());
                                    return modelRepository.save(model)
                                            .map(modelMapper::fromModelToResponse);
                                }), body, userId);
    }

    @Override
    public Mono<ResponseWithUserLikesAndDislikes<RESPONSE>> getModelByIdWithUserLikesAndDislikes(Long id, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser ->
                        titleBodyServiceCacheHandler.getModelByIdWithUserLikesAndDislikesPersist.apply(
                                getModel(id)
                                        .flatMap(model -> getModelGuardWithLikesAndDislikes(authUser, model, true))
                                , authUser, id
                        )
                );
    }

    public Mono<ResponseWithUserLikesAndDislikes<RESPONSE>> getModelGuardWithLikesAndDislikes(UserDto authUser, MODEL model, boolean guard) {
        return getModelGuardWithUser(authUser, model, guard)
                .zipWith(userClient.getUsersByIdIn("/byIds", model.getUserLikes()).collectList())
                .zipWith(userClient.getUsersByIdIn("/byIds", model.getUserDislikes()).collectList())
                .map(tuple -> {
                    ResponseWithUserDto<RESPONSE> responseWithUserDto = tuple.getT1().getT1();
                    List<UserDto> userLikes = tuple.getT1().getT2();
                    List<UserDto> userDislikes = tuple.getT2();
                    return ResponseWithUserLikesAndDislikes.<RESPONSE>builder()
                            .model(responseWithUserDto.getModel())
                            .user(responseWithUserDto.getUser())
                            .userLikes(userLikes)
                            .userDislikes(userDislikes)
                            .build();
                });
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
//    @SuperBuilder
    @AllArgsConstructor
    public static class TitleBodyServiceCacheHandler<MODEL extends TitleBody, BODY, RESPONSE extends WithUserDto>
            extends ManyToOneUserServiceImpl.ManyToOneUserServiceCacheHandler<MODEL, BODY, RESPONSE> {

        Function4<Mono<RESPONSE>, Long, String, String, Mono<RESPONSE>> reactToModelInvalidate;
        Function3<Mono<ResponseWithUserLikesAndDislikes<RESPONSE>>, UserDto, Long, Mono<ResponseWithUserLikesAndDislikes<RESPONSE>>> getModelByIdWithUserLikesAndDislikesPersist;

        public TitleBodyServiceCacheHandler() {
            super();
            this.reactToModelInvalidate = (mono, id, type, userId) -> mono;
            this.getModelByIdWithUserLikesAndDislikesPersist = (mono, authUser, id) -> mono;
        }
    }


}
