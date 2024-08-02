package com.mocicarazvan.commentservice.hateos;


import com.mocicarazvan.commentservice.controllers.CommentController;
import com.mocicarazvan.commentservice.dtos.CommentBody;
import com.mocicarazvan.commentservice.dtos.CommentResponse;
import com.mocicarazvan.commentservice.enums.CommentReferenceType;
import com.mocicarazvan.commentservice.mappers.CommentMapper;
import com.mocicarazvan.commentservice.models.Comment;
import com.mocicarazvan.commentservice.repositories.CommentRepository;
import com.mocicarazvan.commentservice.services.CommentService;
import com.mocicarazvan.templatemodule.hateos.controller.generics.TitleBodyReactiveLinkBuilder;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.util.List;


public class CommentReactiveLinkBuilder extends TitleBodyReactiveLinkBuilder<Comment, CommentBody, CommentResponse,
        CommentRepository, CommentMapper, CommentService, CommentController> {

    @Override
    public List<WebFluxLinkBuilder.WebFluxLink> createModelLinks(CommentResponse commentResponse, Class<CommentController> c) {
        List<WebFluxLinkBuilder.WebFluxLink> links = super.createModelLinks(commentResponse, c);
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).createModel(commentResponse.getReferenceId(), CommentReferenceType.POST.toString().toLowerCase(), null, null)).withRel("create"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getCommentsByReference(commentResponse.getReferenceId(), null, CommentReferenceType.POST.toString().toLowerCase())).withRel("getByPost"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getCommentsByUser(commentResponse.getUserId(), null, CommentReferenceType.POST.toString().toLowerCase())).withRel("getByUser"));
        return links;
    }
}
