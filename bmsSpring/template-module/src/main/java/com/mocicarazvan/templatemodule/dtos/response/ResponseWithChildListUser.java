package com.mocicarazvan.templatemodule.dtos.response;

import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ResponseWithChildListUser<E, C> {
    private ResponseWithUserDtoEntity<E> entity;
    private List<C> children;
}
