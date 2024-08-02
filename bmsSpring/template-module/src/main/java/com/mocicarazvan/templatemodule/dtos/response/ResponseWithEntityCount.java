package com.mocicarazvan.templatemodule.dtos.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
// use with concat map in count in parent service
public class ResponseWithEntityCount<T> {
    private T model;
    private Long count;

    public static <T> ResponseWithEntityCount<T> of(T model, EntityCount entityCount) {
        return ResponseWithEntityCount.<T>builder()
                .model(model)
                .count(entityCount.getCount())
                .build();
    }
}
