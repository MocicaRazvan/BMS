package com.mocicarazvan.postservice.hateos;


import com.mocicarazvan.postservice.controllers.PostController;
import com.mocicarazvan.postservice.dtos.PostResponse;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveResponseBuilder;
import org.springframework.stereotype.Component;


@Component
public class PostReactiveResponseBuilder extends ReactiveResponseBuilder<PostResponse, PostController> {
    public PostReactiveResponseBuilder() {
        super(new PostReactiveLinkBuilder());
    }
}
