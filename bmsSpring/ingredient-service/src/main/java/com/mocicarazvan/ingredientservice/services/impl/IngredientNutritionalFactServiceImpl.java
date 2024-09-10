package com.mocicarazvan.ingredientservice.services.impl;

import com.mocicarazvan.ingredientservice.clients.RecipeClient;
import com.mocicarazvan.ingredientservice.dtos.IngredientNutritionalFactBody;
import com.mocicarazvan.ingredientservice.dtos.IngredientNutritionalFactResponse;
import com.mocicarazvan.ingredientservice.enums.DietType;
import com.mocicarazvan.ingredientservice.mappers.IngredientNutritionalFactMapper;
import com.mocicarazvan.ingredientservice.repositories.ExtendedIngredientNutritionalFactRepository;
import com.mocicarazvan.ingredientservice.services.IngredientNutritionalFactService;
import com.mocicarazvan.ingredientservice.services.IngredientService;
import com.mocicarazvan.ingredientservice.services.NutritionalFactService;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCache;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithEntityCount;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function7;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class IngredientNutritionalFactServiceImpl implements IngredientNutritionalFactService {

    private final IngredientService ingredientService;
    private final NutritionalFactService nutritionalFactService;
    private final IngredientNutritionalFactMapper ingredientNutritionalFactMapper;
    private final ExtendedIngredientNutritionalFactRepository extendedIngredientNutritionalFactRepository;
    private final PageableUtilsCustom pageableUtilsCustom;
    private final RecipeClient recipeClient;
    private final List<String> allowedSortingFields = List.of("name", "type", "display", "createdAt", "updatedAt", "id", "fat", "protein",
            "fat",
            "carbohydrates",
            "salt",
            "sugar",
            "saturatedFat");

    private final IngredientNutritionalFactCacheHandler ingredientNutritionalFactCacheHandler;

    @Override
    public Mono<IngredientNutritionalFactResponse> deleteModel(Long id, String userId) {
        return
                ingredientNutritionalFactCacheHandler.deleteUpdateModelInvalidate.apply(
                        nutritionalFactService.findByIngredientIdUserId(id, userId)
                                .zipWith(ingredientService.deleteModel(id, userId))
                                .map(t -> ingredientNutritionalFactMapper.fromResponsesToResponse(t.getT2(), t.getT1()))
                        , id);
    }

    @Override
    public Mono<IngredientNutritionalFactResponse> getModelById(Long id, String userId) {
        return
                ingredientService.getIngredientById(id, userId)
                        .zipWith(nutritionalFactService.findByIngredientId(id))
                        .map(t ->
                                ingredientNutritionalFactMapper.fromResponsesToResponse(t.getT1(), t.getT2())
                        );
    }

    @Override
    public Mono<IngredientNutritionalFactResponse> updateModel(Long id, IngredientNutritionalFactBody body, String userId) {
        return
                ingredientNutritionalFactCacheHandler.deleteUpdateModelInvalidate.apply(
                        ingredientService.updateModel(id, body.getIngredient(), userId)
                                .zipWith(nutritionalFactService.updateModelByIngredient(id, body.getNutritionalFact(), userId))
                                .map(t -> ingredientNutritionalFactMapper.fromResponsesToResponse(t.getT1(), t.getT2())), id);
    }

    @Override
    public Flux<PageableResponse<IngredientNutritionalFactResponse>> getAllModelsFiltered(String name, Boolean display, DietType type, PageableBody pageableBody, String userId, Boolean admin) {
        return pageableUtilsCustom.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(pageableUtilsCustom.createPageRequest(pageableBody))
                .flatMapMany(pr ->
                        ingredientNutritionalFactCacheHandler.getAllModelsFilteredPersist.apply(
                                pageableUtilsCustom.createPageableResponse(
                                        extendedIngredientNutritionalFactRepository.getModelsFiltered(name, display, type, pr).map(
                                                ingredientNutritionalFactMapper::fromModelToResponse
                                        ),
                                        extendedIngredientNutritionalFactRepository.countModelsFiltered(name, display, type, pr),
                                        pr
                                ), name, display, type, pageableBody, userId, admin)
                );
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<IngredientNutritionalFactResponse>>> getAllModelsFilteredWithEntityCount(String name, Boolean display, DietType type, PageableBody pageableBody, String userId, Boolean admin) {
        return getAllModelsFiltered(name, display, type, pageableBody, userId, admin)
                .concatMap(pr -> recipeClient.getCountInParent(pr.getContent().getIngredient().getId(), userId)
                        .map(entityCount -> PageableResponse.<ResponseWithEntityCount<IngredientNutritionalFactResponse>>builder()
                                .content(ResponseWithEntityCount.of(pr.getContent(), entityCount))
                                .pageInfo(pr.getPageInfo())
                                .links(pr.getLinks())
                                .build()));
    }

    @Override
    public Mono<IngredientNutritionalFactResponse> createModel(IngredientNutritionalFactBody body, String userId) {
        return
                ingredientNutritionalFactCacheHandler.createModelInvalidate.apply(
                        ingredientService.createModel(body.getIngredient(), userId)
                                .flatMap(ing -> nutritionalFactService.createModel(body.getNutritionalFact(), ing.getId(), userId)
                                        .flatMap(nf -> Mono.just(ingredientNutritionalFactMapper.fromResponsesToResponse(ing, nf))
                                        )));
    }

    @Override
    public Mono<IngredientNutritionalFactResponse> alterDisplay(Long id, Boolean display, String userId) {
        return
                ingredientNutritionalFactCacheHandler.deleteUpdateModelInvalidate.apply(
                        ingredientService.alterDisplay(id, display, userId)
                                .zipWith(nutritionalFactService.findByIngredientIdUserId(id, userId))
                                .map(t -> ingredientNutritionalFactMapper.fromResponsesToResponse(t.getT1(), t.getT2())), id);
    }

    @Override
    public Flux<IngredientNutritionalFactResponse> getModelsByIds(List<Long> ids) {
        return ingredientService.getIngredientsByIds(ids)
                .flatMap(ing -> nutritionalFactService.findByIngredientId(ing.getId())
                        .map(nf -> ingredientNutritionalFactMapper.fromResponsesToResponse(ing, nf)));
    }

    @Override
    public Mono<IngredientNutritionalFactResponse> getModelByIdInternal(Long id, String userId) {
        return ingredientService.getIngredientByIdInternal(id)
                .zipWith(nutritionalFactService.findByIngredientId(id))
                .map(t -> ingredientNutritionalFactMapper.fromResponsesToResponse(t.getT1(), t.getT2()));
    }


    @Data
    @Component
    public static class IngredientNutritionalFactCacheHandler {
        private final FilteredListCaffeineCache<FilterKeyType, IngredientNutritionalFactResponse> cacheFilter;

        Function2<Mono<IngredientNutritionalFactResponse>, Long, Mono<IngredientNutritionalFactResponse>> deleteUpdateModelInvalidate;
        Function<Mono<IngredientNutritionalFactResponse>, Mono<IngredientNutritionalFactResponse>> createModelInvalidate;
        Function7<Flux<PageableResponse<IngredientNutritionalFactResponse>>, String, Boolean, DietType, PageableBody, String, Boolean, Flux<PageableResponse<IngredientNutritionalFactResponse>>>
                getAllModelsFilteredPersist;

        public IngredientNutritionalFactCacheHandler(FilteredListCaffeineCache<FilterKeyType, IngredientNutritionalFactResponse> cacheFilter) {
            this.cacheFilter = cacheFilter;

            this.deleteUpdateModelInvalidate = (mono, id) -> cacheFilter.invalidateByWrapperCallback(
                    mono, r -> cacheFilter.updateDeleteBasePredicate(id, r.getIngredient().getUserId())
            );

            this.createModelInvalidate = (mono) -> cacheFilter.invalidateByWrapperCallback(
                    mono, r -> cacheFilter.createBasePredicate(r.getIngredient().getUserId())
            );

            this.getAllModelsFilteredPersist = (flux, name, display, type, pageableBody, userId, admin) ->
            {
                FilterKeyType.KeyRouteType keyRouteType = Boolean.TRUE.equals(admin) ? FilterKeyType.KeyRouteType.createForAdmin() : FilterKeyType.KeyRouteType.createForPublic();
                return cacheFilter.getUniqueFluxCache(
                        EntitiesUtils.getListOfNotNullObjects(name, display, type, pageableBody, admin),
                        "getAllModelsFiltered",
                        m -> m.getContent().getIngredient().getId(),
                        keyRouteType,
                        flux
                );
            };

        }


    }

}
