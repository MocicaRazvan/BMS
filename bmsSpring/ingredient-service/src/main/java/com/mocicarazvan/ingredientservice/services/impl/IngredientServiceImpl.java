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
import com.mocicarazvan.rediscache.annotation.RedisReactiveCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCacheEvict;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityUsed;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class IngredientServiceImpl extends
        ManyToOneUserServiceImpl<
                Ingredient, IngredientBody, IngredientResponse, IngredientRepository, IngredientMapper, IngredientServiceImpl.IngredientServiceRedisCacheWrapper>
        implements IngredientService {


    private final CustomIngredientRepository customIngredientRepository;
    private final EntitiesUtils entitiesUtils;
    private final RecipeClient recipeClient;
    private final TransactionalOperator transactionalOperator;
    private final IngredientEmbedServiceImpl ingredientEmbedServiceImpl;

    private static final String CACHE_KEY_PATH = "#this.modelName";

    public IngredientServiceImpl(IngredientRepository modelRepository, IngredientMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, CustomIngredientRepository customIngredientRepository, EntitiesUtils entitiesUtils, RecipeClient recipeClient, IngredientServiceRedisCacheWrapper self, TransactionalOperator transactionalOperator, IngredientEmbedServiceImpl ingredientEmbedServiceImpl, RabbitMqUpdateDeleteService<Ingredient> rabbitMqUpdateDeleteService) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "ingredient", List.of("name", "type", "display", "createdAt", "updatedAt", "id", "fat", "protein",
                "fat",
                "carbohydrates",
                "salt",
                "sugar",
                "saturatedFat"), self, rabbitMqUpdateDeleteService);
        this.customIngredientRepository = customIngredientRepository;
        this.entitiesUtils = entitiesUtils;
        this.recipeClient = recipeClient;
        this.transactionalOperator = transactionalOperator;
        this.ingredientEmbedServiceImpl = ingredientEmbedServiceImpl;
    }

    public Mono<List<String>> seedEmbeddings() {
        int batchSize = 20;

        return modelRepository.findAll()
                .collectList()
                .flatMapMany(ingredients -> Flux.fromIterable(partitionList(ingredients, batchSize)))
                .concatMap(batch ->
                        transactionalOperator.transactional(
                                Flux.fromIterable(batch)
                                        .flatMap(ingredient ->
                                                ingredientEmbedServiceImpl.saveEmbedding(ingredient.getId(), ingredient.getName())
                                        )
                                        .then(Mono.just("Seeded embeddings for batch of size: " + batch.size()))
                        )
                )
                .collectList();
    }

    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }


    @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.id")
    public Flux<PageableResponse<IngredientResponse>> getAllModels(PageableBody pageableBody, String userId) {
        return super.getAllModels(pageableBody, userId);
    }

    @Override
    @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.id")
    public Flux<PageableResponse<IngredientResponse>> getModelsByIdInPageable(List<Long> ids, PageableBody pageableBody) {
        return super.getModelsByIdInPageable(ids, pageableBody);
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, masterId = "#userId")
    public Mono<IngredientResponse> createModel(IngredientBody body, String userId) {
        return

                modelRepository.existsByNameIgnoreCase(body.getName())
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(new NameAlreadyExists("Ingredient with name " + body.getName() + " already exists", body.getName()));
                            }
                            return super.createModel(body, userId)
                                    .flatMap(ing -> ingredientEmbedServiceImpl.saveEmbedding(ing.getId(), ing.getName())
                                            .thenReturn(ing)
                                    ).as(transactionalOperator::transactional);
                        });
    }


    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, id = "#id", masterId = "#userId")
    public Mono<IngredientResponse> updateModel(Long id, IngredientBody body, String userId) {

        return
                modelRepository.existsByNameIgnoreCaseAndIdNot(body.getName(), id)
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(new NameAlreadyExists("Ingredient with name " + body.getName() + " already exists", body.getName()));
                            }
                            return super.updateModelWithSuccess(id, userId, m -> ingredientEmbedServiceImpl.updateEmbeddingWithZip(body.getName(), m.getName(), id, modelMapper.updateModelFromBody(body, m)))
                                    .as(transactionalOperator::transactional);
                        });
    }

    @Override
    public Ingredient cloneModel(Ingredient ingredient) {
        return ingredient.clone();
    }

    @Override
    @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.id")
    public Flux<PageableResponse<IngredientResponse>> getAllModelsFiltered(String name, Boolean display, DietType type, PageableBody pageableBody, Boolean admin) {
        Ingredient example = createIngredientExample(name, display, type);

        return pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(pageableUtils.createPageRequest(pageableBody))
                .flatMapMany(pr ->
                        pageableUtils.createPageableResponse(
                                customIngredientRepository.findAllByExample(example, pr)
                                        .map(modelMapper::fromModelToResponse),
                                customIngredientRepository.countByExample(example),
                                pr
                        )

                );
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, id = "#id")
    public Mono<IngredientResponse> alterDisplay(Long id, Boolean display, String userId) {
        if (display == null) {
            return Mono.error(new IllegalArgumentException("Display cannot be null"));
        }

        return
                getModel(id)
                        .flatMap(ing -> {
                            ing.setDisplay(display);
                            return modelRepository.save(ing)
                                    .map(modelMapper::fromModelToResponse);
                        });
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
                this.validIds(ids, modelRepository, modelName)
                ;

    }

    @Override
    @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "id")
    public Flux<IngredientResponse> getIngredientsByIds(List<Long> ids) {

        return
                modelRepository.findAllByIdIn(ids)
                        .map(modelMapper::fromModelToResponse);
    }

    @Override
    public Mono<IngredientResponse> getIngredientById(Long id, String userId) {

        return userClient.getUser("", userId)
                .flatMap(userDto ->
                        self.getIngredientById(id)
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
        return self.getIngredientByIdInternal(id)
                .map(modelMapper::fromModelToResponse);
    }


    @Override
    @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "id")
    public Flux<IngredientResponse> getModelsByIds(List<Long> ids) {
        return
                modelRepository.findAllByIdInAndDisplayTrue(ids)
                        .map(modelMapper::fromModelToResponse);
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, id = "#id")
    public Mono<IngredientResponse> deleteModel(Long id, String userId) {
        return
                recipeClient.getCountInParent(id, userId)
                        .flatMap(count -> {
                            if (count.getCount() > 0) {
                                return Mono.error(new SubEntityUsed(modelName, id));
                            }
                            return super.deleteModel(id, userId);
                        });
    }

    @Getter
    @Component
    public static class IngredientServiceRedisCacheWrapper extends ManyToOneUserServiceImpl.ManyToOneUserServiceRedisCacheWrapper<Ingredient, IngredientBody, IngredientResponse, IngredientRepository, IngredientMapper> {

        public IngredientServiceRedisCacheWrapper(IngredientRepository modelRepository, IngredientMapper modelMapper, UserClient userClient) {
            super(modelRepository, modelMapper, "ingredient", userClient);
        }

        @Override
        @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "entity.id")
        public Flux<MonthlyEntityGroup<IngredientResponse>> getModelGroupedByMonthBase(int month, UserDto userDto) {
            return super.getModelGroupedByMonthBase(month, userDto);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "id")
        public Flux<Ingredient> findAllById(List<Long> ids) {
            return super.findAllById(ids);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<Ingredient> getModel(Long id) {
            return super.getModel(id);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<ResponseWithUserDto<IngredientResponse>> getModelByIdWithUserBase(UserDto authUser, Long id) {
            return super.getModelByIdWithUserBase(authUser, id);
        }

        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<Ingredient> getIngredientById(Long id) {
            return super.getModel(id);
        }

        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<Ingredient> getIngredientByIdInternal(Long id) {
            return super.getModel(id);
        }
    }

}

