package com.mocicarazvan.recipeservice.services;

import com.mocicarazvan.recipeservice.dtos.RecipeBody;
import com.mocicarazvan.recipeservice.dtos.RecipeResponse;
import com.mocicarazvan.recipeservice.enums.DietType;
import com.mocicarazvan.recipeservice.mappers.RecipeMapper;
import com.mocicarazvan.recipeservice.models.Recipe;
import com.mocicarazvan.recipeservice.repositories.RecipeRepository;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithEntityCount;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.services.ApprovedService;
import com.mocicarazvan.templatemodule.services.CountInParentService;
import com.mocicarazvan.templatemodule.services.ValidIds;
import org.springframework.data.util.Pair;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface RecipeService extends ApprovedService<Recipe, RecipeBody, RecipeResponse, RecipeRepository, RecipeMapper>, CountInParentService, ValidIds<Recipe, RecipeRepository, RecipeResponse> {
    Mono<List<String>> seedEmbeddings();

    // todo filtered with count for all and for trainer
    Flux<PageableResponse<ResponseWithUserDto<RecipeResponse>>> getRecipesFilteredWithUser(String title, DietType dietType,
                                                                                           LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                           LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                           PageableBody pageableBody, String userId, Boolean approved, Boolean admin);

    Flux<PageableResponse<RecipeResponse>> getRecipesFiltered(String title, DietType dietType,
                                                              LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                              LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                              PageableBody pageableBody, String userId, Boolean approved, Boolean admin);

    Flux<PageableResponse<ResponseWithEntityCount<RecipeResponse>>> getRecipesFilteredWithCount(String title, DietType dietType,
                                                                                                LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                                LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                                PageableBody pageableBody, String userId, Boolean approved, Boolean admin);

    Mono<RecipeResponse> createModelWithVideos(Flux<FilePart> images, Flux<FilePart> videos, RecipeBody recipeBody, String userId, String clientId);

    Mono<RecipeResponse> updateModelWithVideos(Flux<FilePart> images, Flux<FilePart> videos, RecipeBody recipeBody, Long id, String userId, String clientId);

    Mono<Pair<RecipeResponse, Boolean>> updateModelWithVideosGetOriginalApproved(Flux<FilePart> images, Flux<FilePart> videos, RecipeBody recipeBody, Long id, String userId, String clientId);

    Flux<PageableResponse<RecipeResponse>> getRecipesFilteredTrainer(String title, DietType type, Long trainerId,
                                                                     LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                     LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                     PageableBody pageableBody, String userId, Boolean approved);

    Flux<PageableResponse<ResponseWithEntityCount<RecipeResponse>>> getRecipesFilteredTrainerWithCount(String title, DietType type, Long trainerId,
                                                                                                       LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                                       LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                                       PageableBody pageableBody, String userId, Boolean approved);

    Mono<ResponseWithUserDto<RecipeResponse>> getModelByIdWithUserInternal(Long id);

    Mono<Void> validIds(List<Long> ids, String userId);

    Mono<DietType> determineMostRestrictiveDietType(List<Long> recipeIds);

//    Mono<ResponseWithChildList<ResponseWithUserDto<RecipeResponse>, IngredientQuantityDto>> getRecipeWithIngredients(Long id, String userId);
}

