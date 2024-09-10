package com.mocicarazvan.dayservice.hateos.day;

import com.mocicarazvan.dayservice.controllers.DayController;
import com.mocicarazvan.dayservice.dtos.day.DayBody;
import com.mocicarazvan.dayservice.dtos.day.DayResponse;
import com.mocicarazvan.dayservice.mappers.DayMapper;
import com.mocicarazvan.dayservice.models.Day;
import com.mocicarazvan.dayservice.repositories.DayRepository;
import com.mocicarazvan.dayservice.services.DayService;
import com.mocicarazvan.templatemodule.hateos.controller.generics.TitleBodyReactiveLinkBuilder;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.util.List;

public class DayReactiveLinkBuilder extends TitleBodyReactiveLinkBuilder<
        Day, DayBody, DayResponse, DayRepository, DayMapper, DayService, DayController
        > {
    @Override
    public List<WebFluxLinkBuilder.WebFluxLink> createModelLinks(DayResponse dayResponse, Class<DayController> c) {
        List<WebFluxLinkBuilder.WebFluxLink> links = super.createModelLinks(dayResponse, c);
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getDaysFiltered(null, null, null, null, null, null)).withRel("getDaysFiltered"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getDaysFilteredWithCount(null, null, null, null, null, null)).withRel("getDaysFilteredWithCount"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getDaysFilteredWithUser(null, null, null, null, null, null)).withRel("getDaysFilteredWithUser"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getDaysFilteredTrainer(null, null, null, null, null, null)).withRel("getDaysFilteredTrainer"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getDaysFilteredTrainerWithCount(null, null, null, null, null, null)).withRel("getDaysFilteredTrainerWithCount"));
        return links;

    }
}
