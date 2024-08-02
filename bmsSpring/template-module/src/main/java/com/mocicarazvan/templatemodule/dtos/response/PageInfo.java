package com.mocicarazvan.templatemodule.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Page information for a given request.")
public class PageInfo {
    @Schema(description = "Current page number.")
    private int currentPage;
    @Schema(description = "Number of elements in the current page.")
    private int pageSize;
    @Schema(description = "Total number of elements.")
    private long totalElements;
    @Schema(description = "Total number of pages.")
    private int totalPages;
}
