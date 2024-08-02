package com.mocicarazvan.kanbanservice.hateos;

import com.mocicarazvan.kanbanservice.controllers.KanbanColumnController;
import com.mocicarazvan.kanbanservice.dtos.columns.KanbanColumnBody;
import com.mocicarazvan.kanbanservice.dtos.columns.KanbanColumnResponse;
import com.mocicarazvan.kanbanservice.mappers.KanbanColumnMapper;
import com.mocicarazvan.kanbanservice.models.KanbanColumn;
import com.mocicarazvan.kanbanservice.repositories.KanbanColumnRepository;
import com.mocicarazvan.kanbanservice.services.KanbanColumnService;
import com.mocicarazvan.templatemodule.hateos.controller.generics.ManyToOneUserReactiveLinkBuilder;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.util.List;

public class KanbanColumnReactiveLinkBuilder extends ManyToOneUserReactiveLinkBuilder
        <KanbanColumn, KanbanColumnBody, KanbanColumnResponse, KanbanColumnRepository, KanbanColumnMapper, KanbanColumnService, KanbanColumnController> {

    @Override
    public List<WebFluxLinkBuilder.WebFluxLink> createModelLinks(KanbanColumnResponse kanbanColumnResponse, Class<KanbanColumnController> c) {
        List<WebFluxLinkBuilder.WebFluxLink> links = super.createModelLinks(kanbanColumnResponse, c);
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getAllByUserId(null)).withRel("getAllByUserId"));
        return links;
    }
}
