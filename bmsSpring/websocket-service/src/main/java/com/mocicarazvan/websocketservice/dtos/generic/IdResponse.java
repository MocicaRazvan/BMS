package com.mocicarazvan.websocketservice.dtos.generic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class IdResponse {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
