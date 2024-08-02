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
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithEntityCount;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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

    @Override
    public Mono<IngredientNutritionalFactResponse> deleteModel(Long id, String userId) {
        return nutritionalFactService.findByIngredientIdUserId(id, userId)
                .zipWith(ingredientService.deleteModel(id, userId))
                .map(t -> ingredientNutritionalFactMapper.fromResponsesToResponse(t.getT2(), t.getT1()));
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
        return ingredientService.updateModel(id, body.getIngredient(), userId)
                .zipWith(nutritionalFactService.updateModel(id, body.getNutritionalFact(), userId))
                .map(t -> ingredientNutritionalFactMapper.fromResponsesToResponse(t.getT1(), t.getT2()));
    }

    @Override
    public Flux<PageableResponse<IngredientNutritionalFactResponse>> getAllModelsFiltered(String name, Boolean display, DietType type, PageableBody pageableBody, String userId) {
//        return ingredientService.getAllModelsFiltered(name, display, type, pageableBody)
//                .concatMap(pr -> nutritionalFactService.findByIngredientId(pr.getContent().getId(), userId)
//                        .map(nf -> PageableResponse.<IngredientNutritionalFactResponse>builder()
//                                .content(ingredientNutritionalFactMapper.fromResponsesToResponse(pr.getContent(), nf))
//                                .pageInfo(pr.getPageInfo())
//                                .links(pr.getLinks())
//                                .build()));
        return pageableUtilsCustom.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(pageableUtilsCustom.createPageRequest(pageableBody))
                .flatMapMany(pr -> pageableUtilsCustom.createPageableResponse(
                        extendedIngredientNutritionalFactRepository.getModelsFiltered(name, display, type, pr).map(
                                ingredientNutritionalFactMapper::fromModelToResponse
                        ),
                        extendedIngredientNutritionalFactRepository.countModelsFiltered(name, display, type, pr),
                        pr
                ));
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<IngredientNutritionalFactResponse>>> getAllModelsFilteredWithEntityCount(String name, Boolean display, DietType type, PageableBody pageableBody, String userId) {
        return getAllModelsFiltered(name, display, type, pageableBody, userId)
                .concatMap(pr -> recipeClient.getCountInParent(pr.getContent().getIngredient().getId(), userId)
                        .map(entityCount -> PageableResponse.<ResponseWithEntityCount<IngredientNutritionalFactResponse>>builder()
                                .content(ResponseWithEntityCount.of(pr.getContent(), entityCount))
                                .pageInfo(pr.getPageInfo())
                                .links(pr.getLinks())
                                .build()));
    }

    @Override
    public Mono<IngredientNutritionalFactResponse> createModel(IngredientNutritionalFactBody body, String userId) {
        return ingredientService.createModel(body.getIngredient(), userId)
                .flatMap(ing -> nutritionalFactService.createModel(body.getNutritionalFact(), ing.getId(), userId)
                        .flatMap(nf -> Mono.just(ingredientNutritionalFactMapper.fromResponsesToResponse(ing, nf))
                        ));
    }

    @Override
    public Mono<IngredientNutritionalFactResponse> alterDisplay(Long id, Boolean display, String userId) {
        return ingredientService.alterDisplay(id, display, userId)
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


}
