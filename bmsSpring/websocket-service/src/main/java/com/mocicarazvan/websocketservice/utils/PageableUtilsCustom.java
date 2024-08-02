package com.mocicarazvan.websocketservice.utils;


import com.mocicarazvan.websocketservice.dtos.PageInfo;
import com.mocicarazvan.websocketservice.dtos.PageableBody;
import com.mocicarazvan.websocketservice.dtos.PageableResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component

public class PageableUtilsCustom {


    public Sort createSortFromMap(Map<String, String> sortCriteria) {
        return Sort.by(
                sortCriteria.entrySet().stream().filter(
                        entry -> entry.getValue().equals("asc") || entry.getValue().equals("desc")
                ).map(
                        entry -> new Sort.Order(
                                entry.getValue().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                                entry.getKey()
                        )
                ).collect(Collectors.toList())

        );
    }

    public PageRequest createPageRequest(PageableBody pageableBody) {
        return PageRequest.of(
                pageableBody.getPage(),
                pageableBody.getSize(),
                createSortFromMap(pageableBody.getSortingCriteria())
        );
    }

    public PageInfo createPageInfo(Page<?> page) {
        return PageInfo.builder()
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    public <T, R> PageableResponse<List<R>> createPageableResponse(Page<T> page, Function<T, R> contentMapper) {
        return PageableResponse.<List<R>>builder()
                .pageInfo(createPageInfo(page))
                .content(page.getContent().stream().map(contentMapper).toList())
                .links(List.of())
                .build();
    }


//    public <T> Flux<PageableResponse<T>> createPageableResponse(Flux<T> content, Mono<Long> count, Pageable pageable) {
//
//
//        return count.flatMapMany(totalElements -> {
//            PageInfo pi = PageInfo.builder()
//                    .currentPage(pageable.getPageNumber())
//                    .pageSize(pageable.getPageSize())
//                    .totalElements(totalElements)
//                    .totalPages((int) Math.ceil((double) totalElements / pageable.getPageSize()))
//                    .build();
//            return content.map(c -> PageableResponse.<T>builder()
//                    .pageInfo(pi)
//                    .content(c)
//                    .build());
//        });
//
//    }
//
//    public <T> Mono<PageInfo> createPageInfo(Page<T> page) {
//        return Mono.just(PageInfo.builder()
//                .currentPage(page.getNumber())
//                .pageSize(page.getSize())
//                .totalElements(page.getTotalElements())
//                .totalPages(page.getTotalPages())
//                .build());
//    }
}
