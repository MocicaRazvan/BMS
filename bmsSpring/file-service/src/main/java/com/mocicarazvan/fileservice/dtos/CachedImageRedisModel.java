package com.mocicarazvan.fileservice.dtos;


import com.mocicarazvan.fileservice.repositories.ImageRedisRepository;
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
    private ImageRedisRepository.ImageRedisRepositoryImageCharacteristics imageCharacteristics;

    @Builder.Default
    private long timestamp = System.currentTimeMillis();
}
