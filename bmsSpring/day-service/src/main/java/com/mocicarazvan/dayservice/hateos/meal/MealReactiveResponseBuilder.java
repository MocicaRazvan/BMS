package com.mocicarazvan.dayservice.hateos.meal;


import com.mocicarazvan.dayservice.controllers.MealController;
import com.mocicarazvan.dayservice.dtos.meal.MealResponse;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveResponseBuilder;
import org.springframework.stereotype.Component;

@Component
public class MealReactiveResponseBuilder extends ReactiveResponseBuilder<MealResponse, MealController> {

    public MealReactiveResponseBuilder() {
        super(new MealReactiveLinkBuilder());
    }
}
