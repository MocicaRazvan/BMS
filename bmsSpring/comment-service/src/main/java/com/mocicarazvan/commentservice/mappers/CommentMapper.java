package com.mocicarazvan.commentservice.mappers;


import com.mocicarazvan.commentservice.dtos.CommentBody;
import com.mocicarazvan.commentservice.dtos.CommentResponse;
import com.mocicarazvan.commentservice.enums.CommentReferenceType;
import com.mocicarazvan.commentservice.models.Comment;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import org.mapstruct.Mapper;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public abstract class CommentMapper extends DtoMapper<Comment, CommentBody, CommentResponse> {


    @Override
    public CommentResponse fromModelToResponse(Comment comment) {
        return CommentResponse.builder()
                .referenceId(comment.getReferenceId())
                .body(comment.getBody())
                .title(comment.getTitle())
                .userLikes(comment.getUserLikes())
                .userDislikes(comment.getUserDislikes())
                .userId(comment.getUserId())
                .id(comment.getId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    @Override
    public Comment fromBodyToModel(CommentBody body) {
        return Comment.builder()
                .body(body.getBody())
                .title(body.getTitle())
                .build();
    }

    @Override
    public Mono<Comment> updateModelFromBody(CommentBody body, Comment comment) {
        comment.setBody(body.getBody());
        comment.setTitle(body.getTitle());
        comment.setUpdatedAt(LocalDateTime.now());
        return Mono.just(comment);
    }
}
