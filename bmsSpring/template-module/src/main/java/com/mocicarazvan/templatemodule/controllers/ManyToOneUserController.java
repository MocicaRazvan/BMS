package com.mocicarazvan.templatemodule.controllers;


import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import com.mocicarazvan.templatemodule.services.ManyToOneUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


public interface ManyToOneUserController<MODEL extends ManyToOneUser, BODY, RESPONSE extends WithUserDto,
        S extends ManyToOneUserRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>,
        G extends ManyToOneUserService<MODEL, BODY, RESPONSE, S, M>> {


    @DeleteMapping("/delete/{id}")
    Mono<ResponseEntity<CustomEntityModel<RESPONSE>>> deleteModel(@PathVariable Long id, ServerWebExchange exchange);

    @GetMapping("/{id}")
    Mono<ResponseEntity<CustomEntityModel<RESPONSE>>> getModelById(@PathVariable Long id, ServerWebExchange exchange);

    @GetMapping("/withUser/{id}")
    Mono<ResponseEntity<ResponseWithUserDtoEntity<RESPONSE>>> getModelByIdWithUser(@PathVariable Long id, ServerWebExchange exchange);

    @PutMapping("/update/{id}")
    Mono<ResponseEntity<CustomEntityModel<RESPONSE>>> updateModel(@Valid @RequestBody BODY body,
                                                                  @PathVariable Long id, ServerWebExchange exchange);

    @PatchMapping("/byIds")
    @ResponseStatus(HttpStatus.OK)
    Flux<PageableResponse<CustomEntityModel<RESPONSE>>> getModelsByIdIn(@Valid @RequestBody PageableBody pageableBody,
                                                                        @RequestParam List<Long> ids);

    @PostMapping("/create")
    Mono<ResponseEntity<CustomEntityModel<RESPONSE>>> createModel(@Valid @RequestBody BODY body, ServerWebExchange exchange);

    @GetMapping("/admin/groupedByMonth")
    @ResponseStatus(HttpStatus.OK)
    Flux<MonthlyEntityGroup<CustomEntityModel<RESPONSE>>> getModelGroupedByMonth(@RequestParam int month, ServerWebExchange exchange);
}
