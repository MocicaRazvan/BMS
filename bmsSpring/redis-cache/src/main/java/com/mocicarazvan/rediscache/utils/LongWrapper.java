package com.mocicarazvan.rediscache.utils;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LongWrapper {

    private Long value;

    public static LongWrapper of(Long value) {
        return new LongWrapper(value);
    }
}
