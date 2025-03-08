package com.mocicarazvan.templatemodule.utils;


import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageInfo;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.exceptions.common.SortingCriteriaException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component

public class PageableUtilsCustom {

    public Mono<Void> isSortingCriteriaValid(Map<String, String> sortingCriteria, List<String> allowedFields) {
        assert allowedFields != null;

        if (sortingCriteria == null) {
            return Mono.empty();
        }

        final Set<String> allowedValues = Set.of("asc", "desc");
        Map<String, String> invalidCriteria = new HashMap<>();

        sortingCriteria.forEach((key, value) -> {
            if (allowedFields.contains(key) && !allowedValues.contains(value.toLowerCase())) {
                invalidCriteria.put(key, value);
            }
        });

        if (!invalidCriteria.isEmpty()) {
            return Mono.error(new SortingCriteriaException("Invalid sorting criteria provided.", invalidCriteria));
        }
        return Mono.empty();
    }

    public Mono<Sort> createSortFromMap(Map<String, String> sortCriteria) {
        return Mono.just(Sort.by(
                sortCriteria.entrySet().stream().filter(
                        entry -> entry.getValue().equals("asc") || entry.getValue().equals("desc")
                ).map(
                        entry -> new Sort.Order(
                                entry.getValue().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                                entry.getKey()
                        )
                ).collect(Collectors.toList())

        ))
//                .log()
                ;


    }

    public Mono<PageRequest> createPageRequest(PageableBody pageableBody) {
        return createSortFromMap(pageableBody.getSortingCriteria())
                .map(sort -> PageRequest.of(
                        pageableBody.getPage(),
                        pageableBody.getSize(),
                        sort
                ));
    }

    public String createPageRequestQuery(PageRequest pageRequest) {
        StringBuilder queryString = new StringBuilder();

        if (pageRequest.getSort().isSorted()) {
            queryString.append(" ORDER BY ");
            pageRequest.getSort().forEach(order -> {
                queryString.append(EntitiesUtils.camelToSnake(order.getProperty()))
                        .append(" ")
                        .append(order.getDirection().name())
                        .append(", ");
            });
            // remove the last comma and space
            queryString.setLength(queryString.length() - 2);
        }

        addLimitAndOffset(pageRequest, queryString);

        return queryString.toString();
    }

    public String createPageRequestQuery(PageRequest pageRequest, String extraOrder) {
        StringBuilder queryString = new StringBuilder();

        if (pageRequest.getSort().isSorted()) {
            queryString.append(" ORDER BY ");
            pageRequest.getSort().forEach(order -> {
                queryString.append(EntitiesUtils.camelToSnake(order.getProperty()))
                        .append(" ")
                        .append(order.getDirection().name())
                        .append(", ");
            });
            // remove the last comma and space
            queryString.setLength(queryString.length() - 2);

            if (extraOrder != null && !extraOrder.isEmpty()) {
                queryString.append(" , ")
                        .append(extraOrder);
            }

        } else if (extraOrder != null && !extraOrder.isEmpty()) {
            queryString.append(" ORDER BY ")
                    .append(extraOrder);
        }

        addLimitAndOffset(pageRequest, queryString);

        return queryString.toString();
    }


    public String createPageRequestQuery(PageRequest pageRequest, List<String> extraOrders) {
        StringBuilder queryString = new StringBuilder();

        if (pageRequest.getSort().isSorted()) {
            queryString.append(" ORDER BY ");
            pageRequest.getSort().forEach(order -> {
                queryString.append(EntitiesUtils.camelToSnake(order.getProperty()))
                        .append(" ")
                        .append(order.getDirection().name())
                        .append(", ");
            });
            // remove the last comma and space
            queryString.setLength(queryString.length() - 2);

            if (extraOrders != null && !extraOrders.isEmpty()) {
                extraOrders.forEach(extraOrder -> {
                    if (extraOrder != null && !extraOrder.isEmpty()) {
                        queryString.append(" , ")
                                .append(extraOrder);
                    }
                });
            }

        } else if (extraOrders != null && !extraOrders.isEmpty()) {
            final boolean[] first = {true};

            extraOrders.forEach(extraOrder -> {
                if (extraOrder != null && !extraOrder.isEmpty()) {
                    if (first[0]) {
                        queryString.append(" ORDER BY ");
                        first[0] = false;
                    } else {
                        queryString.append(" , ");
                    }
                    queryString.append(extraOrder);
                }
            });

        }

        addLimitAndOffset(pageRequest, queryString);

        return queryString.toString();
    }

    private void addLimitAndOffset(PageRequest pageRequest, StringBuilder queryString) {
        queryString.append(" LIMIT ")
                .append(pageRequest.getPageSize())
                .append(" OFFSET ")
                .append(pageRequest.getOffset());
    }


    public <T> Flux<PageableResponse<T>> createPageableResponse(Flux<T> content, Mono<Long> count, Pageable pageable) {


        return count.flatMapMany(totalElements -> {
            PageInfo pi = PageInfo.builder()
                    .currentPage(pageable.getPageNumber())
                    .pageSize(pageable.getPageSize())
                    .totalElements(totalElements)
                    .totalPages((int) Math.ceil((double) totalElements / pageable.getPageSize()))
                    .build();
            return content.map(c -> PageableResponse.<T>builder()
                    .pageInfo(pi)
                    .content(c)
                    .build());
        });

    }

    public <T> Mono<PageInfo> createPageInfo(Page<T> page) {
        return Mono.just(PageInfo.builder()
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build());
    }

    public void appendPageRequestQueryCallbackIfFieldIsNotEmpty(StringBuilder queryBuilder, PageRequest pageRequest, String field, Function<String, String> callback) {
        if (isNotNullOrEmpty(field)) {
            queryBuilder.append(createPageRequestQuery(pageRequest,
                    callback.apply(field)
            ));
        } else {
            queryBuilder.append(createPageRequestQuery(pageRequest));
        }
    }

    public boolean isNotNullOrEmpty(String field) {
        return field != null && !field.isBlank();
    }
}
