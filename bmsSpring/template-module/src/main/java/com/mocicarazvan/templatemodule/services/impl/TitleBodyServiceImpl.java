package com.mocicarazvan.templatemodule.services.impl;


import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserLikesAndDislikes;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.TitleBody;
import com.mocicarazvan.templatemodule.repositories.TitleBodyRepository;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.TitleBodyService;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Getter;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public abstract class TitleBodyServiceImpl<MODEL extends TitleBody, BODY, RESPONSE extends WithUserDto,
        S extends TitleBodyRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>,
        CR extends TitleBodyServiceImpl.TitleBodyServiceRedisCacheWrapper<MODEL, BODY, RESPONSE, S, M>
        >
        extends ManyToOneUserServiceImpl<MODEL, BODY, RESPONSE, S, M, CR>
        implements TitleBodyService<MODEL, BODY, RESPONSE, S, M> {


    protected final EntitiesUtils entitiesUtils;


    public TitleBodyServiceImpl(S modelRepository, M modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, String modelName, List<String> allowedSortingFields,
                                EntitiesUtils entitiesUtils, CR titleBodyServiceRedisCacheWrapper, RabbitMqUpdateDeleteService<MODEL> rabbitMqUpdateDeleteService) {
        super(modelRepository, modelMapper, pageableUtils, userClient, modelName, allowedSortingFields, titleBodyServiceRedisCacheWrapper, rabbitMqUpdateDeleteService);
        this.entitiesUtils = entitiesUtils;
    }


    @Override
    public Mono<RESPONSE> reactToModel(Long id, String type, String userId) {

        return

                userClient.getUser("", userId)
                        .flatMap(authUser -> getModel(id)
                                .flatMap(model -> entitiesUtils.setReaction(model, authUser, type)
                                        .flatMap(modelRepository::save)
                                        .map(modelMapper::fromModelToResponse)
                                )

                        ).flatMap(self::reactToModelInvalidate);

    }

    @Override
    public Mono<RESPONSE> createModel(BODY body, String userId) {

        return
                userClient.getUser("", userId)
                        .flatMap(authUser -> {
                            MODEL model = modelMapper.fromBodyToModel(body);
                            model.setUserId(authUser.getId());
                            model.setCreatedAt(LocalDateTime.now());
                            model.setUpdatedAt(LocalDateTime.now());
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
                .flatMap(authUser ->
                        self.getModelByIdWithUserLikesAndDislikesBase(id, authUser)


                );
    }

    public Mono<ResponseWithUserLikesAndDislikes<RESPONSE>> getModelGuardWithLikesAndDislikes(UserDto authUser, MODEL model, boolean guard) {

        return self.getModelGuardWithLikesAndDislikesBase(authUser, model, guard);
    }


    @Getter
    public static class TitleBodyServiceRedisCacheWrapper<MODEL extends TitleBody, BODY, RESPONSE extends WithUserDto,
            S extends TitleBodyRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>> extends ManyToOneUserServiceRedisCacheWrapper<MODEL, BODY, RESPONSE, S, M> {
        public TitleBodyServiceRedisCacheWrapper(S modelRepository, M modelMapper, String modelName, UserClient userClient) {
            super(modelRepository, modelMapper, modelName, userClient);
        }

        public Mono<ResponseWithUserLikesAndDislikes<RESPONSE>> getModelByIdWithUserLikesAndDislikesBase(Long id, UserDto authUser) {
            return
                    getModel(id)
                            .flatMap(model -> getModelGuardWithLikesAndDislikesBase(authUser, model, true));
        }

        public Mono<RESPONSE> reactToModelInvalidate(RESPONSE r) {
            return Mono.just(r);
        }


        public Mono<ResponseWithUserLikesAndDislikes<RESPONSE>> getModelGuardWithLikesAndDislikesBase(UserDto authUser, MODEL model, boolean guard) {
            return getModelGuardWithUserBase(authUser, model, guard)
                    .zipWhen(_ ->
                            Mono.zip(
                                    userClient.getUsersByIdIn("/byIds", model.getUserLikes()).collectList(),
                                    userClient.getUsersByIdIn("/byIds", model.getUserDislikes()).collectList()
                            )
                    )
                    .map(tuple -> ResponseWithUserLikesAndDislikes.<RESPONSE>builder()
                            .model(tuple.getT1().getModel())
                            .user(tuple.getT1().getUser())
                            .userLikes(tuple.getT2().getT1())
                            .userDislikes(tuple.getT2().getT2())
                            .build());
        }

    }


}
