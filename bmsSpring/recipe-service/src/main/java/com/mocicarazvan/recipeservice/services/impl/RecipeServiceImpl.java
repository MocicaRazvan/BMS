package com.mocicarazvan.recipeservice.services.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.recipeservice.clients.DayClient;
import com.mocicarazvan.recipeservice.clients.IngredientClient;
import com.mocicarazvan.recipeservice.dtos.RecipeBody;
import com.mocicarazvan.recipeservice.dtos.RecipeResponse;
import com.mocicarazvan.recipeservice.dtos.ingredients.IngredientResponse;
import com.mocicarazvan.recipeservice.enums.DietType;
import com.mocicarazvan.recipeservice.exceptions.InvalidTypeException;
import com.mocicarazvan.recipeservice.mappers.RecipeMapper;
import com.mocicarazvan.recipeservice.models.Recipe;
import com.mocicarazvan.recipeservice.repositories.RecipeExtendedRepository;
import com.mocicarazvan.recipeservice.repositories.RecipeRepository;
import com.mocicarazvan.recipeservice.services.IngredientQuantityService;
import com.mocicarazvan.recipeservice.services.RecipeService;
import com.mocicarazvan.templatemodule.adapters.CacheApprovedFilteredToHandlerAdapter;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheApproveFilterKey;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.generic.IdGenerateDto;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.enums.FileType;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityUsed;
import com.mocicarazvan.templatemodule.services.RabbitMqApprovedSenderWrapper;
import com.mocicarazvan.templatemodule.services.impl.ApprovedServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function3;
import org.springframework.data.util.Pair;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.Function7;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@Service
@Slf4j
public class RecipeServiceImpl extends ApprovedServiceImpl<Recipe, RecipeBody, RecipeResponse, RecipeRepository, RecipeMapper>
        implements RecipeService {

    private final RecipeExtendedRepository recipeExtendedRepository;
    private final IngredientClient ingredientClient;
    private final IngredientQuantityService ingredientQuantityService;
    private final RecipeServiceCacheHandler recipeServiceCacheHandler;
    private final RabbitMqApprovedSenderWrapper<RecipeResponse> rabbitMqSender;
    private final DayClient dayClient;

    public RecipeServiceImpl(RecipeRepository modelRepository, RecipeMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils, FileClient fileClient, ObjectMapper objectMapper, RecipeExtendedRepository recipeExtendedRepository, IngredientClient ingredientClient, IngredientQuantityService ingredientQuantityService, RecipeServiceCacheHandler recipeServiceCacheHandler, RabbitMqApprovedSenderWrapper<RecipeResponse> rabbitMqSender, DayClient dayClient) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "recipe", List.of("id", "userId", "type", "title", "createdAt", "updatedAt", "approved"), entitiesUtils, fileClient, objectMapper, recipeServiceCacheHandler, rabbitMqSender);
        this.recipeExtendedRepository = recipeExtendedRepository;
        this.ingredientClient = ingredientClient;
        this.ingredientQuantityService = ingredientQuantityService;
        this.recipeServiceCacheHandler = recipeServiceCacheHandler;
        this.rabbitMqSender = rabbitMqSender;
        this.dayClient = dayClient;
    }


    @Override
    public Flux<PageableResponse<ResponseWithUserDto<RecipeResponse>>> getRecipesFilteredWithUser(String title, DietType dietType, PageableBody pageableBody, String userId, Boolean approved, Boolean admin) {
        return getRecipesFiltered(title, dietType, pageableBody, userId, approved, admin).concatMap(this::getPageableWithUser);
    }

    @Override
    public Flux<PageableResponse<RecipeResponse>> getRecipesFiltered(String title, DietType dietType, PageableBody pageableBody, String userId, Boolean approved, Boolean admin) {
        final boolean approvedNotNull = approved != null;

        return protectRoute(approvedNotNull, pageableBody, userId)
                .flatMapMany(pr ->
                        recipeServiceCacheHandler.getRecipesFilteredPersist.apply(
                                pageableUtils.createPageableResponse(
                                        recipeExtendedRepository.getRecipesFiltered(title, approved, dietType, pr).map(modelMapper::fromModelToResponse),
                                        recipeExtendedRepository.countRecipesFiltered(title, approved, dietType), pr
                                ), title, dietType, pageableBody, userId, approved, admin)
                );
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<RecipeResponse>>> getRecipesFilteredWithCount(String title, DietType dietType, PageableBody pageableBody, String userId, Boolean approved, Boolean admin) {
        return getRecipesFiltered(title, dietType, pageableBody, userId, approved, admin).concatMap(pr -> toResponseWithCount(userId, dayClient, pr));


    }


    @Override
    public Mono<RecipeResponse> createModelWithVideos(Flux<FilePart> images, Flux<FilePart> videos, RecipeBody recipeBody, String userId, String clientId) {


        return
                recipeServiceCacheHandler.getCreateModelInvalidate().apply(
                        ingredientClient.verifyIds(recipeBody.getIngredients().stream().map(i -> i.getIngredientId().toString()).toList(), userId)
                                .then(
                                        ingredientClient.getByIds(recipeBody.getIngredients().stream().map(i -> i.getIngredientId().toString()).toList(),
                                                        userId).collectList().
                                                flatMap(ings ->
                                                        {
                                                            List<DietType> ingTs = ings.stream().map(IngredientResponse::getType).toList();

                                                            if (!DietType.isDietTypeValid(recipeBody.getType(), ingTs)) {
                                                                return Mono.error(new InvalidTypeException(recipeBody.getType(), ingTs));
                                                            }

                                                            return getModelToBeCreatedWithVideos(images, videos, recipeBody, userId, clientId)
                                                                    .flatMap(modelRepository::save)
                                                                    .flatMap(m ->
                                                                            ingredientQuantityService.saveAllFromIngredientList(m.getId(), recipeBody.getIngredients())
                                                                                    .then(Mono.just(modelMapper.fromModelToResponse(m))));
                                                        }

                                                )), recipeBody, userId);
    }

    @Override
    public Mono<RecipeResponse> updateModelWithVideos(Flux<FilePart> images, Flux<FilePart> videos, RecipeBody recipeBody, Long id, String userId, String clientId) {
        return
                recipeServiceCacheHandler.getUpdateModelInvalidate().apply(
                        verifyRecipeIds(recipeBody, id, userId)
                                .then(updateModelWithSuccess(id, userId, model ->
                                        {
                                            List<String> urls = model.getImages();
                                            urls.addAll(model.getVideos());
                                            return fileClient.deleteFiles(urls)
                                                    .then(uploadFiles(images, FileType.IMAGE, clientId)
                                                            .zipWith(uploadFiles(videos, FileType.VIDEO, clientId))
                                                            .flatMap(t -> modelMapper.updateModelFromBody(recipeBody, model)
                                                                    .flatMap(handleVideos(recipeBody, id, t))));

                                        }
                                )), id, recipeBody, userId);
    }

    @Override
    public Mono<Pair<RecipeResponse, Boolean>> updateModelWithVideosGetOriginalApproved(Flux<FilePart> images, Flux<FilePart> videos, RecipeBody recipeBody, Long id, String userId, String clientId) {
        return
                recipeServiceCacheHandler.getUpdateModelGetOriginalApprovedInvalidate().apply(
                        verifyRecipeIds(recipeBody, id, userId)
                                .then(updateModelWithSuccessGeneral(id, userId, model ->
                                        {
                                            Boolean originalApproved = model.isApproved();
                                            List<String> urls = model.getImages();
                                            urls.addAll(model.getVideos());
                                            return fileClient.deleteFiles(urls)
                                                    .then(uploadFiles(images, FileType.IMAGE, clientId)
                                                            .zipWith(uploadFiles(videos, FileType.VIDEO, clientId))
                                                            .flatMap(t -> modelMapper.updateModelFromBody(recipeBody, model)
                                                                    .flatMap(handleVideos(recipeBody, id, t)))).flatMap(modelRepository::save)
                                                    .map(modelMapper::fromModelToResponse).map(r -> Pair.of(r, originalApproved));

                                        }
                                )), id, recipeBody, userId);
    }

    private Function<Recipe, Mono<? extends Recipe>> handleVideos(RecipeBody recipeBody, Long id, Tuple2<FileUploadResponse, FileUploadResponse> t) {
        return m -> {
            m.setImages(t.getT1().getFiles());
            m.setVideos(t.getT2().getFiles());
            return ingredientQuantityService.deleteAllByRecipeId(id)
                    .thenMany(ingredientQuantityService.saveAllFromIngredientList(id, recipeBody.getIngredients()))
                    .then(Mono.just(m));

        };
    }

    private Mono<Void> verifyRecipeIds(RecipeBody recipeBody, Long id, String userId) {
        return ingredientQuantityService.findAllByRecipeId(id, userId)
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
                });
    }

    @Override
    public Flux<PageableResponse<RecipeResponse>> getRecipesFilteredTrainer(String title, DietType type, Long trainerId, PageableBody pageableBody, String userId, Boolean approved) {
        return getModelsAuthor(trainerId, pageableBody, userId, pr ->
                recipeServiceCacheHandler.getRecipesFilteredTrainerPersist.apply(
                        pageableUtils.createPageableResponse(
                                recipeExtendedRepository.getRecipesFilteredTrainer(title, approved, type, trainerId, pr).map(modelMapper::fromModelToResponse),
                                recipeExtendedRepository.countRecipesFilteredTrainer(title, approved, trainerId, type), pr
                        ), title, type, trainerId, pageableBody, userId, approved)

        );
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<RecipeResponse>>> getRecipesFilteredTrainerWithCount(String title, DietType type, Long trainerId, PageableBody pageableBody, String userId, Boolean approved) {
        return
                getRecipesFilteredTrainer(title, type, trainerId, pageableBody, userId, approved)
                        .concatMap(pr -> toResponseWithCount(userId, dayClient, pr));
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
                recipeServiceCacheHandler.getDeleteModelInvalidate().apply(
                        userClient.getUser("", userId)
                                .flatMap(authUser -> getModel(id)
                                        .flatMap(model ->
                                                dayClient.getCountInParent(id, userId)
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
                                ), id, userId);
    }

    private Mono<Recipe> getModelToBeCreatedWithVideos(Flux<FilePart> images, Flux<FilePart> videos, RecipeBody recipeBody, String userId, String clientId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> uploadFiles(images, FileType.IMAGE, clientId)
                        .zipWith(uploadFiles(videos, FileType.VIDEO, clientId))
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
        return

                modelRepository.countInParent(childId)
                        .collectList()
                        .map(EntityCount::new).log();
    }

    @Override
    public Flux<RecipeResponse> getModelsByIds(List<Long> ids) {
//        return modelRepository.findAllByIdInAndApprovedTrue(ids)
//                .map(modelMapper::fromModelToResponse);
        return
                recipeServiceCacheHandler.getModelsByIdsPersist.apply(
                        modelRepository.findAllByIdIn(ids)
                                .map(modelMapper::fromModelToResponse), ids);
    }

    @Override
    public Mono<Void> validIds(List<Long> ids, String userId) {
        return
                recipeServiceCacheHandler.validIdsPersist.apply(
                                modelRepository.countAllByIdsUser(ids, Long.valueOf(userId))
                                        .map(count -> count == ids.size()), ids, userId)
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(new IllegalActionException(modelName + " " + ids.toString() + " are not valid")))
                        .then();
    }

    @Override
    public Mono<DietType> determineMostRestrictiveDietType(List<Long> recipeIds) {
        return
                recipeServiceCacheHandler.determineMostRestrictiveDietTypePersist.apply(
                        recipeExtendedRepository.determineMostRestrictiveDietType(recipeIds), recipeIds);
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Component
    public static class RecipeServiceCacheHandler
            extends ApprovedServiceImpl.ApprovedServiceCacheHandler<Recipe, RecipeBody, RecipeResponse> {
        private final FilteredListCaffeineCacheApproveFilterKey<RecipeResponse> cacheFilterList;

        Function7<Flux<PageableResponse<RecipeResponse>>, String, DietType, PageableBody, String, Boolean, Boolean, Flux<PageableResponse<RecipeResponse>>> getRecipesFilteredPersist;
        Function7<Flux<PageableResponse<RecipeResponse>>, String, DietType, Long, PageableBody, String, Boolean, Flux<PageableResponse<RecipeResponse>>> getRecipesFilteredTrainerPersist;
        Function2<Flux<RecipeResponse>, List<Long>, Flux<RecipeResponse>> getModelsByIdsPersist;
        Function3<Mono<Boolean>, List<Long>, String, Mono<Boolean>> validIdsPersist;
        Function2<Mono<DietType>, List<Long>, Mono<DietType>> determineMostRestrictiveDietTypePersist;

        public RecipeServiceCacheHandler(FilteredListCaffeineCacheApproveFilterKey<RecipeResponse> cacheFilterList) {
            super();
            this.cacheFilterList = cacheFilterList;
            CacheApprovedFilteredToHandlerAdapter.convert(cacheFilterList, this);

            this.getRecipesFilteredPersist
                    = (flux, title, dietType, pageableBody, userId, approved, admin) -> {
                FilterKeyType.KeyRouteType keyRouteType = Boolean.TRUE.equals(admin) ? FilterKeyType.KeyRouteType.createForAdmin() : FilterKeyType.KeyRouteType.createForPublic();
                return cacheFilterList.getExtraUniqueFluxCache(
                        EntitiesUtils.getListOfNotNullObjects(flux, title, dietType, pageableBody, approved, admin),
                        "getRecipesPersist",
                        m -> m.getContent().getId(),
                        keyRouteType,
                        approved,
                        flux
                );
            };

            this.getRecipesFilteredTrainerPersist = (flux, title, type, trainerId, pageableBody, userId, approved) ->
                    cacheFilterList.getExtraUniqueCacheForTrainer(
                            EntitiesUtils.getListOfNotNullObjects(title, type, trainerId, pageableBody, approved),
                            trainerId,
                            "getRecipesFilteredTrainerPersist" + trainerId,
                            m -> m.getContent().getId(),
                            approved,
                            flux
                    );


            this.getModelsByIdsPersist = (flux, ids) -> cacheFilterList.getExtraUniqueFluxCacheIndependent(
                    EntitiesUtils.getListOfNotNullObjects(ids),
                    "getModelsByIdsPersist" + ids,
                    IdGenerateDto::getId,
                    flux
            );

            this.validIdsPersist = (mono, ids, userId) -> cacheFilterList.getExtraUniqueMonoCacheIdListIndependent(
                    EntitiesUtils.getListOfNotNullObjects(ids),
                    "validIdsPersist" + ids,
                    ids,
                    mono
            );

            this.determineMostRestrictiveDietTypePersist = (mono, recipeIds) -> cacheFilterList.getExtraUniqueMonoCacheIdListIndependent(
                    EntitiesUtils.getListOfNotNullObjects(recipeIds),
                    "determineMostRestrictiveDietTypePersist" + recipeIds,
                    recipeIds,
                    mono
            );
        }
    }
}
