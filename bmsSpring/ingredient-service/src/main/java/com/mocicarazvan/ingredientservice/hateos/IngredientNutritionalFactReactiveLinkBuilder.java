package com.mocicarazvan.ingredientservice.hateos;

import com.mocicarazvan.ingredientservice.controllers.IngredientNutritionalFactController;
import com.mocicarazvan.ingredientservice.dtos.IngredientNutritionalFactResponse;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveLinkBuilder;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.util.ArrayList;
import java.util.List;

public class IngredientNutritionalFactReactiveLinkBuilder implements ReactiveLinkBuilder<IngredientNutritionalFactResponse, IngredientNutritionalFactController> {
    @Override
    public List<WebFluxLinkBuilder.WebFluxLink> createModelLinks(IngredientNutritionalFactResponse ingredientNutritionalFactResponse, Class<IngredientNutritionalFactController> c) {
        List<WebFluxLinkBuilder.WebFluxLink> links = new ArrayList<>();
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).deleteModel(1L, null)).withRel(LinkRelation.of("delete")));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getModelById(1L, null)).withRel(LinkRelation.of("getById")));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).updateModel(1L, null, null)).withRel(LinkRelation.of("update")));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getModelsFiltered("", true, null, null, null, null)).withRel(LinkRelation.of("getFiltered")));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).createModel(null, null)).withRel(LinkRelation.of("create")));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).alterDisplay(1L, true, null)).withRel(LinkRelation.of("alterDisplay")));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getAllModelsFilteredWithEntityCount("", true, null, null, null, null)).withRel(LinkRelation.of("getAllModelsFilteredWithEntityCount")));
        return links;
    }
}
