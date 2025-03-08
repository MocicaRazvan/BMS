package com.mocicarazvan.rediscache.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotifyCacheRemoveDto {
    private Collection<String> keys;
    private CacheRemoveType type;
    private CacheRemoveKeyRemoveType keyRemoveType;

    public NotifyCacheRemoveDto(String key, CacheRemoveType type, CacheRemoveKeyRemoveType keyRemoveType) {
        this.keys = List.of(key);
        this.type = type;
        this.keyRemoveType = keyRemoveType;
    }
}
