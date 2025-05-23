package com.mocicarazvan.recipeservice.services.impl;


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
import com.mocicarazvan.rediscache.annotation.RedisReactiveApprovedCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveApprovedCacheEvict;
import com.mocicarazvan.rediscache.annotation.RedisReactiveCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveCacheEvict;
import com.mocicarazvan.rediscache.enums.BooleanEnum;
import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.enums.FileType;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityUsed;
import com.mocicarazvan.templatemodule.repositories.AssociativeEntityRepository;
import com.mocicarazvan.templatemodule.services.RabbitMqApprovedSender;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.impl.ApprovedServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.OrderEnsurer;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@Service
@Slf4j
public class RecipeServiceImpl extends ApprovedServiceImpl<Recipe, RecipeBody, RecipeResponse, RecipeRepository, RecipeMapper, RecipeServiceImpl.RecipeServiceRedisCacheWrapper>
        implements RecipeService {

    private final RecipeExtendedRepository recipeExtendedRepository;
    private final IngredientClient ingredientClient;
    private final IngredientQuantityService ingredientQuantityService;
    private final RabbitMqApprovedSender<RecipeResponse> rabbitMqSender;
    private final DayClient dayClient;
    private final TransactionalOperator transactionalOperator;
    private final RecipeEmbedServiceImpl recipeEmbedServiceImpl;

    public RecipeServiceImpl(RecipeRepository modelRepository, RecipeMapper modelMapper,
                             PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils,
                             FileClient fileClient, RecipeExtendedRepository recipeExtendedRepository,
                             IngredientClient ingredientClient, IngredientQuantityService ingredientQuantityService,
                             RabbitMqApprovedSender<RecipeResponse> rabbitMqSender, DayClient dayClient,
                             RecipeServiceRedisCacheWrapper self, TransactionalOperator transactionalOperator,
                             RecipeEmbedServiceImpl recipeEmbedServiceImpl,
                             RabbitMqUpdateDeleteService<Recipe> recipeRabbitMqUpdateDeleteService,
                             @Qualifier("userLikesRepository") AssociativeEntityRepository userLikesRepository, @Qualifier("userDislikesRepository") AssociativeEntityRepository userDislikesRepository
    ) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "recipe", List.of("id", "userId", "type", "title", "createdAt", "updatedAt", "approved", PageableUtilsCustom.USER_LIKES_LENGTH_SORT_PROPERTY, PageableUtilsCustom.USER_DISLIKES_LENGTH_SORT_PROPERTY),
                entitiesUtils, fileClient, rabbitMqSender, self, recipeRabbitMqUpdateDeleteService, transactionalOperator, userLikesRepository, userDislikesRepository);
        this.recipeExtendedRepository = recipeExtendedRepository;
        this.ingredientClient = ingredientClient;
        this.ingredientQuantityService = ingredientQuantityService;
        this.rabbitMqSender = rabbitMqSender;
        this.dayClient = dayClient;
        this.transactionalOperator = transactionalOperator;
        this.recipeEmbedServiceImpl = recipeEmbedServiceImpl;
    }

    @Override
    public Mono<List<String>> seedEmbeddings() {
        return modelRepository.findAll()
                .flatMap(recipe -> recipeEmbedServiceImpl.saveEmbedding(recipe.getId(), recipe.getTitle()).then(Mono.just("Seeded embeddings for recipe: " + recipe.getId())))
                .collectList()
                .as(transactionalOperator::transactional);
    }

    @Override
    @RedisReactiveCacheEvict(key = CACHE_KEY_PATH, id = "#id")
    public Mono<RecipeResponse> reactToModel(Long id, String type, String userId) {
        return super.reactToModel(id, type, userId);
    }

    @Override
    public Flux<PageableResponse<ResponseWithUserDto<RecipeResponse>>> getRecipesFilteredWithUser(String title, DietType dietType,
                                                                                                  LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                                  LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                                  PageableBody pageableBody, String userId, Boolean approved, Boolean admin) {
        return getRecipesFiltered(title, dietType,
                createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                pageableBody, userId, approved, admin).flatMapSequential(this::getPageableWithUser);
    }

    @Override
    public Flux<PageableResponse<RecipeResponse>> getRecipesFiltered(String title, DietType dietType,
                                                                     LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                     LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                     PageableBody pageableBody, String userId, Boolean approved, Boolean admin) {
        final boolean approvedNotNull = approved != null;

        return protectRoute(approvedNotNull, pageableBody, userId)
                .flatMapMany(pr ->
                        self.getRecipesFilteredBase(title, dietType,
                                createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                                approved, admin, pr)
                );
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<RecipeResponse>>> getRecipesFilteredWithCount(String title, DietType dietType, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                                       LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                                       PageableBody pageableBody, String userId, Boolean approved, Boolean admin) {
        return getRecipesFiltered(title, dietType,
                createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                pageableBody, userId, approved, admin)
                .flatMapSequential(pr -> toResponseWithCount(userId, dayClient, pr));


    }


    @Override
    public Mono<RecipeResponse> createModelWithVideos(Flux<FilePart> images, Flux<FilePart> videos, RecipeBody recipeBody, String userId, String clientId) {


        return
                ingredientClient.verifyIds(recipeBody.getIngredients().stream().map(i -> i.getIngredientId().toString()).toList(), userId)
                        .then(
                                ingredientClient.getByIds(recipeBody.getIngredients().stream().map(i -> i.getIngredientId().toString()).toList(),
                                                userId)
                                        .map(IngredientResponse::getType)
                                        .collectList().
                                        flatMap(ingTs ->
                                                {
                                                    if (!DietType.isDietTypeValid(recipeBody.getType(), ingTs)) {
                                                        return Mono.error(new InvalidTypeException(recipeBody.getType(), ingTs));
                                                    }

                                                    return getModelToBeCreatedWithVideos(images, videos, recipeBody, userId, clientId)
                                                            .flatMap(modelRepository::save)
                                                            .flatMap(m ->
                                                                    Mono.zip(
                                                                                    Mono.defer(() -> ingredientQuantityService.saveAllFromIngredientList(m.getId(), recipeBody.getIngredients()).then(Mono.just(m))),
                                                                                    Mono.defer(() -> recipeEmbedServiceImpl.saveEmbedding(m.getId(), m.getTitle())))
                                                                            .then(Mono.just(modelMapper.fromModelToResponse(m))));
                                                }

                                        )).flatMap(self::createInvalidate).map(Pair::getFirst).as(transactionalOperator::transactional);
    }


    @Override
    public Mono<RecipeResponse> updateModelWithVideos(Flux<FilePart> images, Flux<FilePart> videos, RecipeBody recipeBody, Long id, String userId, String clientId) {
        return
                verifyRecipeIds(recipeBody, id, userId)
                        .then(updateModelWithSuccess(id, userId, model ->
                                {
                                    List<String> urls = model.getImages();
                                    String origTitle = model.getTitle();
                                    urls.addAll(model.getVideos());
                                    return fileClient.deleteFiles(urls)
                                            .then(uploadFiles(images, FileType.IMAGE, clientId)
                                                    .zipWith(uploadFiles(videos, FileType.VIDEO, clientId))
                                                    .flatMap(t ->
                                                            recipeEmbedServiceImpl.updateEmbeddingWithZip(recipeBody.getTitle(), origTitle, model.getId(), modelMapper.updateModelFromBody(recipeBody, model))
                                                                    .flatMap(handleVideos(recipeBody, id, t))));

                                }
                        )).as(transactionalOperator::transactional);
    }

    @Override
    public Mono<Pair<RecipeResponse, Boolean>> updateModelWithVideosGetOriginalApproved(Flux<FilePart> images, Flux<FilePart> videos,
                                                                                        RecipeBody recipeBody, Long id, String userId, String clientId) {
        return
                verifyRecipeIds(recipeBody, id, userId)
                        .then(updateModelWithSuccessGeneral(id, userId, model ->
                                {
                                    Boolean originalApproved = model.isApproved();
                                    List<String> urls = model.getImages();
                                    urls.addAll(model.getVideos());
                                    String origTitle = model.getTitle();
                                    return fileClient.deleteFiles(urls)
                                            .then(uploadFiles(images, FileType.IMAGE, clientId)
                                                    .zipWith(uploadFiles(videos, FileType.VIDEO, clientId))
                                                    .flatMap(t ->
                                                            recipeEmbedServiceImpl.updateEmbeddingWithZip(recipeBody.getTitle(),
                                                                            origTitle, model.getId(),
                                                                            modelMapper.updateModelFromBody(recipeBody, model))
                                                                    .flatMap(handleVideos(recipeBody, id, t)))).flatMap(modelRepository::save)
                                            .map(modelMapper::fromModelToResponse).map(r -> Pair.of(r, originalApproved));

                                }
                        )).flatMap(self::updateDeleteInvalidate).as(transactionalOperator::transactional);
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
    public Flux<PageableResponse<RecipeResponse>> getRecipesFilteredTrainer(String title, DietType type, Long trainerId,
                                                                            LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                            LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                            PageableBody pageableBody, String userId, Boolean approved) {
        return getModelsAuthor(trainerId, pageableBody, userId, pr ->
                self.getRecipesFilteredTrainerBase(title, type, trainerId,
                        createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                        approved, pr)

        );
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<RecipeResponse>>> getRecipesFilteredTrainerWithCount(String title, DietType type, Long trainerId,
                                                                                                              LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                                              LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                                              PageableBody pageableBody, String userId, Boolean approved) {
        return
                getRecipesFilteredTrainer(title, type, trainerId,
                        createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                        pageableBody, userId, approved)
                        .flatMapSequential(pr -> toResponseWithCount(userId, dayClient, pr));
    }

    @Override
    public Mono<ResponseWithUserDto<RecipeResponse>> getModelByIdWithUserInternal(Long id) {
        return self.getModelInternal(id)
                .flatMap(model -> userClient.getUser("", model.getUserId().toString())
                        .map(user -> ResponseWithUserDto.<RecipeResponse>builder()
                                .model(modelMapper.fromModelToResponse(model))
                                .user(user)
                                .build())
                );
    }

    @Override
    public Mono<RecipeResponse> deleteModel(Long id, String userId) {
        return
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
                                                .doOnSuccess(rabbitMqUpdateDeleteService::sendDeleteMessage)
                                )
                                .map(modelMapper::fromModelToResponse)
                                .flatMap(m -> self.updateDeleteInvalidate(Pair.of(m, m.isApproved())))
                                .map(Pair::getFirst)
                        );
    }

    @Override
    public Recipe cloneModel(Recipe recipe) {
        return recipe.clone();
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
                        .map(EntityCount::new)
//                        .log()
                ;
    }

    @Override
    public Flux<RecipeResponse> getModelsByIds(List<Long> ids) {
        return

                self.getModelsByIdsBase(ids);
    }

    @Override
    public Mono<Void> validIds(List<Long> ids, String userId) {
        return
                modelRepository.countAllByIdsUser(ids, Long.valueOf(userId))
                        .map(count -> count == ids.size())
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(new IllegalActionException(modelName + " " + ids.toString() + " are not valid")))
                        .then();
    }

    @Override
    public Mono<DietType> determineMostRestrictiveDietType(List<Long> recipeIds) {
        return
                recipeExtendedRepository.determineMostRestrictiveDietType(recipeIds);
    }

    @Getter
    @Component
    public static class RecipeServiceRedisCacheWrapper extends ApprovedServiceRedisCacheWrapper<Recipe, RecipeBody, RecipeResponse, RecipeRepository, RecipeMapper> {

        private final RecipeExtendedRepository recipeExtendedRepository;

        public RecipeServiceRedisCacheWrapper(RecipeRepository modelRepository, RecipeMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, RecipeExtendedRepository recipeExtendedRepository) {
            super(modelRepository, modelMapper, "recipe", pageableUtils, userClient);
            this.recipeExtendedRepository = recipeExtendedRepository;
        }

        @Override
        @RedisReactiveApprovedCache(key = CACHE_KEY_PATH, idPath = "entity.id", approved = BooleanEnum.NULL, forWhom = "0")
        public Flux<MonthlyEntityGroup<RecipeResponse>> getModelGroupedByMonthBase(int month, UserDto userDto) {
            return super.getModelGroupedByMonthBase(month, userDto);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<Recipe> getModel(Long id) {
            return super.getModel(id);
        }

        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<Recipe> getModelInternal(Long id) {
            return super.getModel(id);
        }


        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "id")
        public Flux<Recipe> findAllById(List<Long> ids) {
            return super.findAllById(ids);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<ResponseWithUserDto<RecipeResponse>> getModelByIdWithUserBase(UserDto authUser, Long id) {
            return super.getModelByIdWithUserBase(authUser, id);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<ResponseWithUserLikesAndDislikes<RecipeResponse>> getModelByIdWithUserLikesAndDislikesBase(Long id, UserDto authUser) {
            return super.getModelByIdWithUserLikesAndDislikesBase(id, authUser);
        }

        @Override
        @RedisReactiveApprovedCache(key = CACHE_KEY_PATH, approvedArgumentPath = "#approved", idPath = "content.id")
        public Flux<PageableResponse<RecipeResponse>> getModelsTitleBase(boolean approved, PageRequest pr, String newTitle) {
            return super.getModelsTitleBase(approved, pr, newTitle);
        }

        @Override
        @RedisReactiveApprovedCacheEvict(key = CACHE_KEY_PATH, forWhomPath = "#r.userId")
        public Mono<Pair<RecipeResponse, Boolean>> createInvalidate(RecipeResponse r) {
            return super.createInvalidate(r);
        }

        @Override
        @RedisReactiveApprovedCacheEvict(key = CACHE_KEY_PATH, id = "#p.getFirst().getId()", forWhomPath = "#p.getFirst().getUserId()")
        public Mono<Pair<RecipeResponse, Boolean>> updateDeleteInvalidate(Pair<RecipeResponse, Boolean> p) {
            return super.updateDeleteInvalidate(p);
        }

        @RedisReactiveApprovedCache(key = CACHE_KEY_PATH, idPath = "content.id", approvedArgumentPath = "#approved", forWhom = "#admin?0:-1")
        public Flux<PageableResponse<RecipeResponse>> getRecipesFilteredBase(String title, DietType dietType,
                                                                             LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                             LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                             Boolean approved, Boolean admin, PageRequest pr) {

            return
                    pageableUtils.createPageableResponse(
                            recipeExtendedRepository.getRecipesFiltered(title, approved, dietType,
                                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                                    pr).map(modelMapper::fromModelToResponse),
                            recipeExtendedRepository.countRecipesFiltered(title, approved, dietType,
                                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound
                            ), pr

                    );
        }

        @RedisReactiveApprovedCache(key = CACHE_KEY_PATH, idPath = "content.id", approvedArgumentPath = "#approved", forWhom = "#trainerId")
        public Flux<PageableResponse<RecipeResponse>> getRecipesFilteredTrainerBase(String title, DietType type, Long trainerId, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                    LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, Boolean approved, PageRequest pr) {
            return
                    pageableUtils.createPageableResponse(
                            recipeExtendedRepository.getRecipesFilteredTrainer(title, approved, type, trainerId,
                                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                                    pr).map(modelMapper::fromModelToResponse),
                            recipeExtendedRepository.countRecipesFilteredTrainer(title, approved, trainerId, type,
                                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound
                            ), pr
                    );
        }

        @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "id")
        public Flux<RecipeResponse> getModelsByIdsBase(List<Long> ids) {
            return
                    modelRepository.findAllByIdIn(ids)
                            .map(modelMapper::fromModelToResponse)
                            .transform(f -> OrderEnsurer.orderFlux(
                                    f,
                                    ids,
                                    RecipeResponse::getId
                            ));
        }


    }

}
