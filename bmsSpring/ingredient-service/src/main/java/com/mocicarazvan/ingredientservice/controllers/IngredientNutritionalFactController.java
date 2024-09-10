package com.mocicarazvan.ingredientservice.controllers;


import com.mocicarazvan.ingredientservice.dtos.IngredientNutritionalFactBody;
import com.mocicarazvan.ingredientservice.dtos.IngredientNutritionalFactResponse;
import com.mocicarazvan.ingredientservice.dtos.NutritionalFactBody;
import com.mocicarazvan.ingredientservice.enums.DietType;
import com.mocicarazvan.ingredientservice.hateos.IngredientNutritionalValueResponseBuilder;
import com.mocicarazvan.ingredientservice.repositories.ExtendedIngredientNutritionalFactRepository;
import com.mocicarazvan.ingredientservice.services.IngredientNutritionalFactService;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithEntityCount;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/ingredients")
@RequiredArgsConstructor
@Slf4j
public class IngredientNutritionalFactController {

    private final IngredientNutritionalFactService ingredientNutritionalFactService;
    private final IngredientNutritionalValueResponseBuilder ingredientNutritionalValueResponseBuilder;
    private final RequestsUtils requestsUtils;


    @DeleteMapping(value = "/delete/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<IngredientNutritionalFactResponse>>> deleteModel(
            @PathVariable Long id,
            ServerWebExchange exchange

    ) {
        return ingredientNutritionalFactService.deleteModel(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(i -> ingredientNutritionalValueResponseBuilder.toModel(i, IngredientNutritionalFactController.class))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<IngredientNutritionalFactResponse>>> getModelById(
            @PathVariable Long id,
            ServerWebExchange exchange

    ) {
        return ingredientNutritionalFactService.getModelById(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(i -> ingredientNutritionalValueResponseBuilder.toModel(i, IngredientNutritionalFactController.class))
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/update/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<IngredientNutritionalFactResponse>>> updateModel(
            @PathVariable Long id,
            @Valid @RequestBody IngredientNutritionalFactBody body,
            ServerWebExchange exchange

    ) {
        return ingredientNutritionalFactService.updateModel(id, body, requestsUtils.extractAuthUser(exchange))
                .flatMap(i -> ingredientNutritionalValueResponseBuilder.toModel(i, IngredientNutritionalFactController.class))
                .map(ResponseEntity::ok);
    }

    @PatchMapping(value = "/filtered", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<IngredientNutritionalFactResponse>>> getModelsFiltered(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean display,
            @RequestParam(required = false) DietType type,
            @Valid @RequestBody PageableBody pageableBody,
            @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
            ServerWebExchange exchange
    ) {
        return ingredientNutritionalFactService.getAllModelsFiltered(name, display, type, pageableBody, requestsUtils.extractAuthUser(exchange), admin)
                .flatMap(pr -> ingredientNutritionalValueResponseBuilder.toModelPageable(pr, IngredientNutritionalFactController.class));
    }

    @PatchMapping(value = "/filteredWithCount", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithEntityCount<CustomEntityModel<IngredientNutritionalFactResponse>>>> getAllModelsFilteredWithEntityCount(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean display,
            @RequestParam(required = false) DietType type,
            @Valid @RequestBody PageableBody pageableBody,
            @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
            ServerWebExchange exchange
    ) {
        return ingredientNutritionalFactService.getAllModelsFilteredWithEntityCount(name, display, type, pageableBody, requestsUtils.extractAuthUser(exchange), admin)
                .flatMap(pr -> ingredientNutritionalValueResponseBuilder.toModelWithEntityCountPageable(pr, IngredientNutritionalFactController.class));
    }

    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<IngredientNutritionalFactResponse>>> createModel(
            @Valid @RequestBody IngredientNutritionalFactBody body,
            ServerWebExchange exchange

    ) {
        return ingredientNutritionalFactService.createModel(body, requestsUtils.extractAuthUser(exchange))
                .flatMap(i -> ingredientNutritionalValueResponseBuilder.toModel(i, IngredientNutritionalFactController.class))
                .map(ResponseEntity::ok);
    }


    @PatchMapping(value = "/alterDisplay/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<IngredientNutritionalFactResponse>>> alterDisplay(
            @PathVariable Long id,
            @RequestParam Boolean display,
            ServerWebExchange exchange

    ) {
        return ingredientNutritionalFactService.alterDisplay(id, display, requestsUtils.extractAuthUser(exchange))
                .flatMap(i -> ingredientNutritionalValueResponseBuilder.toModel(i, IngredientNutritionalFactController.class))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/getByIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Flux<IngredientNutritionalFactResponse> getByIds(@RequestParam List<Long> ids) {
        return ingredientNutritionalFactService.getModelsByIds(ids);
    }

    @GetMapping(value = "/internal/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<IngredientNutritionalFactResponse>> getInternal(@PathVariable Long id, ServerWebExchange exchange) {
        return ingredientNutritionalFactService.getModelByIdInternal(id, requestsUtils.extractAuthUser(exchange)).map(ResponseEntity::ok);
    }
}
