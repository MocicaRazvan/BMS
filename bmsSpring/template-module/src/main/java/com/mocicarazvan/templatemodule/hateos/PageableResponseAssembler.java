package com.mocicarazvan.templatemodule.hateos;


import com.mocicarazvan.templatemodule.controllers.UserController;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.templatemodule.enums.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Getter
@Slf4j
public abstract class PageableResponseAssembler<T, D extends ReactiveRepresentationModelAssembler<T>> {

    private final D itemAssembler;

    private final Class<? extends UserController> userController;


    public Mono<PageableResponse<CustomEntityModel<T>>> toModel(PageableResponse<T> pageableResponse, List<WebFluxLinkBuilder.WebFluxLink> additionalLinks) {
        return
                Flux.fromIterable(additionalLinks)
                        .flatMap(WebFluxLinkBuilder.WebFluxLink::toMono)
                        .collectList()
                        .flatMap(links ->
                                itemAssembler.toModel(pageableResponse.getContent())
                                        .map(c -> PageableResponse.<CustomEntityModel<T>>builder()
                                                .content(c)
                                                .pageInfo(pageableResponse.getPageInfo())
                                                .links(links)
                                                .build())
                        );
    }


    public Mono<PageableResponse<CustomEntityModel<T>>> toModel(PageableResponse<T> pageableResponse) {
        List<WebFluxLinkBuilder.WebFluxLink> links = new ArrayList<>();
        LinkedHashMap<String, String> sortingCriteria = new LinkedHashMap<>();
        sortingCriteria.put("email", "asc");
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(userController).getAllUsers(
                PageableBody.builder()
                        .page(0)
                        .size(10)
                        .sortingCriteria(sortingCriteria)
                        .build(), "raz", Set.of(Role.ROLE_USER, Role.ROLE_TRAINER), Set.of(AuthProvider.GOOGLE), true, false, null, null, null, null)).withSelfRel());
        return toModel(pageableResponse, links);
    }


}
