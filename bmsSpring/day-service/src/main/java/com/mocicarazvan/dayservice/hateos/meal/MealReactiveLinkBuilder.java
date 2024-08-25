package com.mocicarazvan.dayservice.hateos.meal;

import com.mocicarazvan.dayservice.controllers.MealController;
import com.mocicarazvan.dayservice.dtos.meal.MealBody;
import com.mocicarazvan.dayservice.dtos.meal.MealResponse;
import com.mocicarazvan.dayservice.mappers.MealMapper;
import com.mocicarazvan.dayservice.models.Meal;
import com.mocicarazvan.dayservice.repositories.MealRepository;
import com.mocicarazvan.dayservice.services.MealService;
import com.mocicarazvan.templatemodule.hateos.controller.generics.ManyToOneUserReactiveLinkBuilder;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.util.List;

public class MealReactiveLinkBuilder extends ManyToOneUserReactiveLinkBuilder<
        Meal, MealBody, MealResponse, MealRepository, MealMapper, MealService, MealController
        > {

    @Override
    public List<WebFluxLinkBuilder.WebFluxLink> createModelLinks(MealResponse mealResponse, Class<MealController> c) {
        List<WebFluxLinkBuilder.WebFluxLink> links = super.createModelLinks(mealResponse, c);
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getRecipesByMeal(mealResponse.getId(), null)).withRel("getRecipesByMeal"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getMealsByDay(mealResponse.getDayId(), null)).withRel("getMealsByDay"));
        return links;
    }
}
