package com.mocicarazvan.kanbanservice.hateos;

import com.mocicarazvan.kanbanservice.controllers.KanbanColumnController;
import com.mocicarazvan.kanbanservice.dtos.columns.KanbanColumnResponse;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveResponseBuilder;
import org.springframework.stereotype.Component;

@Component
public class KanbanColumnReactiveResponseBuilder extends ReactiveResponseBuilder<KanbanColumnResponse, KanbanColumnController> {
    public KanbanColumnReactiveResponseBuilder() {
        super(new KanbanColumnReactiveLinkBuilder());
    }
}
