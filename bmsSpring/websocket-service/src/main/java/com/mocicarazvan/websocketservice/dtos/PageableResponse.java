package com.mocicarazvan.websocketservice.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PageableResponse<T> {
    private T content;
    private PageInfo pageInfo;
    private List<?> links;
}
