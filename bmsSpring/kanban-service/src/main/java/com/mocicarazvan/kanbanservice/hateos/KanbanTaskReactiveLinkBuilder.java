package com.mocicarazvan.kanbanservice.hateos;

import com.mocicarazvan.kanbanservice.controllers.KanbanTaskController;
import com.mocicarazvan.kanbanservice.dtos.tasks.GroupedKanbanTask;
import com.mocicarazvan.kanbanservice.dtos.tasks.KanbanTaskBody;
import com.mocicarazvan.kanbanservice.dtos.tasks.KanbanTaskResponse;
import com.mocicarazvan.kanbanservice.mappers.KanbanTaskMapper;
import com.mocicarazvan.kanbanservice.models.KanbanTask;
import com.mocicarazvan.kanbanservice.repositories.KanbanTaskRepository;
import com.mocicarazvan.kanbanservice.services.KanbanTaskService;
import com.mocicarazvan.templatemodule.hateos.controller.generics.ManyToOneUserReactiveLinkBuilder;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.util.List;

public class KanbanTaskReactiveLinkBuilder extends ManyToOneUserReactiveLinkBuilder
        <KanbanTask, KanbanTaskBody, KanbanTaskResponse, KanbanTaskRepository, KanbanTaskMapper, KanbanTaskService, KanbanTaskController> {

    @Override
    public List<WebFluxLinkBuilder.WebFluxLink> createModelLinks(KanbanTaskResponse kanbanTaskResponse, Class<com.mocicarazvan.kanbanservice.controllers.KanbanTaskController> c) {
        List<WebFluxLinkBuilder.WebFluxLink> links = super.createModelLinks(kanbanTaskResponse, c);
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getByColumnId(kanbanTaskResponse.getColumnId())).withRel("getByColumnId"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).reindex(new GroupedKanbanTask(), null)).withRel("reindex"));
        return links;
    }
}

