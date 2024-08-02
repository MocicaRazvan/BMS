package com.mocicarazvan.kanbanservice.hateos;

import com.mocicarazvan.kanbanservice.controllers.KanbanTaskController;
import com.mocicarazvan.kanbanservice.dtos.tasks.KanbanTaskResponse;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveResponseBuilder;
import org.springframework.stereotype.Component;

@Component
public class KanbanTaskReactiveResponseBuilder extends ReactiveResponseBuilder<KanbanTaskResponse, KanbanTaskController> {
    public KanbanTaskReactiveResponseBuilder() {
        super(new KanbanTaskReactiveLinkBuilder());
    }
}
