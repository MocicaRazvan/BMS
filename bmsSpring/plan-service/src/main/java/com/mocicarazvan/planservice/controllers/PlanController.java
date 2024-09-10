package com.mocicarazvan.planservice.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.planservice.dtos.PlanBody;
import com.mocicarazvan.planservice.dtos.PlanResponse;
import com.mocicarazvan.planservice.dtos.dayClient.DayResponse;
import com.mocicarazvan.planservice.dtos.dayClient.MealResponse;
import com.mocicarazvan.planservice.dtos.dayClient.RecipeResponse;
import com.mocicarazvan.planservice.dtos.dayClient.collect.FullDayResponse;
import com.mocicarazvan.planservice.enums.DietType;
import com.mocicarazvan.planservice.enums.ObjectiveType;
import com.mocicarazvan.planservice.hateos.PlansReactiveResponseBuilder;
import com.mocicarazvan.planservice.mappers.PlanMapper;
import com.mocicarazvan.planservice.models.Plan;
import com.mocicarazvan.planservice.repositories.PlanRepository;
import com.mocicarazvan.planservice.services.PlanService;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCache;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheApproveFilterKey;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.controllers.ApproveController;
import com.mocicarazvan.templatemodule.controllers.CountInParentController;
import com.mocicarazvan.templatemodule.controllers.ValidControllerIds;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
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
@RequestMapping("/plans")
public class PlanController implements ApproveController<Plan, PlanBody, PlanResponse, PlanRepository, PlanMapper, PlanService>,
        CountInParentController, ValidControllerIds<PlanResponse> {

    private final PlanService planService;
    private final RequestsUtils requestsUtils;
    private final PlansReactiveResponseBuilder plansReactiveResponseBuilder;
    private final ObjectMapper objectMapper;

    @Override
    @PatchMapping(value = "/approved", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<PlanResponse>>> getModelsApproved(
            @RequestParam(required = false) String title, @Valid @RequestBody PageableBody pageableBody, ServerWebExchange exchange) {
        return planService.getModelsApproved(title, pageableBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> plansReactiveResponseBuilder.toModelPageable(m, PlanController.class));
    }

    @Override
    @PatchMapping("/withUser")
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>> getModelsWithUser(
            @RequestParam(required = false) String title,
            @RequestParam(name = "approved", required = false, defaultValue = "true") boolean approved,
            @Valid @RequestBody PageableBody pageableBody,
            @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
            ServerWebExchange exchange
    ) {
        return planService.getModelsWithUser(title, pageableBody, requestsUtils.extractAuthUser(exchange), approved)
                .flatMap(m -> plansReactiveResponseBuilder.toModelWithUserPageable(m, PlanController.class));
    }

    @Override
    @PatchMapping(value = "/trainer/{trainerId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<PlanResponse>>> getModelsTrainer(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean approved,
            @Valid @RequestBody PageableBody pageableBody,
            @PathVariable Long trainerId,
            ServerWebExchange exchange) {
        return planService.getModelsTrainer(title, trainerId, pageableBody, requestsUtils.extractAuthUser(exchange), approved)
                .flatMap(m -> plansReactiveResponseBuilder.toModelPageable(m, PlanController.class));
    }

    @Override
    @PatchMapping(value = "/admin/approve/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<PlanResponse>>> approveModel(@PathVariable Long id,
                                                                                      @RequestParam boolean approved,
                                                                                      ServerWebExchange exchange) {
        return planService.approveModel(id, requestsUtils.extractAuthUser(exchange), approved)
                .flatMap(m -> plansReactiveResponseBuilder.toModelWithUser(m, PlanController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PatchMapping(value = "/admin", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<PlanResponse>>> getAllModelsAdmin(@RequestParam(required = false) String title,
                                                                                     @Valid @RequestBody PageableBody pageableBody,
                                                                                     ServerWebExchange exchange) {
        return planService.getAllModels(title, pageableBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> plansReactiveResponseBuilder.toModelPageable(m, PlanController.class));
    }

    @Override
    @GetMapping(value = "/internal/count/{childId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<EntityCount>> getCountInParent(@PathVariable Long childId) {
        return planService.countInParent(childId).map(ResponseEntity::ok);
    }

    @Override
    @DeleteMapping(value = "/delete/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PlanResponse>>> deleteModel(@PathVariable Long id, ServerWebExchange exchange) {
        return planService.deleteModel(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> plansReactiveResponseBuilder.toModel(m, PlanController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PlanResponse>>> getModelById(@PathVariable Long id, ServerWebExchange exchange) {
        return planService.getModelById(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> plansReactiveResponseBuilder.toModel(m, PlanController.class))
                .map(ResponseEntity::ok);
    }

    //todo in subscription tine minte si planurile cumparate dar si retetele din ele si fa overwrite la get with user la
    // protected to true daca e in ele
    // daca tii minte doar planurile in subscription
    // subscription iti da planul dupa id, dupa tot subscription iti da retele din plan daca l-ai cumparat si sunt valide
    @Override
    @GetMapping(value = "/withUser/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<PlanResponse>>> getModelByIdWithUser(@PathVariable Long id, ServerWebExchange exchange) {
        return planService.getModelByIdWithUser(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> plansReactiveResponseBuilder.toModelWithUser(m, PlanController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PutMapping(value = "/update/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PlanResponse>>> updateModel(@Valid @RequestBody PlanBody planBody, @PathVariable Long id, ServerWebExchange exchange) {
        return planService.updateModel(id, planBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> plansReactiveResponseBuilder.toModel(m, PlanController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PatchMapping(value = "/byIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<PlanResponse>>> getModelsByIdIn(@Valid @RequestBody PageableBody pageableBody,
                                                                                   @RequestParam List<Long> ids) {
        return planService.getModelsByIdInPageable(ids, pageableBody)
                .flatMap(m -> plansReactiveResponseBuilder.toModelPageable(m, PlanController.class));
    }

    @Override
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PlanResponse>>> createModel(@Valid @RequestBody PlanBody planBody, ServerWebExchange exchange) {
        return planService.createModel(planBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> plansReactiveResponseBuilder.toModel(m, PlanController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/admin/groupedByMonth")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyEntityGroup<CustomEntityModel<PlanResponse>>> getModelGroupedByMonth(@RequestParam int month, ServerWebExchange exchange) {
        return
                planService.getModelGroupedByMonth(month, requestsUtils.extractAuthUser(exchange))
                        .flatMap(m -> plansReactiveResponseBuilder.toModelMonthlyEntityGroup(m, PlanController.class));
    }

    @Override
    @PatchMapping(value = "/like/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PlanResponse>>> likeModel(@PathVariable Long id, ServerWebExchange exchange) {
        return planService.reactToModel(id, "like", requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> plansReactiveResponseBuilder.toModel(m, PlanController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PatchMapping(value = "/dislike/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PlanResponse>>> dislikeModel(@PathVariable Long id, ServerWebExchange exchange) {
        return planService.reactToModel(id, "dislike", requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> plansReactiveResponseBuilder.toModel(m, PlanController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/withUser/withReactions/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserLikesAndDislikesEntity<PlanResponse>>> getModelsWithUserAndReaction(@PathVariable Long id, ServerWebExchange exchange) {
        return planService.getModelByIdWithUserLikesAndDislikes(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> plansReactiveResponseBuilder.toModelWithUserLikesAndDislikes(m, PlanController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PostMapping(value = "/createWithImages", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<CustomEntityModel<PlanResponse>>> createModelWithImages(@RequestPart("files") Flux<FilePart> files,
                                                                                       @RequestPart("body") String body,
                                                                                       @RequestParam("clientId") String clientId,
                                                                                       ServerWebExchange exchange) {
        return requestsUtils.getBodyFromJson(body, PlanBody.class, objectMapper)
                .flatMap(planBody -> planService.createModel(files, planBody, requestsUtils.extractAuthUser(exchange), clientId)
                        .flatMap(m -> plansReactiveResponseBuilder.toModel(m, PlanController.class)))
                .map(ResponseEntity::ok);
    }

    @Override
    @PostMapping(value = "/updateWithImages/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PlanResponse>>> updateModelWithImages(@RequestPart("files") Flux<FilePart> files,
                                                                                       @RequestPart("body") String body,
                                                                                       @RequestParam("clientId") String clientId,
                                                                                       @PathVariable Long id,
                                                                                       ServerWebExchange exchange) {
        return requestsUtils.getBodyFromJson(body, PlanBody.class, objectMapper)
                .flatMap(planBody -> planService.updateModelWithImagesGetOriginalApproved(files, id, planBody, requestsUtils.extractAuthUser(exchange), clientId)
                        .flatMap(m -> plansReactiveResponseBuilder.toModelWithPair(m, PlanController.class))
                        .map(Pair::getFirst)
                )
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/internal/validIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<Void>> validIds(@RequestParam List<Long> ids) {
        return planService.validIds(ids)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @Override
    @GetMapping(value = "/internal/getByIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Flux<PlanResponse> getByIds(@RequestParam List<Long> ids) {
        return planService.getModelsByIds(ids);
    }

    @PatchMapping(value = "/filtered", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<PlanResponse>>> getAllPlansFiltered(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean approved,
            @RequestParam(required = false) Boolean display,
            @RequestParam(required = false) DietType type,
            @RequestParam(required = false) ObjectiveType objective,
            @RequestParam(required = false) List<Long> excludeIds,
            @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
            @Valid @RequestBody PageableBody pageableBody,
            ServerWebExchange exchange
    ) {
        return planService.getPlansFiltered(title, approved, display, type, objective, excludeIds, pageableBody, requestsUtils.extractAuthUser(exchange), admin)
                .flatMap(m -> plansReactiveResponseBuilder.toModelPageable(m, PlanController.class));
    }

    @PatchMapping(value = "/filteredWithCount", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithEntityCount<CustomEntityModel<PlanResponse>>>> getAllPlansFilteredWithCount(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean approved,
            @RequestParam(required = false) Boolean display,
            @RequestParam(required = false) DietType type,
            @RequestParam(required = false) List<Long> excludeIds,
            @RequestParam(required = false) ObjectiveType objective,
            @Valid @RequestBody PageableBody pageableBody,
            @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
            ServerWebExchange exchange
    ) {
        return planService.getPlansFilteredWithCount(title, approved, display, type, objective, excludeIds, pageableBody, requestsUtils.extractAuthUser(exchange), admin)
                .flatMap(m -> plansReactiveResponseBuilder.toModelWithEntityCountPageable(m, PlanController.class));

    }

    @PatchMapping(value = "/filtered/withUser", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>> getAllPlansFilteredWithUser(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean approved,
            @RequestParam(required = false) Boolean display,
            @RequestParam(required = false) DietType type,
            @RequestParam(required = false) List<Long> excludeIds,
            @RequestParam(required = false) ObjectiveType objective,
            @Valid @RequestBody PageableBody pageableBody,
            @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
            ServerWebExchange exchange
    ) {
        return planService.getPlansFilteredWithUser(title, approved, display, type, objective, excludeIds, pageableBody, requestsUtils.extractAuthUser(exchange), admin)
                .flatMap(m -> plansReactiveResponseBuilder.toModelWithUserPageable(m, PlanController.class));

    }

    @PatchMapping(value = "/trainer/filtered/{trainerId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<PlanResponse>>> getAllPlansFilteredTrainer(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean approved,
            @RequestParam(required = false) Boolean display,
            @RequestParam(required = false) DietType type,
            @RequestParam(required = false) ObjectiveType objective,
            @PathVariable Long trainerId,
            @Valid @RequestBody PageableBody pageableBody,
            ServerWebExchange exchange
    ) {
        return planService.getPlansFilteredTrainer(title, approved, display, type, objective, pageableBody, requestsUtils.extractAuthUser(exchange), trainerId)
                .flatMap(m -> plansReactiveResponseBuilder.toModelPageable(m, PlanController.class));
    }

    @PatchMapping(value = "/trainer/filteredWithCount/{trainerId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithEntityCount<CustomEntityModel<PlanResponse>>>> getAllPlansFilteredTrainerWithCount(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean approved,
            @RequestParam(required = false) Boolean display,
            @RequestParam(required = false) DietType type,
            @RequestParam(required = false) ObjectiveType objective,
            @PathVariable Long trainerId,
            @Valid @RequestBody PageableBody pageableBody,
            ServerWebExchange exchange
    ) {
        return planService.getPlansFilteredTrainerWithCount(title, approved, display, type, objective, pageableBody, requestsUtils.extractAuthUser(exchange), trainerId)
                .flatMap(m -> plansReactiveResponseBuilder.toModelWithEntityCountPageable(m, PlanController.class));
    }

    @PatchMapping(value = "/alterDisplay/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<PlanResponse>>> toggleDisplay(@PathVariable Long id,
                                                                               @RequestParam boolean display,
                                                                               ServerWebExchange exchange) {
        return planService.toggleDisplay(id, display, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> plansReactiveResponseBuilder.toModel(m, PlanController.class))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/days/full/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<FullDayResponse> getFullDaysByPlan(@PathVariable Long id, ServerWebExchange exchange) {
        return planService.getFullDaysByPlan(id, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping(value = "/days/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<DayResponse> getDaysByPlan(@PathVariable Long id, ServerWebExchange exchange) {
        return planService.getDaysByPlan(id, requestsUtils.extractAuthUser(exchange));
    }


    @GetMapping(value = "/days/{id}/{dayId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<FullDayResponse>> getDayByPlan(@PathVariable Long id, @PathVariable Long dayId, ServerWebExchange exchange) {
        return planService.getDayByPlan(id, dayId, requestsUtils.extractAuthUser(exchange))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/internal/days/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<FullDayResponse> getDaysByPlanInternal(@PathVariable Long id, ServerWebExchange exchange) {
        return planService.getDaysByPlanInternal(id, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping(value = "/internal/days/full/{id}/{dayId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<FullDayResponse>> getFullDayByPlanInternal(@PathVariable Long id,
                                                                          @PathVariable Long dayId,
                                                                          ServerWebExchange exchange) {
        return planService.getDayByPlanInternal(id, dayId, requestsUtils.extractAuthUser(exchange))
                .map(ResponseEntity::ok);
    }


    @PatchMapping(value = "/internal/filtered/withUser", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>> getAllPlansFilteredWithUserByIds(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DietType type,
            @RequestParam(required = false) ObjectiveType objective,
            @RequestParam List<Long> ids,
            @Valid @RequestBody PageableBody pageableBody,
            ServerWebExchange exchange
    ) {
        return planService.getPlansFilteredWithUserByIds(title, type, objective, pageableBody, ids, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> plansReactiveResponseBuilder.toModelWithUserPageable(m, PlanController.class));
    }


    @GetMapping(value = "/internal/withUser/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<PlanResponse>>> getModelByIdWithUserInternal(@PathVariable Long id, ServerWebExchange exchange) {
        return planService.getModelByIdWithUserInternal(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> plansReactiveResponseBuilder.toModelWithUser(m, PlanController.class))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/internal/trainer/{trainerId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Flux<PlanResponse> getModelsTrainerInternal(
            @PathVariable Long trainerId,
            ServerWebExchange exchange) {
        return planService.getModelsTrainerInternal(trainerId, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping(value = "/internal/days/recipe/{id}/{dayId}/{recipeId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    Mono<ResponseEntity<ResponseWithUserDtoEntity<RecipeResponse>>> getRecipeByIdWithUserInternal(@PathVariable Long id, @PathVariable Long dayId,
                                                                                                  @PathVariable Long recipeId, ServerWebExchange exchange) {
        return planService.getRecipeByIdWithUserInternal(id, dayId, recipeId, requestsUtils.extractAuthUser(exchange))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/internal/days/{id}/{dayId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<DayResponse>>> getDayByIdWithUserInternal(@PathVariable Long id,
                                                                                                   @PathVariable Long dayId,
                                                                                                   ServerWebExchange exchange) {
        return planService.getDayByIdWithUserInternal(id, dayId, requestsUtils.extractAuthUser(exchange))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/internal/days/meals/{id}/{dayId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<CustomEntityModel<MealResponse>> getMealsByDayInternalEntity(@PathVariable Long dayId,
                                                                             @PathVariable Long id,
                                                                             ServerWebExchange exchange) {
        return planService.getMealsByDayInternal(id, dayId, requestsUtils.extractAuthUser(exchange));
    }

}
