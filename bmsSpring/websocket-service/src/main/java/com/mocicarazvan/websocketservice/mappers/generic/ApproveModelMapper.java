package com.mocicarazvan.websocketservice.mappers.generic;

import com.mocicarazvan.websocketservice.dtos.generic.ApproveResponse;
import com.mocicarazvan.websocketservice.mappers.ConversationUserMapper;
import com.mocicarazvan.websocketservice.models.generic.ApprovedModel;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public abstract class ApproveModelMapper<M extends ApprovedModel, R extends ApproveResponse> implements ModelResponseMapper<M, R> {
    protected final ConversationUserMapper conversationUserMapper;


}
