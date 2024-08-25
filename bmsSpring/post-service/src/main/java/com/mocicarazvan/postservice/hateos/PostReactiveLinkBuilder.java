package com.mocicarazvan.postservice.hateos;

import com.mocicarazvan.postservice.controllers.PostController;
import com.mocicarazvan.postservice.dtos.PostBody;
import com.mocicarazvan.postservice.dtos.PostResponse;
import com.mocicarazvan.postservice.mappers.PostMapper;
import com.mocicarazvan.postservice.models.Post;
import com.mocicarazvan.postservice.repositories.PostRepository;
import com.mocicarazvan.postservice.services.PostService;
import com.mocicarazvan.templatemodule.hateos.controller.generics.ApproveReactiveLinkBuilder;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.util.List;

public class PostReactiveLinkBuilder extends ApproveReactiveLinkBuilder<Post, PostBody, PostResponse, PostRepository, PostMapper, PostService, PostController> {


    @Override
    public List<WebFluxLinkBuilder.WebFluxLink> createModelLinks(PostResponse postResponse, Class<PostController> c) {
        List<WebFluxLinkBuilder.WebFluxLink> links = super.createModelLinks(postResponse, c);

        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getPostsFilteredWithUser(null, null, null, null, null, null)).withRel("getPostsFilteredWithUser"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getPostsFiltered(null, null, null, null, null, null)).withRel("getPostsFiltered"));

        return links;
    }
}
