package com.mocicarazvan.templatemodule.hateos.controller.generics;


import com.mocicarazvan.templatemodule.controllers.ManyToOneUserController;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveLinkBuilder;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import com.mocicarazvan.templatemodule.services.ManyToOneUserService;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.util.ArrayList;
import java.util.List;

public abstract class ManyToOneUserReactiveLinkBuilder<
        MODEL extends ManyToOneUser, BODY, RESPONSE extends WithUserDto,
        S extends ManyToOneUserRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>,
        G extends ManyToOneUserService<MODEL, BODY, RESPONSE, S, M>,
        C extends ManyToOneUserController<MODEL, BODY, RESPONSE, S, M, G>>
        implements ReactiveLinkBuilder<RESPONSE, C> {


    @Override
    public List<WebFluxLinkBuilder.WebFluxLink> createModelLinks(RESPONSE response, Class<C> c) {
        List<WebFluxLinkBuilder.WebFluxLink> links = new ArrayList<>();
        links.add(WebFluxLinkBuilder.linkTo(
                WebFluxLinkBuilder.methodOn(c).deleteModel(response.getId(), null)).withRel("delete"));
        links.add(
                WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getModelById(response.getId(), null)).withSelfRel());
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getModelByIdWithUser(response.getId(), null)).withRel("withUser"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).updateModel(null, response.getId(), null)).withRel("update"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getModelsByIdIn(null, List.of(1L, 2L))).withRel("byIds"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).createModel(null, null)).withRel("create"));

        return links;
    }


}
