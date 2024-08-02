package com.mocicarazvan.templatemodule.hateos.controller.generics;

import com.mocicarazvan.templatemodule.controllers.TitleBodyImagesController;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveLinkBuilder;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.TitleBodyImages;
import com.mocicarazvan.templatemodule.repositories.TitleBodyImagesRepository;
import com.mocicarazvan.templatemodule.services.TitleBodyImagesService;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.util.List;

public abstract class TitleBodyImagesReactiveLinkBuilder<MODEL extends TitleBodyImages, BODY, RESPONSE extends WithUserDto,
        S extends TitleBodyImagesRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>,
        G extends TitleBodyImagesService<MODEL, BODY, RESPONSE, S, M>,
        C extends TitleBodyImagesController<MODEL, BODY, RESPONSE, S, M, G>>
        extends TitleBodyReactiveLinkBuilder<MODEL, BODY, RESPONSE, S, M, G, C>
        implements ReactiveLinkBuilder<RESPONSE, C> {
    @Override
    public List<WebFluxLinkBuilder.WebFluxLink> createModelLinks(RESPONSE response, Class<C> c) {
        List<WebFluxLinkBuilder.WebFluxLink> links = super.createModelLinks(response, c);
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).createModelWithImages(null, null, null)).withRel("createWithImages"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).updateModelWithImages(null, null, null, null)).withRel("updateWithImages"));
        return links;
    }
}
