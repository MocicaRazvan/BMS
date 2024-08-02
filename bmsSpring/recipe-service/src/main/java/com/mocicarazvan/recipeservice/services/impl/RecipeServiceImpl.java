package com.mocicarazvan.recipeservice.services.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.recipeservice.clients.IngredientClient;
import com.mocicarazvan.recipeservice.clients.PlanClient;
import com.mocicarazvan.recipeservice.dtos.ingredients.IngredientResponse;
import com.mocicarazvan.recipeservice.dtos.RecipeBody;
import com.mocicarazvan.recipeservice.dtos.RecipeResponse;
import com.mocicarazvan.recipeservice.enums.DietType;
import com.mocicarazvan.recipeservice.exceptions.InvalidTypeException;
import com.mocicarazvan.recipeservice.mappers.RecipeMapper;
import com.mocicarazvan.recipeservice.models.Recipe;
import com.mocicarazvan.recipeservice.repositories.RecipeExtendedRepository;
import com.mocicarazvan.recipeservice.repositories.RecipeRepository;
import com.mocicarazvan.recipeservice.services.IngredientQuantityService;
import com.mocicarazvan.recipeservice.services.RecipeService;
import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.EntityCount;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithEntityCount;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.enums.FileType;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityUsed;
import com.mocicarazvan.templatemodule.services.impl.ApprovedServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class RecipeServiceImpl extends ApprovedServiceImpl<Recipe, RecipeBody, RecipeResponse, RecipeRepository, RecipeMapper>
        implements RecipeService {

    private final RecipeExtendedRepository recipeExtendedRepository;
    private final IngredientClient ingredientClient;
    private final IngredientQuantityService ingredientQuantityService;


    private final PlanClient planClient;

    public RecipeServiceImpl(RecipeRepository modelRepository, RecipeMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils, FileClient fileClient, ObjectMapper objectMapper, RecipeExtendedRepository recipeExtendedRepository, IngredientClient ingredientClient, IngredientQuantityService ingredientQuantityService, PlanClient planClient) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "recipe", List.of("id", "userId", "type", "title", "createdAt", "updatedAt", "approved"), entitiesUtils, fileClient, objectMapper);
        this.recipeExtendedRepository = recipeExtendedRepository;
        this.ingredientClient = ingredientClient;
        this.ingredientQuantityService = ingredientQuantityService;
        this.planClient = planClient;
    }


    @Override
    public Flux<PageableResponse<ResponseWithUserDto<RecipeResponse>>> getRecipesFilteredWithUser(String title, DietType dietType, PageableBody pageableBody, String userId, Boolean approved) {
        return getRecipesFiltered(title, dietType, pageableBody, userId, approved).concatMap(this::getPageableWithUser);
    }

    @Override
    public Flux<PageableResponse<RecipeResponse>> getRecipesFiltered(String title, DietType dietType, PageableBody pageableBody, String userId, Boolean approved) {
        final boolean approvedNotNull = approved != null;

        return protectRoute(approvedNotNull, pageableBody, userId)
                .flatMapMany(pr -> pageableUtils.createPageableResponse(
                        recipeExtendedRepository.getRecipesFiltered(title, approved, dietType, pr).map(modelMapper::fromModelToResponse),
                        recipeExtendedRepository.countRecipesFiltered(title, approved, dietType), pr
                ));
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<RecipeResponse>>> getRecipesFilteredWithCount(String title, DietType dietType, PageableBody pageableBody, String userId, Boolean approved) {
        return getRecipesFiltered(title, dietType, pageableBody, userId, approved)
                .concatMap(pr -> toResponseWithCount(userId, planClient, pr)
                );

    }

//    private Mono<PageableResponse<ResponseWithEntityCount<RecipeResponse>>> toResponseWithCount(String userId, PageableResponse<RecipeResponse> pr) {
//        return planClient.getCountInParent(pr.getContent().getId(), userId)
//                .map(entityCount -> PageableResponse.<ResponseWithEntityCount<RecipeResponse>>builder()
//                        .content(ResponseWithEntityCount.of(pr.getContent(), entityCount))
//                        .pageInfo(pr.getPageInfo())
//                        .links(pr.getLinks())
//                        .build());
//    }

    @Override
    public Mono<RecipeResponse> createModelWithVideos(Flux<FilePart> images, Flux<FilePart> videos, RecipeBody recipeBody, String userId) {


        return ingredientClient.verifyIds(recipeBody.getIngredients().stream().map(i -> i.getIngredientId().toString()).toList(), userId)
                .then(
                        ingredientClient.getByIds(recipeBody.getIngredients().stream().map(i -> i.getIngredientId().toString()).toList(),
                                        userId).collectList().
                                flatMap(ings ->
                                        {
                                            List<DietType> ingTs = ings.stream().map(IngredientResponse::getType).toList();

                                            if (!DietType.isDietTypeValid(recipeBody.getType(), ingTs)) {
                                                return Mono.error(new InvalidTypeException(recipeBody.getType(), ingTs));
                                            }

                                            return getModelToBeCreatedWithVideos(images, videos, recipeBody, userId)
                                                    .flatMap(modelRepository::save)
                                                    .flatMap(m ->
                                                            ingredientQuantityService.saveAllFromIngredientList(m.getId(), recipeBody.getIngredients())
                                                                    .then(Mono.just(modelMapper.fromModelToResponse(m))));
                                        }

                                ));
//                        getModelToBeCreatedWithVideos(images, videos, recipeBody, userId)
//                        .flatMap(modelRepository::save)
//                        .map(modelMapper::fromModelToResponse);
    }

    @Override
    public Mono<RecipeResponse> updateModelWithVideos(Flux<FilePart> images, Flux<FilePart> videos, RecipeBody recipeBody, Long id, String userId) {
        return
                ingredientQuantityService.findAllByRecipeId(id, userId)
                        .map(ing -> ing.getIngredient().getId())
                        .collectList()
                        .map(ingIds -> recipeBody.getIngredients().stream().filter(i -> !ingIds.contains(i.getIngredientId()))
                                .map(i -> i.getIngredientId().toString()).toList())
                        .flatMap(ingIds ->
                        {
                            if (!ingIds.isEmpty()) {
                                return ingredientClient.verifyIds(ingIds, userId);
                            }
                            return Mono.empty();
                        })
                        .then(updateModelWithSuccess(id, userId, model ->
                                {
                                    List<String> urls = model.getImages();
                                    urls.addAll(model.getVideos());
                                    return fileClient.deleteFiles(urls)
                                            .then(uploadFiles(images, FileType.IMAGE)
                                                    .zipWith(uploadFiles(videos, FileType.VIDEO))
                                                    .flatMap(t -> modelMapper.updateModelFromBody(recipeBody, model)
                                                            .flatMap(m -> {
                                                                m.setImages(t.getT1().getFiles());
                                                                m.setVideos(t.getT2().getFiles());
                                                                return ingredientQuantityService.deleteAllByRecipeId(id)
                                                                        .thenMany(ingredientQuantityService.saveAllFromIngredientList(id, recipeBody.getIngredients()))
                                                                        .then(Mono.just(m));

                                                            })));

                                }
                        ));
    }

    @Override
    public Flux<PageableResponse<RecipeResponse>> getRecipesFilteredTrainer(String title, DietType type, Long trainerId, PageableBody pageableBody, String userId, Boolean approved) {
        return getModelsAuthor(trainerId, pageableBody, userId, pr ->
                pageableUtils.createPageableResponse(
                        recipeExtendedRepository.getRecipesFilteredTrainer(title, approved, type, trainerId, pr).map(modelMapper::fromModelToResponse),
                        recipeExtendedRepository.countRecipesFilteredTrainer(title, approved, trainerId, type), pr
                ));
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<RecipeResponse>>> getRecipesFilteredTrainerWithCount(String title, DietType type, Long trainerId, PageableBody pageableBody, String userId, Boolean approved) {
        return getRecipesFilteredTrainer(title, type, trainerId, pageableBody, userId, approved)
                .concatMap(pr -> toResponseWithCount(userId, planClient, pr)
                );
    }

    @Override
    public Mono<ResponseWithUserDto<RecipeResponse>> getModelByIdWithUserInternal(Long id) {
        return getModel(id)
                .flatMap(model -> userClient.getUser("", model.getUserId().toString())
                        .map(user -> ResponseWithUserDto.<RecipeResponse>builder()
                                .model(modelMapper.fromModelToResponse(model))
                                .user(user)
                                .build())
                );
    }

//    @Override
//    public Mono<ResponseWithChildList<ResponseWithUserDto<RecipeResponse>, IngredientQuantityDto>> getRecipeWithIngredients(Long id, String userId) {
//        return super.getModelByIdWithUser(id, userId)
//                .flatMap(ru -> ingredientQuantityService.findAllByRecipeId(id)
//                        .collectList().map(ings -> new ResponseWithChildList<>(ru, ings)));
//    }

    @Override
    public Mono<RecipeResponse> deleteModel(Long id, String userId) {
        return

                userClient.getUser("", userId)
                        .flatMap(authUser -> getModel(id)
                                .flatMap(model ->
                                        planClient.getCountInParent(id, userId)
                                                .flatMap(count -> {
                                                    if (count.getCount() > 0) {
                                                        return Mono.error(new SubEntityUsed("recipe", id));
                                                    }
                                                    return Mono.just(model);
                                                })
                                                .then(
                                                        privateRoute(true, authUser, model.getUserId())
                                                                .then(Mono.just(model))

                                                )
                                )
                                .flatMap(model ->
                                        Mono.when(
                                                        fileClient.deleteFiles(model.getImages()),
                                                        fileClient.deleteFiles(model.getVideos())
                                                ).then(modelRepository.delete(model))
                                                .thenReturn(model)
                                )
                                .map(modelMapper::fromModelToResponse)
                        );
    }

    private Mono<Recipe> getModelToBeCreatedWithVideos(Flux<FilePart> images, Flux<FilePart> videos, RecipeBody recipeBody, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> uploadFiles(images, FileType.IMAGE)
                        .zipWith(uploadFiles(videos, FileType.VIDEO))
                        .map(t -> {
                            Recipe recipe = modelMapper.fromBodyToModel(recipeBody);
                            recipe.setUserId(authUser.getId());
                            recipe.setImages(t.getT1().getFiles());
                            recipe.setVideos(t.getT2().getFiles());
                            if (recipe.getCreatedAt() == null) {
                                recipe.setCreatedAt(LocalDateTime.now());
                            }
                            if (recipe.getUpdatedAt() == null) {
                                recipe.setUpdatedAt(LocalDateTime.now());
                            }
                            if (recipe.getUserDislikes() == null)
                                recipe.setUserDislikes(List.of());
                            if (recipe.getUserLikes() == null)
                                recipe.setUserLikes(List.of());
                            return recipe;
                        })
                );
    }


    @Override
    public Mono<EntityCount> countInParent(Long childId) {
        return modelRepository.countInParent(childId)
                .map(EntityCount::new).log();
    }

    @Override
    public Flux<RecipeResponse> getModelsByIds(List<Long> ids) {
//        return modelRepository.findAllByIdInAndApprovedTrue(ids)
//                .map(modelMapper::fromModelToResponse);
        return modelRepository.findAllByIdIn(ids)
                .map(modelMapper::fromModelToResponse);
    }

    @Override
    public Mono<Void> validIds(List<Long> ids, String userId) {
        return modelRepository.countAllByIdsUser(ids, Long.valueOf(userId))
                .map(count -> count == ids.size())
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new IllegalActionException(modelName + " " + ids.toString() + " are not valid")))
                .then();
    }
}
