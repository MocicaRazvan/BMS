package com.mocicarazvan.recipeservice.services.impl;

import com.mocicarazvan.recipeservice.dtos.RecipeBody;
import com.mocicarazvan.recipeservice.dtos.RecipeResponse;
import com.mocicarazvan.recipeservice.dtos.ingredients.IngredientQuantityDto;
import com.mocicarazvan.recipeservice.enums.DietType;
import com.mocicarazvan.recipeservice.mappers.RecipeMapper;
import com.mocicarazvan.recipeservice.models.Recipe;
import com.mocicarazvan.recipeservice.repositories.RecipeRepository;
import com.mocicarazvan.recipeservice.services.IngredientQuantityService;
import com.mocicarazvan.recipeservice.services.RecipeService;
import com.mocicarazvan.templatemodule.enums.FileType;
import com.mocicarazvan.templatemodule.services.MockItemService;
import com.mocicarazvan.templatemodule.utils.Randomizer;
import org.springframework.data.util.Pair;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class MockRecipeService extends MockItemService<RecipeResponse> {
    private final List<DietType> dietTypes = List.of(
            DietType.values()
    );
    private final RecipeService recipeService;
    private final IngredientQuantityService ingredientQuantityService;
    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;

    protected MockRecipeService(TransactionalOperator transactionalOperator, RecipeService recipeService, IngredientQuantityService ingredientQuantityService, RecipeRepository recipeRepository, RecipeMapper recipeMapper) {
        super(transactionalOperator, 5);
        this.recipeService = recipeService;
        this.ingredientQuantityService = ingredientQuantityService;
        this.recipeRepository = recipeRepository;
        this.recipeMapper = recipeMapper;
    }

    @Override
    protected Mono<Pair<RecipeResponse, List<FilePart>>> mockItemsBase(Long itemId, String userId) {
        return recipeService.getModel(itemId)
                .zipWith(ingredientQuantityService.findAllByRecipeId(itemId, userId)
                        .map(i -> new IngredientQuantityDto(i.getCount(), i.getIngredient().getId()))
                        .collectList()
                )
                .flatMap(t -> {
                    Recipe recipe = t.getT1();
                    List<IngredientQuantityDto> ingredientQuantityDtos = t.getT2();
                    RecipeBody recipeBody = RecipeBody.builder()
                            .type(recipe.getType())
                            .ingredients(Randomizer.shuffleList(ingredientQuantityDtos))
                            .body(recipe.getBody())
                            .title(createTitle(recipe.getTitle()))
                            .build();
                    return getFiles(recipe.getImages(), FileType.IMAGE)
                            .zipWith(getFiles(recipe.getVideos(), FileType.VIDEO))
                            .flatMap(files -> recipeService.createModelWithVideos(
                                                    Flux.fromIterable(Randomizer.shuffleList(files.getT1())),
                                                    Flux.fromIterable(Randomizer.shuffleList(files.getT2())),
                                                    recipeBody,
                                                    userId,
                                                    UUID.randomUUID().toString()
                                            )
                                            .flatMap(r ->
                                                    recipeRepository.findById(r.getId())
                                                            .flatMap(recipe1 -> {
                                                                recipe1.setApproved(true);
                                                                return recipeRepository.save(recipe1)
                                                                        .map(recipeMapper::fromModelToResponse);
                                                            })
                                            )
                                            .map(r -> {
                                                List<FilePart> combinedFiles = new ArrayList<>(files.getT1());
                                                combinedFiles.addAll(files.getT2());
                                                return Pair.of(r, combinedFiles);
                                            })
                            );
                });
    }
}
