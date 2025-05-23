package com.mocicarazvan.templatemodule.dtos.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
public class EntityCount {
    private Long count;

    public EntityCount(Integer count) {
        if (count == null) {
            this.count = 0L;
            return;
        }
        this.count = count.longValue();
    }

    public EntityCount(Long count) {
        if (count == null) {
            this.count = 0L;
            return;
        }
        this.count = count;
    }

    public EntityCount(List<Long> ids) {
        this.count = (long) ids.size();
    }
}
