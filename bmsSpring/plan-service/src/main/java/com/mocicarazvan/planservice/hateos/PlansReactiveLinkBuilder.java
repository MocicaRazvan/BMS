package com.mocicarazvan.planservice.hateos;

import com.mocicarazvan.planservice.controllers.PlanController;
import com.mocicarazvan.planservice.dtos.PlanBody;
import com.mocicarazvan.planservice.dtos.PlanResponse;
import com.mocicarazvan.planservice.mappers.PlanMapper;
import com.mocicarazvan.planservice.models.Plan;
import com.mocicarazvan.planservice.repositories.PlanRepository;
import com.mocicarazvan.planservice.services.PlanService;
import com.mocicarazvan.templatemodule.hateos.controller.generics.ApproveReactiveLinkBuilder;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.util.List;

public class PlansReactiveLinkBuilder extends ApproveReactiveLinkBuilder<
        Plan, PlanBody, PlanResponse, PlanRepository, PlanMapper, PlanService, PlanController> {

    @Override
    public List<WebFluxLinkBuilder.WebFluxLink> createModelLinks(PlanResponse planResponse, Class<PlanController> c) {
        List<WebFluxLinkBuilder.WebFluxLink> links = super.createModelLinks(planResponse, c);
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getAllPlansFiltered(null, null, null, null, null, null, null, null, null, null, null, null, null)).withRel("getPlansFiltered"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getAllPlansFilteredWithCount(null, null, null, null, null, null, null, null, null, null, null, null, null)).withRel("getPlansFilteredWithCount"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getAllPlansFilteredWithUser(null, null, null, null, null, null, null, null, null, null, null, null, null)).withRel("getPlansFilteredWithUser"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getAllPlansFilteredTrainer(null, null, null, null, null, null, null, null, null, null, null, null)).withRel("getPlansFilteredTrainer"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getAllPlansFilteredTrainerWithCount(null, null, null, null, null, null, null, null, null, null, null, null)).withRel("getPlansFilteredTrainerWithCount"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).toggleDisplay(null, false, null)).withRel("toggleDisplay"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getDaysByPlan(null, null)).withRel("getRecipesByPlan"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getDayByPlan(null, null, null)).withRel("getDayByPlan"));
        return links;
    }
}
