package com.mocicarazvan.ingredientservice.services.impl;

import com.mocicarazvan.ingredientservice.clients.RecipeClient;
import com.mocicarazvan.ingredientservice.dtos.IngredientBody;
import com.mocicarazvan.ingredientservice.dtos.IngredientResponse;
import com.mocicarazvan.ingredientservice.enums.DietType;
import com.mocicarazvan.ingredientservice.exceptions.NameAlreadyExists;
import com.mocicarazvan.ingredientservice.mappers.IngredientMapper;
import com.mocicarazvan.ingredientservice.models.Ingredient;
import com.mocicarazvan.ingredientservice.repositories.CustomIngredientRepository;
import com.mocicarazvan.ingredientservice.repositories.IngredientRepository;
import com.mocicarazvan.ingredientservice.services.IngredientService;
import com.mocicarazvan.templatemodule.adapters.CacheBaseFilteredToHandlerAdapter;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCache;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.generic.IdGenerateDto;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityUsed;
import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function6;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class IngredientServiceImpl extends
        ManyToOneUserServiceImpl<
                Ingredient, IngredientBody, IngredientResponse, IngredientRepository, IngredientMapper>
        implements IngredientService {


    private final CustomIngredientRepository customIngredientRepository;
    private final EntitiesUtils entitiesUtils;
    private final RecipeClient recipeClient;
    private final IngredientServiceCacheHandler ingredientServiceCacheHandler;
    private final NutritionalFactServiceImpl.NutritionalFactServiceCacheHandler nutritionalFactServiceCacheHandler;

    public IngredientServiceImpl(IngredientRepository modelRepository, IngredientMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, CustomIngredientRepository customIngredientRepository, EntitiesUtils entitiesUtils, RecipeClient recipeClient, IngredientServiceCacheHandler ingredientServiceCacheHandler, NutritionalFactServiceImpl.NutritionalFactServiceCacheHandler nutritionalFactServiceCacheHandler) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "ingredient", List.of("name", "type", "display", "createdAt", "updatedAt", "id", "fat", "protein",
                "fat",
                "carbohydrates",
                "salt",
                "sugar",
                "saturatedFat"), ingredientServiceCacheHandler);
        this.customIngredientRepository = customIngredientRepository;
        this.entitiesUtils = entitiesUtils;
        this.recipeClient = recipeClient;
        this.ingredientServiceCacheHandler = ingredientServiceCacheHandler;
        this.nutritionalFactServiceCacheHandler = nutritionalFactServiceCacheHandler;
    }

    @Override
    public Mono<IngredientResponse> createModel(IngredientBody body, String userId) {
        return
                ingredientServiceCacheHandler.getCreateModelInvalidate().apply(
                        modelRepository.existsByNameIgnoreCase(body.getName())
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new NameAlreadyExists("Ingredient with name " + body.getName() + " already exists", body.getName()));
                                    }
                                    return super.createModel(body, userId);
                                }), body, userId);
    }

    @Override
    public Mono<IngredientResponse> updateModel(Long id, IngredientBody body, String userId) {
        return
                nutritionalFactServiceCacheHandler.getUpdateByIngredientInvalidate().apply(
                        ingredientServiceCacheHandler.getUpdateModelInvalidate().apply(
                                modelRepository.existsByNameIgnoreCaseAndIdNot(body.getName(), id)
                                        .flatMap(exists -> {
                                            if (exists) {
                                                return Mono.error(new NameAlreadyExists("Ingredient with name " + body.getName() + " already exists", body.getName()));
                                            }
                                            return super.updateModel(id, body, userId);
                                        }), id, body, userId), id);
    }

    @Override
    public Flux<PageableResponse<IngredientResponse>> getAllModelsFiltered(String name, Boolean display, DietType type, PageableBody pageableBody, Boolean admin) {
        Ingredient example = createIngredientExample(name, display, type);
        return pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(pageableUtils.createPageRequest(pageableBody))
                .flatMapMany(pr ->
                        ingredientServiceCacheHandler.getAllModelsFilteredPersist.apply(
                                pageableUtils.createPageableResponse(
                                        customIngredientRepository.findAllByExample(example, pr)
                                                .map(modelMapper::fromModelToResponse),
                                        customIngredientRepository.countByExample(example),
                                        pr
                                ), name, display, type, pageableBody, admin)

                );
    }

    @Override
    public Mono<IngredientResponse> alterDisplay(Long id, Boolean display, String userId) {
        if (display == null) {
            return Mono.error(new IllegalArgumentException("Display cannot be null"));
        }
        return
                ingredientServiceCacheHandler.getUpdateModelInvalidate().apply(
                        getModel(id)
                                .flatMap(ing -> {
                                    ing.setDisplay(display);
                                    return modelRepository.save(ing)
                                            .map(modelMapper::fromModelToResponse);
                                }), id, new IngredientBody(), userId);
    }

    private Ingredient createIngredientExample(String name, Boolean display, DietType type) {
        Ingredient probe = new Ingredient();
        if (name != null) {
            probe.setName(name);
        }
        if (display != null) {
            probe.setDisplay(display);
        }
        if (type != null) {
            probe.setType(type);
        }
        return probe;
    }

    @Override
    public Mono<Void> validIds(List<Long> ids) {
        return
                ingredientServiceCacheHandler.getValidIdsPersist().apply(
                                this.validIds(ids, modelRepository, modelName)
                                        .thenReturn(true), ids)
                        .then();
    }

    @Override
    public Flux<IngredientResponse> getIngredientsByIds(List<Long> ids) {
        return
                ingredientServiceCacheHandler.getIngredientsByIdsPersist.apply(
                        modelRepository.findAllByIdIn(ids)
                                .map(modelMapper::fromModelToResponse), ids);
    }

    @Override
    public Mono<IngredientResponse> getIngredientById(Long id, String userId) {
        return userClient.getUser("", userId)
                .flatMap(userDto ->
                        getModel(id)
                                .flatMap(m -> {
                                    if (!m.isDisplay() && !userDto.getRole().equals(Role.ROLE_ADMIN)) {
                                        return Mono.error(new PrivateRouteException());
                                    }
                                    return Mono.just(modelMapper.fromModelToResponse(m));
                                })
                );
    }

    @Override
    public Mono<IngredientResponse> getIngredientByIdInternal(Long id) {
        return getModel(id)
                .map(modelMapper::fromModelToResponse);
    }


    @Override
    public Flux<IngredientResponse> getModelsByIds(List<Long> ids) {
        return
                ingredientServiceCacheHandler.getModelsByIdsPersist.apply(
                        modelRepository.findAllByIdInAndDisplayTrue(ids)
                                .map(modelMapper::fromModelToResponse), ids);
    }

    @Override
    public Mono<IngredientResponse> deleteModel(Long id, String userId) {
        return
                nutritionalFactServiceCacheHandler.getDeleteByIngredientInvalidate().apply(
                        ingredientServiceCacheHandler.getDeleteModelInvalidate().apply(
                                recipeClient.getCountInParent(id, userId)
                                        .flatMap(count -> {
                                            if (count.getCount() > 0) {
                                                return Mono.error(new SubEntityUsed(modelName, id));
                                            }
                                            return super.deleteModel(id, userId);
                                        }), id, userId), id);
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Component
    public static class IngredientServiceCacheHandler
            extends ManyToOneUserServiceImpl.ManyToOneUserServiceCacheHandler<Ingredient, IngredientBody, IngredientResponse> {
        private final FilteredListCaffeineCache<FilterKeyType, IngredientResponse> cacheFilter;

        Function6<Flux<PageableResponse<IngredientResponse>>, String, Boolean, DietType, PageableBody, Boolean, Flux<PageableResponse<IngredientResponse>>>
                getAllModelsFilteredPersist;
        Function2<Mono<Boolean>, List<Long>, Mono<Boolean>> validIdsPersist;
        Function2<Flux<IngredientResponse>, List<Long>, Flux<IngredientResponse>> getIngredientsByIdsPersist;
        Function2<Flux<IngredientResponse>, List<Long>, Flux<IngredientResponse>> getModelsByIdsPersist;

        public IngredientServiceCacheHandler(FilteredListCaffeineCache<FilterKeyType, IngredientResponse> cacheFilter) {
            super();
            this.cacheFilter = cacheFilter;
            CacheBaseFilteredToHandlerAdapter.convertToManyUserHandler(cacheFilter, this);

            this.getAllModelsFilteredPersist = (flux, name, display, type, pageableBody, admin) ->
            {
                FilterKeyType.KeyRouteType keyRouteType = Boolean.TRUE.equals(admin) ? FilterKeyType.KeyRouteType.createForAdmin() : FilterKeyType.KeyRouteType.createForPublic();
                return cacheFilter.getUniqueFluxCache(
                        EntitiesUtils.getListOfNotNullObjects(name, display, type, pageableBody, admin),
                        "getAllModelsFilteredPersist",
                        m -> m.getContent().getId(),
                        keyRouteType, flux
                );
            };

            this.validIdsPersist = (mono, ids) -> cacheFilter.getUniqueMonoCacheIdListIndependent(
                    EntitiesUtils.getListOfNotNullObjects(ids),
                    "validIdsPersist" + ids,
                    ids,
                    mono
            );

            this.getIngredientsByIdsPersist = (flux, ids) -> cacheFilter.getUniqueFluxCacheIndependent(
                    EntitiesUtils.getListOfNotNullObjects(ids),
                    "getIngredientsByIdsPersist" + ids,
                    IdGenerateDto::getId,
                    flux
            );
            this.getModelsByIdsPersist = (flux, ids) -> cacheFilter.getUniqueFluxCacheIndependent(
                    EntitiesUtils.getListOfNotNullObjects(ids),
                    "getModelsByIdsPersist" + ids,
                    IdGenerateDto::getId,
                    flux
            );
        }
    }
}

