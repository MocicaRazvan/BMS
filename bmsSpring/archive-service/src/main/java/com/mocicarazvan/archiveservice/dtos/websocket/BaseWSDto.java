package com.mocicarazvan.archiveservice.dtos.websocket;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
public abstract class BaseWSDto {
    private String queueName;
    private String id;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
