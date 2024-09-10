package com.mocicarazvan.templatemodule.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityCount {
    private Long count;

    public EntityCount(Integer count) {
        this.count = count.longValue();
    }

    public EntityCount(List<Long> ids) {
        this.count = (long) ids.size();
    }
}
