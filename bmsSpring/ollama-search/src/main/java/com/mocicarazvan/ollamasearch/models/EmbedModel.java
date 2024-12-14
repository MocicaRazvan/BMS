package com.mocicarazvan.ollamasearch.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class EmbedModel {
    private Long id;
    private Long entityId;
    private float[] embedding;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
