package com.mocicarazvan.planservice.hateos;

import com.mocicarazvan.planservice.controllers.PlanController;
import com.mocicarazvan.planservice.dtos.PlanResponse;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveResponseBuilder;
import org.springframework.stereotype.Component;

@Component
public class PlansReactiveResponseBuilder extends ReactiveResponseBuilder<PlanResponse, PlanController> {

    public PlansReactiveResponseBuilder() {
        super(new PlansReactiveLinkBuilder());
    }
}
