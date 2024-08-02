package com.mocicarazvan.commentservice.hateos;


import com.mocicarazvan.commentservice.controllers.CommentController;
import com.mocicarazvan.commentservice.dtos.CommentResponse;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveResponseBuilder;
import org.springframework.stereotype.Component;

@Component
public class CommentReactiveResponseBuilder extends ReactiveResponseBuilder<CommentResponse, CommentController> {
    public CommentReactiveResponseBuilder() {
        super(new CommentReactiveLinkBuilder());
    }
}
