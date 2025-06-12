package com.mocicarazvan.fileservice.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CachedImageRedisModel implements Serializable {
    private byte[] imageData;
    private String attachment;
    private boolean webpOutputEnabled;

    @Builder.Default
    private long timestamp = System.currentTimeMillis();
}
