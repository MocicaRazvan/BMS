package com.mocicarazvan.templatemodule.dtos.errors;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SortingCriteriaResponse extends BaseErrorResponse {
    private Map<String, String> sortingCriteria;

    public SortingCriteriaResponse withBase(BaseErrorResponse baseErrorResponse, Map<String, String> sortingCriteria) {
        return SortingCriteriaResponse.builder()
                .error(baseErrorResponse.getError())
                .message(baseErrorResponse.getMessage())
                .path(baseErrorResponse.getPath())
                .status(baseErrorResponse.getStatus())
                .timestamp(baseErrorResponse.getTimestamp())
                .sortingCriteria(sortingCriteria)
                .build();
    }
}
