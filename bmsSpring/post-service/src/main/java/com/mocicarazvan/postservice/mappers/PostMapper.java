package com.mocicarazvan.postservice.mappers;


import com.mocicarazvan.postservice.dtos.PostBody;
import com.mocicarazvan.postservice.dtos.PostResponse;
import com.mocicarazvan.postservice.dtos.PostResponseWithSimilarity;
import com.mocicarazvan.postservice.dtos.PostWithSimilarity;
import com.mocicarazvan.postservice.models.Post;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import io.r2dbc.spi.Row;
import org.mapstruct.Mapper;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;


@Mapper(componentModel = "spring")
public abstract class PostMapper extends DtoMapper<Post, PostBody, PostResponse> {

    @Override
    public Mono<Post> updateModelFromBody(PostBody body, Post post) {
        post.setTags(body.getTags());
        post.setTitle(body.getTitle());
        post.setBody(body.getBody());
        post.setApproved(false);
        post.setUpdatedAt(LocalDateTime.now());
        return Mono.just(post);
    }

    @Override
    public PostResponse fromModelToResponse(Post post) {
        return PostResponse.builder()
                .tags(post.getTags())
                .approved(post.isApproved())
                .images(post.getImages())
                .body(post.getBody())
                .title(post.getTitle())
                .userLikes(post.getUserLikes())
                .userDislikes(post.getUserDislikes())
                .userId(post.getUserId())
                .id(post.getId())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    @Override
    public Post fromBodyToModel(PostBody body) {
        Post post = new Post();
        post.setTags(body.getTags());
        post.setTitle(body.getTitle());
        post.setBody(body.getBody());
        post.setApproved(false);
        return post;
    }

    public Post fromRowToModel(Row row) {
        return Post.builder()
                .tags(EntitiesUtils.convertArrayToList(row.get("tags", String[].class)))
                .approved(Boolean.TRUE.equals(row.get("approved", Boolean.class)))
                .images(EntitiesUtils.convertArrayToList(row.get("images", String[].class)))
                .title(row.get("title", String.class))
                .body(row.get("body", String.class))
                .userLikes(EntitiesUtils.convertArrayToList(row.get("user_likes", Long[].class)))
                .userDislikes(EntitiesUtils.convertArrayToList(row.get("user_dislikes", Long[].class)))
                .userId(row.get("user_id", Long.class))
                .id(row.get("id", Long.class))
                .createdAt(row.get("created_at", LocalDateTime.class))
                .updatedAt(row.get("updated_at", LocalDateTime.class))
                .build();
    }

    public PostResponseWithSimilarity fromPostWithSimilarityToResponse(PostWithSimilarity postWithSimilarity) {
        return PostResponseWithSimilarity.builder()
                .tags(postWithSimilarity.getTags())
                .approved(postWithSimilarity.isApproved())
                .images(postWithSimilarity.getImages())
                .body(postWithSimilarity.getBody())
                .title(postWithSimilarity.getTitle())
                .userLikes(postWithSimilarity.getUserLikes())
                .userDislikes(postWithSimilarity.getUserDislikes())
                .userId(postWithSimilarity.getUserId())
                .id(postWithSimilarity.getId())
                .createdAt(postWithSimilarity.getCreatedAt())
                .updatedAt(postWithSimilarity.getUpdatedAt())
                .similarity(postWithSimilarity.getSimilarity())
                .build();
    }


}
