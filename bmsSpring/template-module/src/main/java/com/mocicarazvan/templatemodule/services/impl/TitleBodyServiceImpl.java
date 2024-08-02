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
import reactor.core.publisher.Mono;

import java.util.List;

public abstract class TitleBodyServiceImpl<MODEL extends TitleBody, BODY, RESPONSE extends WithUserDto,
        S extends TitleBodyRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>>
        extends ManyToOneUserServiceImpl<MODEL, BODY, RESPONSE, S, M>
        implements TitleBodyService<MODEL, BODY, RESPONSE, S, M> {


    protected final EntitiesUtils entitiesUtils;

    public TitleBodyServiceImpl(S modelRepository, M modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, String modelName, List<String> allowedSortingFields, EntitiesUtils entitiesUtils) {
        super(modelRepository, modelMapper, pageableUtils, userClient, modelName, allowedSortingFields);
        this.entitiesUtils = entitiesUtils;
    }

    @Override
    public Mono<RESPONSE> reactToModel(Long id, String type, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> getModel(id)
                        .flatMap(model -> entitiesUtils.setReaction(model, authUser, type)
                                .flatMap(modelRepository::save)
                                .map(modelMapper::fromModelToResponse)
                        )


                );
    }

    @Override
    public Mono<RESPONSE> createModel(BODY body, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> {
                    MODEL model = modelMapper.fromBodyToModel(body);
                    model.setUserId(authUser.getId());
                    if (model.getUserDislikes() == null)
                        model.setUserDislikes(List.of());
                    if (model.getUserLikes() == null)
                        model.setUserLikes(List.of());
                    return modelRepository.save(model)
                            .map(modelMapper::fromModelToResponse);
                });
    }

    @Override
    public Mono<ResponseWithUserLikesAndDislikes<RESPONSE>> getModelByIdWithUserLikesAndDislikes(Long id, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> getModel(id)
                        .flatMap(model -> getModelGuardWithLikesAndDislikes(authUser, model, true))
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


}
