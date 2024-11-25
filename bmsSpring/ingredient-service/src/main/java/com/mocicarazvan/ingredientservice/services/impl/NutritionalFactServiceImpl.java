package com.mocicarazvan.ingredientservice.services.impl;

import com.mocicarazvan.ingredientservice.dtos.NutritionalFactBody;
import com.mocicarazvan.ingredientservice.dtos.NutritionalFactResponse;
import com.mocicarazvan.ingredientservice.mappers.NutritionalFactMapper;
import com.mocicarazvan.ingredientservice.models.NutritionalFact;
import com.mocicarazvan.ingredientservice.repositories.NutritionalFactRepository;
import com.mocicarazvan.ingredientservice.services.IngredientService;
import com.mocicarazvan.ingredientservice.services.NutritionalFactService;
import com.mocicarazvan.rediscache.annotation.RedisReactiveCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCacheEvict;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class NutritionalFactServiceImpl extends
        ManyToOneUserServiceImpl<
                NutritionalFact, NutritionalFactBody, NutritionalFactResponse, NutritionalFactRepository, NutritionalFactMapper,
                NutritionalFactServiceImpl.NutritionalFactRedisCacheWrapper
                >
        implements NutritionalFactService {


    public NutritionalFactServiceImpl(NutritionalFactRepository modelRepository, NutritionalFactMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, NutritionalFactRedisCacheWrapper self, IngredientService ingredientService) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "nutritionalFact", List.of("createdAt", "updatedAt", "id", "fat", "saturated_fat",
                "carbohydrates", "sugar", "protein", "salt", "unit", "ingredient_id"
        ), self);
        this.ingredientService = ingredientService;
    }

    private final IngredientService ingredientService;

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, id = "#id", masterPath = "ingredientId")
    public Mono<NutritionalFactResponse> deleteModel(Long id, String userId) {
        return super.deleteModel(id, userId);
    }


    @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.id")
    public Flux<PageableResponse<NutritionalFactResponse>> getAllModels(PageableBody pageableBody, String userId) {
        return super.getAllModels(pageableBody, userId);
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, id = "#id", masterPath = "ingredientId")
    public Mono<NutritionalFactResponse> updateModel(Long id, NutritionalFactBody body, String userId) {
        return super.updateModel(id, body, userId);
    }

    @Override
    @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.id")
    public Flux<PageableResponse<NutritionalFactResponse>> getModelsByIdInPageable(List<Long> ids, PageableBody pageableBody) {
        return super.getModelsByIdInPageable(ids, pageableBody);
    }

    @Override
    public Mono<NutritionalFactResponse> findByIngredientIdUserId(Long ingredientId, String userId) {

        return
                ingredientService.getModelById(ingredientId, userId)
                        .flatMap(i ->
                                self.findByIngredientIdUserId(i.getId(), userId)
                                        .singleOrEmpty()
                        )
                ;
    }

    @Override
    public Mono<NutritionalFactResponse> findByIngredientId(Long ingredientId) {
        return getModelByIngredientId(ingredientId)
                .map(modelMapper::fromModelToResponse);
    }

    private Mono<NutritionalFact> getModelByIngredientId(Long ingredientId) {

        return
                self.getModelByIngredientId(ingredientId).singleOrEmpty()
                        .switchIfEmpty(Mono.error(new NotFoundEntity("NutritionalFact for ingredient id ", ingredientId)));
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, masterId = "#referenceId")
    public Mono<NutritionalFactResponse> createModel(NutritionalFactBody modelBody, Long referenceId, String userId) {

        return
                userClient.getUser("", userId)
                        .flatMap(authUser -> {
                            NutritionalFact model = modelMapper.fromBodyToModel(modelBody);
                            model.setUserId(authUser.getId());
                            model.setIngredientId(referenceId);
                            return modelRepository.save(model)
                                    .map(modelMapper::fromModelToResponse);
                        });
    }

    @Override

    public Mono<NutritionalFactResponse> updateModelByIngredient(Long ingredientId, NutritionalFactBody modelBody, String userId) {

        return
                userClient.getUser("", userId)
                        .flatMap(authUser -> getModelByIngredientId(ingredientId)
                                .flatMap(model -> self.updateByIngredientInvalidate(modelBody, authUser, model)
                                )
                        );
    }


    @Getter
    @Component
    public static class NutritionalFactRedisCacheWrapper extends ManyToOneUserServiceImpl.ManyToOneUserServiceRedisCacheWrapper<NutritionalFact, NutritionalFactBody, NutritionalFactResponse, NutritionalFactRepository, NutritionalFactMapper> {

        public NutritionalFactRedisCacheWrapper(NutritionalFactRepository modelRepository, NutritionalFactMapper modelMapper, UserClient userClient) {
            super(modelRepository, modelMapper, "nutritionalFact", userClient);
        }

        @Override
        @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "entity.id")
        public Flux<MonthlyEntityGroup<NutritionalFactResponse>> getModelGroupedByMonthBase(int month, UserDto userDto) {
            return super.getModelGroupedByMonthBase(month, userDto);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "id")
        public Flux<NutritionalFact> findAllById(List<Long> ids) {
            return super.findAllById(ids);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<NutritionalFact> getModel(Long id) {
            return super.getModel(id);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<ResponseWithUserDto<NutritionalFactResponse>> getModelByIdWithUserBase(UserDto authUser, Long id) {
            return super.getModelByIdWithUserBase(authUser, id);
        }


        @RedisReactiveChildCache(key = CACHE_KEY_PATH, masterId = "#ingredientId", idPath = "id")
        public Flux<NutritionalFactResponse> findByIngredientIdUserId(Long ingredientId, String userId) {
            return
                    modelRepository.findByIngredientId(ingredientId).map(modelMapper::fromModelToResponse)
                            .flatMapMany(Flux::just);

        }

        @RedisReactiveChildCache(key = CACHE_KEY_PATH, masterId = "#ingredientId", idPath = "id")
        public Flux<NutritionalFact> getModelByIngredientId(Long ingredientId) {
            return modelRepository.findByIngredientId(ingredientId)
                    .flatMapMany(Flux::just);
        }

        @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, id = "#model.id", masterPath = "ingredientId")
        public Mono<NutritionalFactResponse> updateByIngredientInvalidate(NutritionalFactBody modelBody, UserDto authUser, NutritionalFact model) {
            return isNotAuthor(model, authUser)
                    .filter(Boolean.FALSE::equals)
                    .switchIfEmpty(Mono.error(new PrivateRouteException()))
                    .flatMap(_ -> modelMapper.updateModelFromBody(modelBody, model)
                            .flatMap(modelRepository::save)
                            .map(modelMapper::fromModelToResponse));
        }

    }


}
