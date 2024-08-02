package com.mocicarazvan.templatemodule.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "The response for a page, when the request it's of type PageableBody")
public class PageableResponse<T> {
    private T content;
    private PageInfo pageInfo;
    private List<WebFluxLinkBuilder.WebFluxLink> links;
}
