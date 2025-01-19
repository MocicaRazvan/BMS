package com.mocicarazvan.ingredientservice.services.impl;

import com.mocicarazvan.ingredientservice.clients.RecipeClient;
import com.mocicarazvan.ingredientservice.dtos.IngredientNutritionalFactBody;
import com.mocicarazvan.ingredientservice.dtos.IngredientNutritionalFactResponse;
import com.mocicarazvan.ingredientservice.enums.DietType;
import com.mocicarazvan.ingredientservice.mappers.IngredientNutritionalFactMapper;
import com.mocicarazvan.ingredientservice.repositories.ExtendedIngredientNutritionalFactRepository;
import com.mocicarazvan.ingredientservice.repositories.IngredientRepository;
import com.mocicarazvan.ingredientservice.services.IngredientNutritionalFactService;
import com.mocicarazvan.ingredientservice.services.IngredientService;
import com.mocicarazvan.ingredientservice.services.NutritionalFactService;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCacheEvict;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithEntityCount;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Getter
public class IngredientNutritionalFactServiceImpl implements IngredientNutritionalFactService {

    private final IngredientService ingredientService;
    private final NutritionalFactService nutritionalFactService;
    private final IngredientNutritionalFactMapper ingredientNutritionalFactMapper;
    private final ExtendedIngredientNutritionalFactRepository extendedIngredientNutritionalFactRepository;
    private final PageableUtilsCustom pageableUtilsCustom;
    private final RecipeClient recipeClient;
    private final String modelName = "ingredientNutritionalFact";
    private static final String CACHE_KEY_PATH = "#this.modelName";
    private final List<String> allowedSortingFields = List.of("name", "type", "display", "createdAt", "updatedAt", "id", "fat", "protein",
            "fat",
            "carbohydrates",
            "salt",
            "sugar",
            "saturatedFat");

    private final IngredientNutritionalFactRedisCacheWrapper self;
    private final TransactionalOperator transactionalOperator;

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, id = "#id")
    public Mono<IngredientNutritionalFactResponse> deleteModel(Long id, String userId) {

        return
                nutritionalFactService.findByIngredientIdUserId(id, userId)
                        .zipWith(ingredientService.deleteModel(id, userId))
                        .map(t -> ingredientNutritionalFactMapper.fromResponsesToResponse(t.getT2(), t.getT1()))
                        .as(transactionalOperator::transactional)
                ;
    }

    @Override
    public Mono<IngredientNutritionalFactResponse> getModelById(Long id, String userId) {
        return
                ingredientService.getIngredientById(id, userId)
                        .zipWith(nutritionalFactService.findByIngredientId(id))
                        .map(t ->
                                ingredientNutritionalFactMapper.fromResponsesToResponse(t.getT1(), t.getT2())
                        ).as(transactionalOperator::transactional);

    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, id = "#id")
    public Mono<IngredientNutritionalFactResponse> updateModel(Long id, IngredientNutritionalFactBody body, String userId) {

        return
                ingredientService.updateModel(id, body.getIngredient(), userId)
                        .zipWith(nutritionalFactService.updateModelByIngredient(id, body.getNutritionalFact(), userId))
                        .map(t -> ingredientNutritionalFactMapper.fromResponsesToResponse(t.getT1(), t.getT2()))
                        .as(transactionalOperator::transactional);
    }

    @Override
    public Flux<PageableResponse<IngredientNutritionalFactResponse>> getAllModelsFiltered(String name, Boolean display, DietType type, PageableBody pageableBody, String userId, Boolean admin) {

        return self.getAllModelsFiltered(name, display, type, pageableBody, userId, admin, allowedSortingFields);
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<IngredientNutritionalFactResponse>>> getAllModelsFilteredWithEntityCount(String name, Boolean display, DietType type, PageableBody pageableBody, String userId, Boolean admin) {
        return getAllModelsFiltered(name, display, type, pageableBody, userId, admin)
                .flatMapSequential(pr -> recipeClient.getCountInParent(pr.getContent().getIngredient().getId(), userId)
                        .map(entityCount -> PageableResponse.<ResponseWithEntityCount<IngredientNutritionalFactResponse>>builder()
                                .content(ResponseWithEntityCount.of(pr.getContent(), entityCount))
                                .pageInfo(pr.getPageInfo())
                                .links(pr.getLinks())
                                .build()));
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, masterId = "#userId")
    public Mono<IngredientNutritionalFactResponse> createModel(IngredientNutritionalFactBody body, String userId) {

        return
                ingredientService.createModel(body.getIngredient(), userId)
                        .flatMap(ing -> nutritionalFactService.createModel(body.getNutritionalFact(), ing.getId(), userId)
                                .flatMap(nf -> Mono.just(ingredientNutritionalFactMapper.fromResponsesToResponse(ing, nf))
                                )).as(transactionalOperator::transactional);
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, id = "#id")
    public Mono<IngredientNutritionalFactResponse> alterDisplay(Long id, Boolean display, String userId) {

        return
                ingredientService.alterDisplay(id, display, userId)
                        .zipWith(nutritionalFactService.findByIngredientIdUserId(id, userId))
                        .map(t -> ingredientNutritionalFactMapper.fromResponsesToResponse(t.getT1(), t.getT2()));
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


    @Component
    @Getter
    public static class IngredientNutritionalFactRedisCacheWrapper {
        private final String modelName = "ingredientNutritionalFact";
        private final IngredientRepository ingredientRepository;
        private final PageableUtilsCustom pageableUtilsCustom;
        private final ExtendedIngredientNutritionalFactRepository extendedIngredientNutritionalFactRepository;
        private final IngredientNutritionalFactMapper ingredientNutritionalFactMapper;

        public IngredientNutritionalFactRedisCacheWrapper(IngredientRepository ingredientRepository, PageableUtilsCustom pageableUtilsCustom, ExtendedIngredientNutritionalFactRepository extendedIngredientNutritionalFactRepository, IngredientNutritionalFactMapper ingredientNutritionalFactMapper) {
            this.ingredientRepository = ingredientRepository;
            this.pageableUtilsCustom = pageableUtilsCustom;
            this.extendedIngredientNutritionalFactRepository = extendedIngredientNutritionalFactRepository;
            this.ingredientNutritionalFactMapper = ingredientNutritionalFactMapper;
        }

        @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.ingredient.id")
        public Flux<PageableResponse<IngredientNutritionalFactResponse>> getAllModelsFiltered(String name, Boolean display, DietType type, PageableBody pageableBody, String userId, Boolean admin,
                                                                                              List<String> allowedSortingFields
        ) {

            return pageableUtilsCustom.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                    .then(pageableUtilsCustom.createPageRequest(pageableBody))
                    .flatMapMany(pr ->
                            pageableUtilsCustom.createPageableResponse(
                                    extendedIngredientNutritionalFactRepository.getModelsFiltered(name, display, type, pr).map(
                                            ingredientNutritionalFactMapper::fromModelToResponse
                                    ),
                                    extendedIngredientNutritionalFactRepository.countModelsFiltered(name, display, type, pr),
                                    pr
                            )
                    );
        }
    }

}
