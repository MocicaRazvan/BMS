package com.mocicarazvan.cartservice.hateos;

import com.mocicarazvan.cartservice.controllers.UserCartController;
import com.mocicarazvan.cartservice.dtos.UserCartBody;
import com.mocicarazvan.cartservice.dtos.UserCartResponse;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveLinkBuilder;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.util.ArrayList;
import java.util.List;


public class UserCartLinkBuilder implements ReactiveLinkBuilder<UserCartResponse, UserCartController> {
    @Override
    public List<WebFluxLinkBuilder.WebFluxLink> createModelLinks(UserCartResponse userCartResponse, Class<UserCartController> c) {
        List<WebFluxLinkBuilder.WebFluxLink> links = new ArrayList<>();
        links.add(WebFluxLinkBuilder.linkTo(
                WebFluxLinkBuilder.methodOn(c).deleteCart(userCartResponse.getUserId())).withRel("delete"));
        links.add(WebFluxLinkBuilder.linkTo(
                WebFluxLinkBuilder.methodOn(c).getOrCreateCart(
                        14L
                )).withRel("getOrCreate"));
        links.add(WebFluxLinkBuilder.linkTo(
                WebFluxLinkBuilder.methodOn(c).removeFromCart(
                        UserCartBody.builder()
                                .planIds(List.of(1L))
                                .build(), 14L
                )).withRel("removeFromCart"));
        links.add(WebFluxLinkBuilder.linkTo(
                WebFluxLinkBuilder.methodOn(c).addToCart(
                        UserCartBody.builder()
                                .planIds(List.of(1L))
                                .build(), 14L
                )).withRel("addToCart"));
        return links;
    }
}
