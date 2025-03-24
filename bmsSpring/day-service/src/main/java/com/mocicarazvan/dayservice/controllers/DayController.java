package com.mocicarazvan.dayservice.controllers;

import com.mocicarazvan.dayservice.dtos.day.DayBody;
import com.mocicarazvan.dayservice.dtos.day.DayBodyWithMeals;
import com.mocicarazvan.dayservice.dtos.day.DayResponse;
import com.mocicarazvan.dayservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.dayservice.enums.DayType;
import com.mocicarazvan.dayservice.hateos.day.DayReactiveResponseBuilder;
import com.mocicarazvan.dayservice.mappers.DayMapper;
import com.mocicarazvan.dayservice.models.Day;
import com.mocicarazvan.dayservice.repositories.DayRepository;
import com.mocicarazvan.dayservice.services.DayService;
import com.mocicarazvan.templatemodule.controllers.CountInParentController;
import com.mocicarazvan.templatemodule.controllers.TitleBodyController;
import com.mocicarazvan.templatemodule.controllers.ValidControllerIds;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/days")
@RequiredArgsConstructor
public class DayController implements TitleBodyController<
        Day, DayBody, DayResponse, DayRepository, DayMapper, DayService>, CountInParentController, ValidControllerIds<DayResponse> {

    private final DayService dayService;
    private final RequestsUtils requestsUtils;
    private final DayReactiveResponseBuilder dayReactiveResponseBuilder;

    @Override
    @GetMapping(value = "/internal/count/{childId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<EntityCount>> getCountInParent(@PathVariable Long childId) {
        return dayService.countInParent(childId).map(ResponseEntity::ok);
    }

    @Override
    @PatchMapping(value = "/like/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<DayResponse>>> likeModel(@PathVariable Long id, ServerWebExchange exchange) {
        return dayService.reactToModel(id, "like", requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> dayReactiveResponseBuilder.toModel(m, DayController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PatchMapping(value = "/dislike/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<DayResponse>>> dislikeModel(@PathVariable Long id, ServerWebExchange exchange) {
        return dayService.reactToModel(id, "dislike", requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> dayReactiveResponseBuilder.toModel(m, DayController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/withUser/withReactions/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserLikesAndDislikesEntity<DayResponse>>> getModelsWithUserAndReaction(@PathVariable Long id, ServerWebExchange exchange) {
        return dayService.getModelByIdWithUserLikesAndDislikes(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> dayReactiveResponseBuilder.toModelWithUserLikesAndDislikes(m, DayController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @DeleteMapping(value = "/delete/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<DayResponse>>> deleteModel(@PathVariable Long id, ServerWebExchange exchange) {
        return dayService.deleteModel(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> dayReactiveResponseBuilder.toModel(m, DayController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<DayResponse>>> getModelById(@PathVariable Long id, ServerWebExchange exchange) {
        return dayService.getModelById(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> dayReactiveResponseBuilder.toModel(m, DayController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/withUser/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<DayResponse>>> getModelByIdWithUser(@PathVariable Long id, ServerWebExchange exchange) {
        return dayService.getModelByIdWithUser(id, requestsUtils.extractAuthUser(exchange))
//                .log()
                .flatMap(m -> dayReactiveResponseBuilder.toModelWithUser(m, DayController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PutMapping(value = "/update/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<DayResponse>>> updateModel(@Valid @RequestBody DayBody dayBody, @PathVariable Long id, ServerWebExchange exchange) {
        return dayService.updateModel(id, dayBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> dayReactiveResponseBuilder.toModel(m, DayController.class))
                .map(ResponseEntity::ok);
    }

    // todo stergi cache by dayid in meal
    @PostMapping(value = "/update/meals/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<DayResponse>>> updateWithMeals(@Valid @RequestBody DayBodyWithMeals dayBodyWithMeals, @PathVariable Long id, ServerWebExchange exchange) {
        return dayService.updateWithMeals(id, dayBodyWithMeals, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> dayReactiveResponseBuilder.toModel(m, DayController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PatchMapping(value = "/byIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<DayResponse>>> getModelsByIdIn(@Valid @RequestBody PageableBody pageableBody, @RequestParam List<Long> ids) {
        return dayService.getModelsByIdInPageable(ids, pageableBody)
                .flatMapSequential(m -> dayReactiveResponseBuilder.toModelPageable(m, DayController.class));
    }

    @Override
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<DayResponse>>> createModel(@Valid @RequestBody DayBody dayBody, ServerWebExchange exchange) {
        return dayService.createModel(dayBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> dayReactiveResponseBuilder.toModel(m, DayController.class))
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/create/meals", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<DayResponse>>> createWithMeal(@Valid @RequestBody DayBodyWithMeals dayBodyWithMeals, ServerWebExchange exchange) {
        return dayService.createWithMeals(dayBodyWithMeals, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> dayReactiveResponseBuilder.toModel(m, DayController.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/admin/groupedByMonth")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyEntityGroup<CustomEntityModel<DayResponse>>> getModelGroupedByMonth(@RequestParam int month, ServerWebExchange exchange) {
        return dayService.getModelGroupedByMonth(month, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> dayReactiveResponseBuilder.toModelMonthlyEntityGroup(m, DayController.class));
    }

    @Override
    @GetMapping(value = "/internal/validIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<Void>> validIds(@RequestParam List<Long> ids) {
        return dayService.validIds(ids).then(Mono.just(ResponseEntity.noContent().build()));
    }

    @Override
    @GetMapping(value = "/internal/getByIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Flux<DayResponse> getByIds(@RequestParam List<Long> ids) {
        return dayService.getModelsByIds(ids);
    }

    @PatchMapping(value = "/filtered", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<DayResponse>>> getDaysFiltered(@RequestParam(required = false) String title,
                                                                                  @RequestParam(required = false) DayType type,
                                                                                  @RequestParam(required = false) List<Long> excludeIds,
                                                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtLowerBound,
                                                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtUpperBound,
                                                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtLowerBound,
                                                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtUpperBound,
                                                                                  @Valid @RequestBody PageableBody pageableBody,
                                                                                  @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
                                                                                  ServerWebExchange exchange) {
        return dayService.getDaysFiltered(title, type, excludeIds,
                        createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                        pageableBody, requestsUtils.extractAuthUser(exchange), admin)
                .flatMapSequential(m -> dayReactiveResponseBuilder.toModelPageable(m, DayController.class));
    }

    @PatchMapping(value = "/internal/filtered/byIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<DayResponse>>> getDaysFilteredByIds(@RequestParam(required = false) String title,
                                                                                       @RequestParam(required = false) DayType type,
                                                                                       @RequestParam(required = false) List<Long> excludeIds,
                                                                                       @NotEmpty @RequestParam List<Long> ids,
                                                                                       @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtLowerBound,
                                                                                       @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtUpperBound,
                                                                                       @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtLowerBound,
                                                                                       @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtUpperBound,
                                                                                       @Valid @RequestBody PageableBody pageableBody,
                                                                                       @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
                                                                                       ServerWebExchange exchange) {
        return dayService.getDaysFilteredByIds(title, type, ids, excludeIds,
                        createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                        pageableBody, requestsUtils.extractAuthUser(exchange), admin)
                .flatMapSequential(m -> dayReactiveResponseBuilder.toModelPageable(m, DayController.class));
    }

    @PatchMapping(value = "/filtered/withUser", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithUserDtoEntity<DayResponse>>> getDaysFilteredWithUser(@RequestParam(required = false) String title,
                                                                                                  @RequestParam(required = false) DayType type,
                                                                                                  @RequestParam(required = false) List<Long> excludeIds,
                                                                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtLowerBound,
                                                                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtUpperBound,
                                                                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtLowerBound,
                                                                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtUpperBound,
                                                                                                  @Valid @RequestBody PageableBody pageableBody,
                                                                                                  @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
                                                                                                  ServerWebExchange exchange) {
        return dayService.getDaysFilteredWithUser(title, type, excludeIds,
                        createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                        pageableBody, requestsUtils.extractAuthUser(exchange), admin)
                .flatMapSequential(m -> dayReactiveResponseBuilder.toModelWithUserPageable(m, DayController.class));
    }

    @PatchMapping(value = "/filteredWithCount", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithEntityCount<CustomEntityModel<DayResponse>>>> getDaysFilteredWithCount(@RequestParam(required = false) String title,
                                                                                                                    @RequestParam(required = false) DayType type,
                                                                                                                    @RequestParam(required = false) List<Long> excludeIds,
                                                                                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtLowerBound,
                                                                                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtUpperBound,
                                                                                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtLowerBound,
                                                                                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtUpperBound,
                                                                                                                    @Valid @RequestBody PageableBody pageableBody,
                                                                                                                    @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
                                                                                                                    ServerWebExchange exchange) {
        return dayService.getDaysFilteredWithCount(title, type, excludeIds,
                        createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                        pageableBody, requestsUtils.extractAuthUser(exchange), admin)
                .flatMapSequential(m -> dayReactiveResponseBuilder.toModelWithEntityCountPageable(m, DayController.class));
    }

    @PatchMapping(value = "/trainer/filtered/{trainerId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<DayResponse>>> getDaysFilteredTrainer(@RequestParam(required = false) String title,
                                                                                         @RequestParam(required = false) DayType type,
                                                                                         @RequestParam(required = false) List<Long> excludeIds,
                                                                                         @PathVariable Long trainerId,
                                                                                         @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtLowerBound,
                                                                                         @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtUpperBound,
                                                                                         @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtLowerBound,
                                                                                         @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtUpperBound,
                                                                                         @Valid @RequestBody PageableBody pageableBody,
                                                                                         ServerWebExchange exchange) {
        return dayService.getDaysFilteredTrainer(title, type, excludeIds,
                        createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                        pageableBody, requestsUtils.extractAuthUser(exchange), trainerId)
                .flatMapSequential(m -> dayReactiveResponseBuilder.toModelPageable(m, DayController.class));
    }

    @PatchMapping(value = "/trainer/filteredWithCount/{trainerId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithEntityCount<CustomEntityModel<DayResponse>>>> getDaysFilteredTrainerWithCount(@RequestParam(required = false) String title,
                                                                                                                           @RequestParam(required = false) DayType type,
                                                                                                                           @RequestParam(required = false) List<Long> excludeIds,
                                                                                                                           @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtLowerBound,
                                                                                                                           @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtUpperBound,
                                                                                                                           @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtLowerBound,
                                                                                                                           @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtUpperBound,
                                                                                                                           @PathVariable Long trainerId,
                                                                                                                           @Valid @RequestBody PageableBody pageableBody,
                                                                                                                           ServerWebExchange exchange) {
        return dayService.getDaysFilteredTrainerWithCount(title, type, excludeIds,
                        createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                        pageableBody, requestsUtils.extractAuthUser(exchange), trainerId)
                .flatMapSequential(m -> dayReactiveResponseBuilder.toModelWithEntityCountPageable(m, DayController.class));
    }

    @GetMapping(value = "/internal/recipe/{id}/{recipeId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    Mono<ResponseEntity<ResponseWithUserDtoEntity<RecipeResponse>>> getRecipeByIdWithUserInternal(@PathVariable Long id, @PathVariable Long recipeId, ServerWebExchange exchange) {
        return dayService.getRecipeByIdWithUserInternal(id, recipeId, requestsUtils.extractAuthUser(exchange))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/internal/withUser/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<DayResponse>>> getModelByIdWithUserInternal(@PathVariable Long id, ServerWebExchange exchange) {
        return dayService.getModelByIdWithUserInternal(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> dayReactiveResponseBuilder.toModelWithUser(m, DayController.class))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/seedEmbeddings")
    public Mono<ResponseEntity<List<String>>> seedEmbeddings() {
        return dayService.seedEmbeddings()
                .map(ResponseEntity::ok);
    }
}
