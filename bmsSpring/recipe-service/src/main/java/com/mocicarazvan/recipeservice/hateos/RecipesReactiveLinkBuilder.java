package com.mocicarazvan.recipeservice.hateos;

import com.mocicarazvan.recipeservice.controllers.RecipeController;
import com.mocicarazvan.recipeservice.dtos.RecipeBody;
import com.mocicarazvan.recipeservice.dtos.RecipeResponse;
import com.mocicarazvan.recipeservice.mappers.RecipeMapper;
import com.mocicarazvan.recipeservice.models.Recipe;
import com.mocicarazvan.recipeservice.repositories.RecipeRepository;
import com.mocicarazvan.recipeservice.services.RecipeService;
import com.mocicarazvan.templatemodule.hateos.controller.generics.ApproveReactiveLinkBuilder;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.util.List;

public class RecipesReactiveLinkBuilder extends ApproveReactiveLinkBuilder<Recipe, RecipeBody, RecipeResponse, RecipeRepository, RecipeMapper,
        RecipeService, RecipeController> {

    @Override
    public List<WebFluxLinkBuilder.WebFluxLink> createModelLinks(RecipeResponse recipeResponse, Class<RecipeController> c) {
        List<WebFluxLinkBuilder.WebFluxLink> links = super.createModelLinks(recipeResponse, c);

        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getAllModelsFiltered(null, null, null, null, null, null)).withRel("getAllModelsFiltered"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getAllModelsFilteredWithCount(null, null, null, null, null, null)).withRel("getModelsFilteredWithCount"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getAllModelsFilteredWithUser(null, null, null, null, null, null)).withRel("getAllModelsFilteredWithUser"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).createWithVideos(null, null, null, null, null)).withRel("createWithVideos"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).updateWithVideos(null, null, null, null, null, null)).withRel("updateWithVideos"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getModelsTrainerFiltered(null, null, null, null, null, null)).withRel("getModelsTrainerFiltered"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getModelsTrainerFilteredWithCount(null, null, null, null, null, null)).withRel("getModelsTrainerFilteredWithCount"));


        return links;
    }
}
