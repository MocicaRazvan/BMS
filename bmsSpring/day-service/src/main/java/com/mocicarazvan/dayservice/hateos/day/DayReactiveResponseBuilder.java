package com.mocicarazvan.dayservice.hateos.day;


import com.mocicarazvan.dayservice.controllers.DayController;
import com.mocicarazvan.dayservice.dtos.day.DayResponse;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveResponseBuilder;
import org.springframework.stereotype.Component;

@Component
public class DayReactiveResponseBuilder extends ReactiveResponseBuilder<DayResponse, DayController> {

    public DayReactiveResponseBuilder() {
        super(new DayReactiveLinkBuilder());
    }
}
