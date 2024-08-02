package com.mocicarazvan.ingredientservice.services.impl;

import com.mocicarazvan.ingredientservice.clients.RecipeClient;
import com.mocicarazvan.ingredientservice.enums.DietType;
import com.mocicarazvan.ingredientservice.exceptions.NameAlreadyExists;
import com.mocicarazvan.ingredientservice.repositories.CustomIngredientRepository;
import com.mocicarazvan.ingredientservice.services.IngredientService;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.ingredientservice.dtos.IngredientBody;
import com.mocicarazvan.ingredientservice.dtos.IngredientResponse;
import com.mocicarazvan.ingredientservice.mappers.IngredientMapper;
import com.mocicarazvan.ingredientservice.models.Ingredient;
import com.mocicarazvan.ingredientservice.repositories.IngredientRepository;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityUsed;
import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class IngredientServiceImpl extends
        ManyToOneUserServiceImpl<
                Ingredient, IngredientBody, IngredientResponse, IngredientRepository, IngredientMapper>
        implements IngredientService {


    private final CustomIngredientRepository customIngredientRepository;
    private final EntitiesUtils entitiesUtils;
    private final RecipeClient recipeClient;

    public IngredientServiceImpl(IngredientRepository modelRepository, IngredientMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, CustomIngredientRepository customIngredientRepository, EntitiesUtils entitiesUtils, RecipeClient recipeClient) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "ingredient", List.of("name", "type", "display", "createdAt", "updatedAt", "id", "fat", "protein",
                "fat",
                "carbohydrates",
                "salt",
                "sugar",
                "saturatedFat"));
        this.customIngredientRepository = customIngredientRepository;
        this.entitiesUtils = entitiesUtils;
        this.recipeClient = recipeClient;
    }

    @Override
    public Mono<IngredientResponse> createModel(IngredientBody body, String userId) {
        return modelRepository.existsByNameIgnoreCase(body.getName())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new NameAlreadyExists("Ingredient with name " + body.getName() + " already exists", body.getName()));
                    }
                    return super.createModel(body, userId);
                });
    }

    @Override
    public Mono<IngredientResponse> updateModel(Long id, IngredientBody body, String userId) {
        return modelRepository.existsByNameIgnoreCaseAndIdNot(body.getName(), id)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new NameAlreadyExists("Ingredient with name " + body.getName() + " already exists", body.getName()));
                    }
                    return super.updateModel(id, body, userId);
                });
    }

    @Override
    public Flux<PageableResponse<IngredientResponse>> getAllModelsFiltered(String name, Boolean display, DietType type, PageableBody pageableBody) {
//        Flux<Ingredient> ingredients;
////        Mono<Long> count;
////        if (name != null && display != null && type != null) {
////            ingredients = modelRepository.findAllByNameContainingIgnoreCaseAndDisplayAndType(name, display, type, pageable);
////            count = modelRepository.countAllByNameContainingIgnoreCaseAndDisplayAndType(name, display, type);
////        } else if (name != null && display != null) {
////            ingredients = modelRepository.findAllByNameContainingIgnoreCaseAndDisplay(name, display, pageable);
////            count = modelRepository.countAllByNameContainingIgnoreCaseAndDisplay(name, display);
////        } else if (name != null && type != null) {
////            ingredients = modelRepository.findAllByNameContainingIgnoreCaseAndType(name, type, pageable);
////            count = modelRepository.countAllByNameContainingIgnoreCaseAndType(name, type);
////        } else if (name != null) {
////            ingredients = modelRepository.findAllByNameContainingIgnoreCase(name, pageable);
////            count = modelRepository.countAllByNameContainingIgnoreCase(name);
////        } else if (display != null && type != null) {
////            ingredients = modelRepository.findAllByDisplayAndType(display, type, pageable);
////            count = modelRepository.countAllByDisplayAndType(display, type);
////        } else if (display != null) {
////            ingredients = modelRepository.findAllByDisplay(display, pageable);
////            count = modelRepository.countAllByDisplay(display);
////        } else if (type != null) {
////            ingredients = modelRepository.findAllByType(type, pageable);
////            count = modelRepository.countAllByType(type);
////        } else {
////            ingredients = modelRepository.findAll(pageable);
////            count = modelRepository.count();
////        }
        Ingredient example = createIngredientExample(name, display, type);
        return pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(pageableUtils.createPageRequest(pageableBody))
                .flatMapMany(pr -> pageableUtils.createPageableResponse(
                        customIngredientRepository.findAllByExample(example, pr)
                                .map(modelMapper::fromModelToResponse),
                        customIngredientRepository.countByExample(example),
                        pr
                ));
    }

    @Override
    public Mono<IngredientResponse> alterDisplay(Long id, Boolean display, String userId) {
        if (display == null) {
            return Mono.error(new IllegalArgumentException("Display cannot be null"));
        }
        return getModel(id)
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
        return this.validIds(ids, modelRepository, modelName);
    }

    @Override
    public Flux<IngredientResponse> getIngredientsByIds(List<Long> ids) {
        return modelRepository.findAllByIdIn(ids)
                .map(modelMapper::fromModelToResponse);
    }

    @Override
    public Mono<IngredientResponse> getIngredientById(Long id, String userId) {
        return userClient.getUser("", userId)
                .flatMap(userDto ->
                        getModel(id)
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
        return getModel(id)
                .map(modelMapper::fromModelToResponse);
    }


    @Override
    public Flux<IngredientResponse> getModelsByIds(List<Long> ids) {
        return modelRepository.findAllByIdInAndDisplayTrue(ids)
                .map(modelMapper::fromModelToResponse);
    }

    @Override
    public Mono<IngredientResponse> deleteModel(Long id, String userId) {
        return recipeClient.getCountInParent(id, userId)
                .flatMap(count -> {
                    if (count.getCount() > 0) {
                        return Mono.error(new SubEntityUsed(modelName, id));
                    }
                    return super.deleteModel(id, userId);
                });
    }
}

