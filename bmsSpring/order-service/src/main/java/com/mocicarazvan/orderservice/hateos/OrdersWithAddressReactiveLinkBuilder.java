package com.mocicarazvan.orderservice.hateos;

import com.mocicarazvan.orderservice.controllers.OrderWithAddressController;
import com.mocicarazvan.orderservice.dtos.OrderDtoWithAddress;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveLinkBuilder;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.util.ArrayList;
import java.util.List;

public class OrdersWithAddressReactiveLinkBuilder implements ReactiveLinkBuilder<OrderDtoWithAddress, OrderWithAddressController> {
    @Override
    public List<WebFluxLinkBuilder.WebFluxLink> createModelLinks(OrderDtoWithAddress orderDtoWithAddress, Class<OrderWithAddressController> c) {
        List<WebFluxLinkBuilder.WebFluxLink> links = new ArrayList<>();
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getModelById(orderDtoWithAddress.getOrder().getId(), null)).withRel("getModelById"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getModelsFiltered(null, null, null, null, null)).withRel("getModelsFiltered"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getModelsFilteredUser(null, null, null, null, null, null)).withRel("getModelsFilteredUser"));
        return links;
    }
}
