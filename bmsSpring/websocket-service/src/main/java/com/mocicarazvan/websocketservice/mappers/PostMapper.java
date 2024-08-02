package com.mocicarazvan.websocketservice.mappers;

import com.mocicarazvan.websocketservice.dtos.post.PostResponse;
import com.mocicarazvan.websocketservice.mappers.generic.ApproveModelMapper;
import com.mocicarazvan.websocketservice.models.Post;
import org.springframework.stereotype.Component;

@Component
public class PostMapper extends ApproveModelMapper<Post, PostResponse> {
    public PostMapper(ConversationUserMapper conversationUserMapper) {
        super(conversationUserMapper);
    }

    @Override
    public PostResponse fromModelToResponse(Post post) {
        return PostResponse.builder()
                .approved(post.isApproved())
                .receiver(conversationUserMapper.fromModelToResponse(post.getReceiver()))
                .appId(post.getAppId())
                .id(post.getId())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
