package com.mocicarazvan.templatemodule.controllers;


import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.generic.TitleBodyDto;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.Approve;
import com.mocicarazvan.templatemodule.models.TitleBody;
import com.mocicarazvan.templatemodule.repositories.ApprovedRepository;
import com.mocicarazvan.templatemodule.services.ApprovedService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApproveController<MODEL extends Approve, BODY extends TitleBodyDto, RESPONSE extends WithUserDto,
        S extends ApprovedRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>,
        G extends ApprovedService<MODEL, BODY, RESPONSE, S, M>>

        extends TitleBodyImagesController<MODEL, BODY, RESPONSE, S, M, G> {


    @PatchMapping("/approved")
    @ResponseStatus(HttpStatus.OK)
    Flux<PageableResponse<CustomEntityModel<RESPONSE>>> getModelsApproved(@RequestParam(required = false) String title, @Valid @RequestBody PageableBody pageableBody, ServerWebExchange exchange);

    @PatchMapping("/withUser")
    @ResponseStatus(HttpStatus.OK)
    Flux<PageableResponse<ResponseWithUserDtoEntity<RESPONSE>>> getModelsWithUser(@RequestParam(required = false) String title,
                                                                                  @RequestParam(required = false) boolean approved,
                                                                                  @Valid @RequestBody PageableBody pageableBody,
                                                                                  ServerWebExchange exchange);

    @PatchMapping("/trainer/{trainerId}")
    @ResponseStatus(HttpStatus.OK)
    Flux<PageableResponse<CustomEntityModel<RESPONSE>>> getModelsTrainer(@RequestParam(required = false) String title, @RequestParam(required = false) Boolean approved, @Valid @RequestBody PageableBody pageableBody, @PathVariable Long trainerId, ServerWebExchange exchange);


    @PatchMapping("/admin/approve/{id}")
    Mono<ResponseEntity<ResponseWithUserDtoEntity<RESPONSE>>> approveModel(@PathVariable Long id,
                                                                           @RequestParam boolean approved,
                                                                           ServerWebExchange exchange);

    @PatchMapping("/admin")
    Flux<PageableResponse<CustomEntityModel<RESPONSE>>> getAllModelsAdmin(@RequestParam(required = false) String title, @Valid @RequestBody PageableBody pageableBody, ServerWebExchange exchange);

}
