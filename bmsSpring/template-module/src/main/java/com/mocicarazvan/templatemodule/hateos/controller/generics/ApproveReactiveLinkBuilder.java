package com.mocicarazvan.templatemodule.hateos.controller.generics;


import com.mocicarazvan.templatemodule.controllers.ApproveController;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.generic.TitleBodyDto;
import com.mocicarazvan.templatemodule.dtos.generic.TitleBodyUserDto;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveLinkBuilder;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.Approve;
import com.mocicarazvan.templatemodule.models.TitleBody;
import com.mocicarazvan.templatemodule.repositories.ApprovedRepository;
import com.mocicarazvan.templatemodule.services.ApprovedService;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.util.List;

public abstract class ApproveReactiveLinkBuilder<MODEL extends Approve, BODY extends TitleBodyDto, RESPONSE extends TitleBodyUserDto,
        S extends ApprovedRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>,
        G extends ApprovedService<MODEL, BODY, RESPONSE, S, M>,
        C extends ApproveController<MODEL, BODY, RESPONSE, S, M, G>
        >
        extends TitleBodyImagesReactiveLinkBuilder<MODEL, BODY, RESPONSE, S, M, G, C>
        implements ReactiveLinkBuilder<RESPONSE, C> {

    @Override
    public List<WebFluxLinkBuilder.WebFluxLink> createModelLinks(RESPONSE response, Class<C> c) {
        List<WebFluxLinkBuilder.WebFluxLink> links = super.createModelLinks(response, c);
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).approveModel(response.getId(), false, null)).withRel("approve"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getModelsTrainer(response.getTitle(), Boolean.TRUE,
                PageableBody.builder().page(0).size(10).build(), response.getUserId(), null)).withRel("models by trainer"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getModelsApproved(response.getTitle(),
                PageableBody.builder().page(0).size(10).build(), null)).withRel("approved models"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getAllModelsAdmin(response.getTitle(),
                PageableBody.builder().page(0).size(10).build(), null)).withRel("all models admin"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getModelsWithUser(response.getTitle(), true,
                PageableBody.builder().page(0).size(10).build(), null)).withRel("models with user"));
        return links;
    }
}
