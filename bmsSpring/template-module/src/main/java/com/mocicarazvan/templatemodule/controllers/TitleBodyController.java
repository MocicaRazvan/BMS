package com.mocicarazvan.templatemodule.controllers;


import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserLikesAndDislikesEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.TitleBody;
import com.mocicarazvan.templatemodule.repositories.TitleBodyRepository;
import com.mocicarazvan.templatemodule.services.TitleBodyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface TitleBodyController<MODEL extends TitleBody, BODY, RESPONSE extends WithUserDto,
        S extends TitleBodyRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>,
        G extends TitleBodyService<MODEL, BODY, RESPONSE, S, M>>
        extends ManyToOneUserController<MODEL, BODY, RESPONSE, S, M, G> {

    @PatchMapping("/like/{id}")
    Mono<ResponseEntity<CustomEntityModel<RESPONSE>>> likeModel(@PathVariable Long id, ServerWebExchange exchange);

    @PatchMapping("/dislike/{id}")
    Mono<ResponseEntity<CustomEntityModel<RESPONSE>>> dislikeModel(@PathVariable Long id, ServerWebExchange exchange);

    @GetMapping("/withUser/withReactions/{id}")
    Mono<ResponseEntity<ResponseWithUserLikesAndDislikesEntity<RESPONSE>>> getModelsWithUserAndReaction(@PathVariable Long id, ServerWebExchange exchange);
}
