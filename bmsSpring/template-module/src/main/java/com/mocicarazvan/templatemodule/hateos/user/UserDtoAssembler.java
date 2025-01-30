package com.mocicarazvan.templatemodule.hateos.user;


import com.mocicarazvan.templatemodule.controllers.UserController;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;


@RequiredArgsConstructor
public class UserDtoAssembler implements ReactiveRepresentationModelAssembler<UserDto> {


    private final Class<? extends UserController> userController;

    @Override
    public Mono<CustomEntityModel<UserDto>> toModel(UserDto entity) {
        return Mono.just(CustomEntityModel.<UserDto>builder()
                        .content(entity)
                        .build())
                .flatMap(model ->
                        WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(userController).getUser(entity.getId()))
                                .withSelfRel()
                                .toMono()
                                .doOnNext(model::add).then(Mono.just(model)))
                .flatMap(model ->
                        WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(userController).updateUser(entity.getId(), null, null, null))
                                .withRel("updateUser")
                                .toMono()
                                .doOnNext(model::add).then(Mono.just(model)))
                .flatMap(model ->
                        WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(userController).getAllUsers(
                                        PageableBody.builder()
                                                .page(0)
                                                .size(10)
                                                .sortingCriteria(Map.of("email", "asc"))
                                                .build(), "raz", Set.of(Role.ROLE_USER, Role.ROLE_TRAINER), Set.of(AuthProvider.GOOGLE), true, false,
                                        null, null, null, null))
                                .withRel(IanaLinkRelations.COLLECTION)
                                .toMono()
                                .doOnNext(model::add).then(Mono.just(model)))
                .flatMap(model ->
                        WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(userController).makeTrainer(entity.getId()))
                                .withRel("makeTrainer")
                                .toMono()
                                .doOnNext(model::add).then(Mono.just(model)))
                .flatMap(model ->
                        WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(userController).existsUserByIdAndRoleIn(entity.getId(), Set.of(Role.ROLE_USER, Role.ROLE_TRAINER)))
                                .withRel("existsUserByIdAndRoleIn")
                                .toMono()
                                .doOnNext(model::add).then(Mono.just(model)))
                ;
    }


}
