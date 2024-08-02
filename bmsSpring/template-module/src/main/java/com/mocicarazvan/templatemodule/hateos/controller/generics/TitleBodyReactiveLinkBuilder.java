package com.mocicarazvan.templatemodule.hateos.controller.generics;


import com.mocicarazvan.templatemodule.controllers.TitleBodyController;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveLinkBuilder;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.TitleBody;
import com.mocicarazvan.templatemodule.repositories.TitleBodyRepository;
import com.mocicarazvan.templatemodule.services.TitleBodyService;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.util.List;

public abstract class TitleBodyReactiveLinkBuilder<MODEL extends TitleBody, BODY, RESPONSE extends WithUserDto,
        S extends TitleBodyRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>,
        G extends TitleBodyService<MODEL, BODY, RESPONSE, S, M>,
        C extends TitleBodyController<MODEL, BODY, RESPONSE, S, M, G>>
        extends ManyToOneUserReactiveLinkBuilder<MODEL, BODY, RESPONSE, S, M, G, C>
        implements ReactiveLinkBuilder<RESPONSE, C> {

    @Override
    public List<WebFluxLinkBuilder.WebFluxLink> createModelLinks(RESPONSE response, Class<C> c) {
        List<WebFluxLinkBuilder.WebFluxLink> links = super.createModelLinks(response, c);
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).likeModel(response.getId(), null)).withRel("like"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).dislikeModel(response.getId(), null)).withRel("dislike"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getModelsWithUserAndReaction(response.getId(), null)).withRel("withUser/withReactions"));
        return links;
    }
}
