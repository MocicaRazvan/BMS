package com.mocicarazvan.ingredientservice.services.impl;

import com.mocicarazvan.ingredientservice.dtos.IngredientResponse;
import com.mocicarazvan.ingredientservice.dtos.NutritionalFactBody;
import com.mocicarazvan.ingredientservice.dtos.NutritionalFactResponse;
import com.mocicarazvan.ingredientservice.mappers.NutritionalFactMapper;
import com.mocicarazvan.ingredientservice.models.NutritionalFact;
import com.mocicarazvan.ingredientservice.repositories.NutritionalFactRepository;
import com.mocicarazvan.ingredientservice.services.IngredientService;
import com.mocicarazvan.ingredientservice.services.NutritionalFactService;
import com.mocicarazvan.templatemodule.adapters.CacheChildFilteredToHandlerAdapter;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheChildFilterKey;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.generic.IdGenerateDto;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.models.IdGenerated;
import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function3;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class NutritionalFactServiceImpl extends
        ManyToOneUserServiceImpl<
                NutritionalFact, NutritionalFactBody, NutritionalFactResponse, NutritionalFactRepository, NutritionalFactMapper>
        implements NutritionalFactService {


    private final NutritionalFactServiceCacheHandler nutritionalFactServiceCacheHandler;

    public NutritionalFactServiceImpl(NutritionalFactRepository modelRepository, NutritionalFactMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, NutritionalFactServiceCacheHandler nutritionalFactServiceCacheHandler, IngredientService ingredientService) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "nutritionalFact", List.of("createdAt", "updatedAt", "id", "fat", "saturated_fat",
                "carbohydrates", "sugar", "protein", "salt", "unit", "ingredient_id"
        ), nutritionalFactServiceCacheHandler);
        this.nutritionalFactServiceCacheHandler = nutritionalFactServiceCacheHandler;
        this.ingredientService = ingredientService;
    }

    private final IngredientService ingredientService;

    @Override
    public Mono<NutritionalFactResponse> findByIngredientIdUserId(Long ingredientId, String userId) {
        return
                ingredientService.getModelById(ingredientId, userId)
                        .flatMap(i ->
                                nutritionalFactServiceCacheHandler.getFindByIngredientIdUserIdPersist().apply(
                                        modelRepository.findByIngredientId(i.getId()).map(modelMapper::fromModelToResponse)
                                        , ingredientId, userId
                                )
                        )
                ;
    }

    @Override
    public Mono<NutritionalFactResponse> findByIngredientId(Long ingredientId) {
        return getModelByIngredientId(ingredientId)
                .map(modelMapper::fromModelToResponse);
    }

    private Mono<NutritionalFact> getModelByIngredientId(Long ingredientId) {
        return
                nutritionalFactServiceCacheHandler.getModelByIngredientPersist.apply(
                                modelRepository.findByIngredientId(ingredientId), ingredientId)
                        .switchIfEmpty(Mono.error(new NotFoundEntity("NutritionalFact for ingredient id ", ingredientId)));
    }

    @Override
    public Mono<NutritionalFactResponse> createModel(NutritionalFactBody modelBody, Long referenceId, String userId) {
        return
                nutritionalFactServiceCacheHandler.getCreateModelInvalidate().apply(
                        userClient.getUser("", userId)
                                .flatMap(authUser -> {
                                    NutritionalFact model = modelMapper.fromBodyToModel(modelBody);
                                    model.setUserId(authUser.getId());
                                    model.setIngredientId(referenceId);
                                    return modelRepository.save(model)
                                            .map(modelMapper::fromModelToResponse);
                                }), modelBody, userId);
    }

    @Override
    public Mono<NutritionalFactResponse> updateModelByIngredient(Long ingredientId, NutritionalFactBody modelBody, String userId) {
        return
                nutritionalFactServiceCacheHandler.getUpdateModelInvalidate().apply(
                        userClient.getUser("", userId)
                                .flatMap(authUser -> getModelByIngredientId(ingredientId)
                                        .flatMap(model -> isNotAuthor(model, authUser)
                                                .filter(Boolean.FALSE::equals)
                                                .switchIfEmpty(Mono.error(new PrivateRouteException()))
                                                .flatMap(_ -> modelMapper.updateModelFromBody(modelBody, model)
                                                        .flatMap(modelRepository::save)
                                                        .map(modelMapper::fromModelToResponse))
                                        )
                                ), ingredientId, modelBody, userId);
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    @Component
    public static class NutritionalFactServiceCacheHandler
            extends ManyToOneUserServiceImpl.ManyToOneUserServiceCacheHandler<NutritionalFact, NutritionalFactBody, NutritionalFactResponse> {
        private final FilteredListCaffeineCacheChildFilterKey<NutritionalFactResponse> cacheFilter;
        private final NutritionalFactMapper modelMapper;


        Function3<Mono<NutritionalFactResponse>, Long, String, Mono<NutritionalFactResponse>> findByIngredientIdUserIdPersist;
        Function2<Mono<NutritionalFact>, Long, Mono<NutritionalFact>> getModelByIngredientPersist;
        Function2<Mono<IngredientResponse>, Long, Mono<IngredientResponse>> updateByIngredientInvalidate;
        Function2<Mono<IngredientResponse>, Long, Mono<IngredientResponse>> deleteByIngredientInvalidate;


        public NutritionalFactServiceCacheHandler(FilteredListCaffeineCacheChildFilterKey<NutritionalFactResponse> cacheFilter, NutritionalFactMapper modelMapper) {
            super();
            this.cacheFilter = cacheFilter;
            this.modelMapper = modelMapper;
            CacheChildFilteredToHandlerAdapter.convertToManyUserHandler(
                    cacheFilter, this,
                    NutritionalFactResponse::getIngredientId,
                    modelMapper::fromModelToResponse
            );

            this.findByIngredientIdUserIdPersist = (mono, ingredientId, userId) ->
                    cacheFilter.getExtraUniqueMonoCacheForMasterIndependentOfRouteType(
                            EntitiesUtils.getListOfNotNullObjects(ingredientId, userId),
                            "findByIngredientIdUserIdPersist" + ingredientId + "_" + userId,
                            IdGenerateDto::getId,
                            ingredientId,
                            mono
                    );

            this.getModelByIngredientPersist = (mono, ingredientId) -> cacheFilter.getExtraUniqueMonoCacheForMasterIndependentOfRouteType(
                    EntitiesUtils.getListOfNotNullObjects(ingredientId),
                    "getModelByIngredientPersist" + ingredientId,
                    IdGenerated::getId,
                    ingredientId,
                    mono
            );

            this.updateByIngredientInvalidate = (mono, ingredientId) ->
                    cacheFilter.invalidateByWrapper(mono, cacheFilter.byMasterPredicate(ingredientId));

            this.deleteByIngredientInvalidate = (mono, ingredientId) ->
                    cacheFilter.invalidateByWrapper(mono, cacheFilter.byMasterAndIds(ingredientId));
        }
    }

}
