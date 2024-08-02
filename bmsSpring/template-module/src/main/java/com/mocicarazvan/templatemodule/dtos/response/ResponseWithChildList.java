package com.mocicarazvan.templatemodule.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ResponseWithChildList<E, C> {
    private E entity;
    private List<C> children;
}
