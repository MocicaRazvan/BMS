package com.mocicarazvan.ingredientservice.controllers;

import com.mocicarazvan.ingredientservice.dtos.IngredientResponse;
import com.mocicarazvan.ingredientservice.services.IngredientService;
import com.mocicarazvan.templatemodule.controllers.ValidControllerIds;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/ingredients")
@RequiredArgsConstructor
public class IngredientController implements ValidControllerIds<IngredientResponse> {

    private final IngredientService ingredientService;

    @Override
    @GetMapping(value = "/internal/validIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<Void>> validIds(@RequestParam List<Long> ids) {
        return ingredientService.validIds(ids)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @Override
    @GetMapping(value = "/internal/getByIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Flux<IngredientResponse> getByIds(@RequestParam List<Long> ids) {
        return ingredientService.getModelsByIds(ids);
    }


}
