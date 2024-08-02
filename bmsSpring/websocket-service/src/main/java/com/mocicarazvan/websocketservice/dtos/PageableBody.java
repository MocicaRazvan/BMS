package com.mocicarazvan.websocketservice.dtos;


import com.mocicarazvan.websocketservice.validations.SortingCriteria;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageableBody {
    @NotNull(message = "The page number should be present")
    @Min(value = 0, message = "Page is a non negative value.")
    private int page = 0;

    @NotNull(message = "The page size should be present")
    @Min(value = 1, message = "The size should be at least 1.")
    private int size = 10;

    @SortingCriteria
    private Map<String, String> sortingCriteria = new HashMap<>();

}