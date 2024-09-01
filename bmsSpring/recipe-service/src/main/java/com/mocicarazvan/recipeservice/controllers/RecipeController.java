package com.mocicarazvan.recipeservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.recipeservice.dtos.RecipeBody;
import com.mocicarazvan.recipeservice.dtos.RecipeResponse;
import com.mocicarazvan.recipeservice.dtos.ingredients.IngredientNutritionalFactResponseWithCount;
import com.mocicarazvan.recipeservice.enums.DietType;
import com.mocicarazvan.recipeservice.hateos.RecipeReactiveResponseBuilder;
import com.mocicarazvan.recipeservice.mappers.RecipeMapper;
import com.mocicarazvan.recipeservice.models.Recipe;
import com.mocicarazvan.recipeservice.repositories.RecipeRepository;
import com.mocicarazvan.recipeservice.services.IngredientQuantityService;
import com.mocicarazvan.recipeservice.services.RecipeService;
import com.mocicarazvan.templatemodule.controllers.ApproveController;
import com.mocicarazvan.templatemodule.controllers.CountInParentController;
import com.mocicarazvan.templatemodule.controllers.ValidControllerIds;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/recipes")
@Slf4j
public class RecipeController implements ApproveController<Recipe, RecipeBody, RecipeResponse, RecipeRepository, RecipeMapper, RecipeService>,
        CountInParentController
        ,
        ValidControllerIds<RecipeResponse> {

    private final RecipeService recipeService;
    private final RequestsUtils requestsUtils;
    private final IngredientQuantityService ingredientQuantityService;
    private final RecipeReactiveResponseBuilder recipeReactiveResponseBuilder;
    private final ObjectMapper objectMapper;

    @Override
    @PatchMapping(value = "/approved", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<RecipeResponse>>> getModelsApproved(
            @RequestParam(required = false) String title, @Valid @RequestBody PageableBody pageableBody, ServerWebExchange exchange
    ) {
        return recipeService.getModelsApproved(title, pageableBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> recipeReactiveResponseBuilder.toModelPageable(m, RecipeController.class));
    }

    @Override
    @PatchMapping("/withUser")
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithUserDtoEntity<RecipeResponse>>> getModelsWithUser(@RequestParam(required = false) String title,
                                                                                               @RequestParam(name = "approved", required = false, defaultValue = "true") boolean approved,
                                                                                               @Valid @RequestBody PageableBody pageableBody,
                                                                                               ServerWebExchange exchange) {
        return recipeService.getModelsWithUser(title, pageableBody, requestsUtils.extractAuthUser(exchange), approved)
                .concatMap(m -> recipeReactiveResponseBuilder.toModelWithUserPageable(m, RecipeController.class));
    }

    @Override
    @PatchMapping(value = "/trainer/{trainerId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<RecipeResponse>>> getModelsTrainer(@RequestParam(required = false) String title,
                                                                                      @RequestParam(required = false) Boolean approved,
                                                                                      @Valid @RequestBody PageableBody pageableBody,
                                                                                      @PathVariable Long trainerId,
                                                                                      ServerWebExchange exchange) {
        return recipeService.getModelsTrainer(title, trainerId, pageableBody, requestsUtils.extractAuthUser(exchange), approved)
                .flatMap(m -> recipeReactiveResponseBuilder.toModelPageable(m, RecipeController.class));
    }

    @Override
    @PatchMapping(value = "/admin/approve/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<RecipeResponse>>> approveModel(@PathVariable Long id,
                                                                                        @RequestParam boolean approved,
                                                                                        ServerWebExchange exchange) {
        return recipeService.approveModel(id, requestsUtils.extractAuthUser(exchange), approved)
                .flatMap(m -> recipeReactiveResponseBuilder.toModelWithUser(m, RecipeController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PatchMapping(value = "/admin", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<RecipeResponse>>> getAllModelsAdmin(@RequestParam(required = false) String title,
                                                                                       @Valid @RequestBody PageableBody pageableBody,
                                                                                       ServerWebExchange exchange) {
        return recipeService.getAllModels(title, pageableBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> recipeReactiveResponseBuilder.toModelPageable(m, RecipeController.class));
    }

    @Override
    @DeleteMapping(value = "/delete/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<RecipeResponse>>> deleteModel(@PathVariable Long id, ServerWebExchange exchange) {
        return recipeService.deleteModel(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> recipeReactiveResponseBuilder.toModel(m, RecipeController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<RecipeResponse>>> getModelById(@PathVariable Long id, ServerWebExchange exchange) {
        return recipeService.getModelById(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> recipeReactiveResponseBuilder.toModel(m, RecipeController.class))
                .map(ResponseEntity::ok);
    }


    @Override
    @GetMapping(value = "/withUser/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<RecipeResponse>>> getModelByIdWithUser(@PathVariable Long id, ServerWebExchange exchange) {
        return recipeService.getModelByIdWithUser(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> recipeReactiveResponseBuilder.toModelWithUser(m, RecipeController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PutMapping(value = "/update/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<RecipeResponse>>> updateModel(@Valid @RequestBody RecipeBody recipeBody, @PathVariable Long id, ServerWebExchange exchange) {
        return recipeService.updateModel(id, recipeBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> recipeReactiveResponseBuilder.toModel(m, RecipeController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PatchMapping(value = "/byIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<RecipeResponse>>> getModelsByIdIn(@Valid @RequestBody PageableBody pageableBody,
                                                                                     @RequestParam List<Long> ids) {
        return recipeService.getModelsByIdIn(ids, pageableBody)
                .flatMap(m -> recipeReactiveResponseBuilder.toModelPageable(m, RecipeController.class));
    }

    @Override
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<RecipeResponse>>> createModel(@Valid @RequestBody RecipeBody recipeBody, ServerWebExchange exchange) {
        return recipeService.createModel(recipeBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> recipeReactiveResponseBuilder.toModel(m, RecipeController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/admin/groupedByMonth")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyEntityGroup<CustomEntityModel<RecipeResponse>>> getModelGroupedByMonth(@RequestParam int month, ServerWebExchange exchange) {
        return recipeService.getModelGroupedByMonth(month, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> recipeReactiveResponseBuilder.toModelMonthlyEntityGroup(m, RecipeController.class));
    }

    @Override
    @PatchMapping(value = "/like/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<RecipeResponse>>> likeModel(@PathVariable Long id, ServerWebExchange exchange) {
        return recipeService.reactToModel(id, "like", requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> recipeReactiveResponseBuilder.toModel(m, RecipeController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PatchMapping(value = "/dislike/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<RecipeResponse>>> dislikeModel(@PathVariable Long id, ServerWebExchange exchange) {
        return recipeService.reactToModel(id, "dislike", requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> recipeReactiveResponseBuilder.toModel(m, RecipeController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/withUser/withReactions/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserLikesAndDislikesEntity<RecipeResponse>>> getModelsWithUserAndReaction(@PathVariable Long id, ServerWebExchange exchange) {
        return recipeService.getModelByIdWithUserLikesAndDislikes(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> recipeReactiveResponseBuilder.toModelWithUserLikesAndDislikes(m, RecipeController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PostMapping(value = "/createWithImages", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<CustomEntityModel<RecipeResponse>>> createModelWithImages(@RequestPart("files") Flux<FilePart> files,
                                                                                         @RequestPart("body") String body,
                                                                                         @RequestParam("clientId") String clientId,
                                                                                         ServerWebExchange exchange) {
        return requestsUtils.getBodyFromJson(body, RecipeBody.class, objectMapper)
                .flatMap(recipeBody -> recipeService.createModel(files, recipeBody, requestsUtils.extractAuthUser(exchange), clientId)
                        .flatMap(m -> recipeReactiveResponseBuilder.toModel(m, RecipeController.class))
                        .map(ResponseEntity::ok));
    }

    @Override
    @PostMapping(value = "/updateWithImages/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<RecipeResponse>>> updateModelWithImages(@RequestPart("files") Flux<FilePart> files,
                                                                                         @RequestPart("body") String body,
                                                                                         @RequestParam("clientId") String clientId,
                                                                                         @PathVariable Long id,
                                                                                         ServerWebExchange exchange) {
        return requestsUtils.getBodyFromJson(body, RecipeBody.class, objectMapper)
                .flatMap(recipeBody -> recipeService.updateModelWithImages(files, id, recipeBody, requestsUtils.extractAuthUser(exchange), clientId)
                        .flatMap(m -> recipeReactiveResponseBuilder.toModel(m, RecipeController.class))
                        .map(ResponseEntity::ok));
    }

    @Override
    @GetMapping(value = "/internal/count/{childId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<EntityCount>> getCountInParent(@PathVariable Long childId) {
        return recipeService.countInParent(childId)
                .map(ResponseEntity::ok);
    }

    @PatchMapping(value = "/filtered", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<RecipeResponse>>> getAllModelsFiltered(@RequestParam(required = false) String title,
                                                                                          @RequestParam(required = false) Boolean approved,
                                                                                          @RequestParam(required = false) DietType type,
                                                                                          @Valid @RequestBody PageableBody pageableBody,
                                                                                          ServerWebExchange exchange) {
        return recipeService.getRecipesFiltered(title, type, pageableBody, requestsUtils.extractAuthUser(exchange), approved)
                .flatMap(m -> recipeReactiveResponseBuilder.toModelPageable(m, RecipeController.class));
    }

    @PatchMapping(value = "/filteredWithCount", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithEntityCount<CustomEntityModel<RecipeResponse>>>> getAllModelsFilteredWithCount(@RequestParam(required = false) String title,
                                                                                                                            @RequestParam(required = false) Boolean approved,
                                                                                                                            @RequestParam(required = false) DietType type,
                                                                                                                            @Valid @RequestBody PageableBody pageableBody,
                                                                                                                            ServerWebExchange exchange) {
        return recipeService.getRecipesFilteredWithCount(title, type, pageableBody, requestsUtils.extractAuthUser(exchange), approved)
                .flatMap(m -> recipeReactiveResponseBuilder.toModelWithEntityCountPageable(m, RecipeController.class));
    }

    @PatchMapping(value = "/filtered/withUser", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithUserDtoEntity<RecipeResponse>>> getAllModelsFilteredWithUser(@RequestParam(required = false) String title,
                                                                                                          @RequestParam(required = false) Boolean approved,
                                                                                                          @RequestParam(required = false) DietType type,
                                                                                                          @Valid @RequestBody PageableBody pageableBody,
                                                                                                          ServerWebExchange exchange) {
        return recipeService.getRecipesFilteredWithUser(title, type, pageableBody, requestsUtils.extractAuthUser(exchange), approved)
                .flatMap(m -> recipeReactiveResponseBuilder.toModelWithUserPageable(m, RecipeController.class));
    }

    @PostMapping(value = "/createWithVideos", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<CustomEntityModel<RecipeResponse>>> createWithVideos(
            @RequestPart("images") Flux<FilePart> images,
            @RequestPart("videos") Flux<FilePart> videos,
            @RequestPart("body") String body,
            @RequestParam("clientId") String clientId,
            ServerWebExchange exchange) {
        return requestsUtils.getBodyFromJson(body, RecipeBody.class, objectMapper)
                .flatMap(recipeBody -> recipeService.createModelWithVideos(images, videos, recipeBody, requestsUtils.extractAuthUser(exchange), clientId))
                .flatMap(m -> recipeReactiveResponseBuilder.toModel(m, RecipeController.class))
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/updateWithVideos/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<CustomEntityModel<RecipeResponse>>> updateWithVideos(
            @RequestPart("images") Flux<FilePart> images,
            @RequestPart("videos") Flux<FilePart> videos,
            @RequestPart("body") String body,
            @RequestParam("clientId") String clientId,
            @PathVariable Long id,
            ServerWebExchange exchange) {
        return requestsUtils.getBodyFromJson(body, RecipeBody.class, objectMapper)
                .flatMap(recipeBody -> recipeService.updateModelWithVideos(images, videos, recipeBody, id, requestsUtils.extractAuthUser(exchange), clientId))
                .flatMap(m -> recipeReactiveResponseBuilder.toModel(m, RecipeController.class))
                .map(ResponseEntity::ok);
    }

    @PatchMapping(value = "/trainer/filtered/{trainerId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<RecipeResponse>>> getModelsTrainerFiltered(@RequestParam(required = false) String title,
                                                                                              @RequestParam(required = false) Boolean approved,
                                                                                              @RequestParam(required = false) DietType type,
                                                                                              @PathVariable Long trainerId,
                                                                                              @Valid @RequestBody PageableBody pageableBody,
                                                                                              ServerWebExchange exchange) {
        return recipeService.getRecipesFilteredTrainer(title, type, trainerId, pageableBody, requestsUtils.extractAuthUser(exchange), approved)
                .flatMap(m -> recipeReactiveResponseBuilder.toModelPageable(m, RecipeController.class));
    }

    @PatchMapping(value = "/trainer/filteredWithCount/{trainerId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithEntityCount<CustomEntityModel<RecipeResponse>>>> getModelsTrainerFilteredWithCount(@RequestParam(required = false) String title,
                                                                                                                                @RequestParam(required = false) Boolean approved,
                                                                                                                                @RequestParam(required = false) DietType type,
                                                                                                                                @PathVariable Long trainerId,
                                                                                                                                @Valid @RequestBody PageableBody pageableBody,
                                                                                                                                ServerWebExchange exchange) {
        return recipeService.getRecipesFilteredTrainerWithCount(title, type, trainerId, pageableBody, requestsUtils.extractAuthUser(exchange), approved)
                .flatMap(m -> recipeReactiveResponseBuilder.toModelWithEntityCountPageable(m, RecipeController.class));
    }

    @GetMapping(value = "/ingredients/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<IngredientNutritionalFactResponseWithCount> findIngredientsByRecipe(@PathVariable Long id, ServerWebExchange exchange) {
        return ingredientQuantityService.findAllByRecipeId(id, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping(value = "/internal/validIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<Void>> validIdsUser(@RequestParam List<Long> ids, ServerWebExchange exchange) {
        return recipeService.validIds(ids, requestsUtils.extractAuthUser(exchange))
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @GetMapping(value = "/dietType", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<DietType>> determineMostRestrictiveDietType(@RequestParam List<Long> ids) {
        return recipeService.determineMostRestrictiveDietType(ids)
                .map(ResponseEntity::ok);
    }

    @Override
    @PutMapping
    public Mono<ResponseEntity<Void>> validIds(List<Long> ids) {
        return null;
    }

    @Override
    @GetMapping(value = "/internal/getByIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Flux<RecipeResponse> getByIds(@RequestParam List<Long> ids) {
        return recipeService.getModelsByIds(ids);
    }


    @GetMapping(value = "/internal/withUser/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<RecipeResponse>>> getModelByIdWithUserInternal(@PathVariable Long id) {
        return recipeService.getModelByIdWithUserInternal(id)
                .flatMap(m -> recipeReactiveResponseBuilder.toModelWithUser(m, RecipeController.class))
                .map(ResponseEntity::ok);
    }
}
