package com.mocicarazvan.websocketservice.dtos.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageProjection<T> {
    private List<T> message;
    private long total;
}
