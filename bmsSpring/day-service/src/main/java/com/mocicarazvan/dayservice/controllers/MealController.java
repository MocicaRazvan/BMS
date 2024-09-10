package com.mocicarazvan.dayservice.controllers;


import com.mocicarazvan.dayservice.dtos.meal.MealBody;
import com.mocicarazvan.dayservice.dtos.meal.MealResponse;
import com.mocicarazvan.dayservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.dayservice.enums.DietType;
import com.mocicarazvan.dayservice.hateos.meal.MealReactiveResponseBuilder;
import com.mocicarazvan.dayservice.mappers.MealMapper;
import com.mocicarazvan.dayservice.models.Meal;
import com.mocicarazvan.dayservice.repositories.MealRepository;
import com.mocicarazvan.dayservice.services.MealService;
import com.mocicarazvan.templatemodule.controllers.ManyToOneUserController;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithChildListEntity;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/meals")
@RequiredArgsConstructor
public class MealController implements ManyToOneUserController<
        Meal, MealBody, MealResponse, MealRepository, MealMapper, MealService
        > {

    private final MealService mealService;
    private final RequestsUtils requestsUtils;
    private final MealReactiveResponseBuilder mealReactiveResponseBuilder;

    @Override
    @DeleteMapping(value = "/delete/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<MealResponse>>> deleteModel(@PathVariable Long id, ServerWebExchange exchange) {
        return mealService.deleteModel(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> mealReactiveResponseBuilder.toModel(m, MealController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<MealResponse>>> getModelById(@PathVariable Long id, ServerWebExchange exchange) {
        return mealService.getModelById(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> mealReactiveResponseBuilder.toModel(m, MealController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/withUser/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<MealResponse>>> getModelByIdWithUser(@PathVariable Long id, ServerWebExchange exchange) {
        return mealService.getModelByIdWithUser(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> mealReactiveResponseBuilder.toModelWithUser(m, MealController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PutMapping(value = "/update/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<MealResponse>>> updateModel(@Valid @RequestBody MealBody mealBody, @PathVariable Long id, ServerWebExchange exchange) {
        return mealService.updateModel(id, mealBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> mealReactiveResponseBuilder.toModel(m, MealController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PatchMapping(value = "/byIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<MealResponse>>> getModelsByIdIn(@Valid @RequestBody PageableBody pageableBody, @RequestParam List<Long> ids) {
        return mealService.getModelsByIdInPageable(ids, pageableBody)
                .flatMap(m -> mealReactiveResponseBuilder.toModelPageable(m, MealController.class));
    }

    @Override
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<MealResponse>>> createModel(@Valid @RequestBody MealBody mealBody, ServerWebExchange exchange) {
        return mealService.createModel(mealBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> mealReactiveResponseBuilder.toModel(m, MealController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/admin/groupedByMonth", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyEntityGroup<CustomEntityModel<MealResponse>>> getModelGroupedByMonth(@RequestParam int month, ServerWebExchange exchange) {
        return mealService.getModelGroupedByMonth(month, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> mealReactiveResponseBuilder.toModelMonthlyEntityGroup(m, MealController.class));
    }


    @GetMapping(value = "/recipes/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<RecipeResponse> getRecipesByMeal(@PathVariable Long id, ServerWebExchange exchange) {
        return mealService.getRecipesByMeal(id, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping(value = "/internal/recipes/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<RecipeResponse> getRecipesByMealInternal(@PathVariable Long id, ServerWebExchange exchange) {
        return mealService.getRecipesByMealInternal(id, requestsUtils.extractAuthUser(exchange));
    }


    @GetMapping(value = "/day/{dayId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<CustomEntityModel<MealResponse>> getMealsByDay(@PathVariable Long dayId, ServerWebExchange exchange) {
        return mealService.getMealsByDay(dayId, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> mealReactiveResponseBuilder.toModel(m, MealController.class));
    }

    @GetMapping(value = "/day/recipes/{dayId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<ResponseWithChildListEntity<MealResponse, RecipeResponse>> getMealsByDayWithRecipes(@PathVariable Long dayId, ServerWebExchange exchange) {
        return mealService.getMealsByDayWithRecipes(dayId, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> mealReactiveResponseBuilder.toModelWithChildListEntity(m, MealController.class));
    }

    @GetMapping(value = "/day/dietType", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<DietType>> determineMostRestrictiveDietTypeByDay(@RequestParam List<Long> ids, ServerWebExchange exchange) {
        return mealService.determineMostRestrictiveDietTypeByDay(ids, requestsUtils.extractAuthUser(exchange))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/internal/day/{dayId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<MealResponse> getMealsByDayInternal(@PathVariable Long dayId, ServerWebExchange exchange) {
        return mealService.getMealsByDayInternal(dayId, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping(value = "/internal/entity/day/{dayId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<CustomEntityModel<MealResponse>> getMealsByDayInternalEntity(@PathVariable Long dayId, ServerWebExchange exchange) {
        return mealService.getMealsByDayInternal(dayId, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> mealReactiveResponseBuilder.toModel(m, MealController.class));
    }


}
