package com.mocicarazvan.templatemodule.utils;

import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageInfo;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.exceptions.common.SortingCriteriaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PageableUtilsCustomTest {

    private final PageableUtilsCustom pageableUtilsCustom = new PageableUtilsCustom();

    @Test
    void isSortingCriteriaValid_valid() {
        Map<String, String> criteria = Map.of(
                "name", "asc",
                "age", "desc"
        );
        List<String> allowedFields = List.of("name", "age");
        StepVerifier.create(pageableUtilsCustom.isSortingCriteriaValid(criteria, allowedFields))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void isSortingCriteriaValid_allowedFieldsNull() {
        Map<String, String> criteria = Map.of(
                "name", "asc",
                "age", "desc"
        );
        StepVerifier.create(pageableUtilsCustom.isSortingCriteriaValid(criteria, null))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void isSortingCriteriaValid_criteriaNull() {
        List<String> allowedFields = List.of("name", "age");
        StepVerifier.create(pageableUtilsCustom.isSortingCriteriaValid(null, allowedFields))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void isSortingCriteriaValid_allowedFieldsNotMatch() {
        Map<String, String> criteria = Map.of(
                "name", "asc",
                "age", "desc"
        );
        List<String> allowedFields = List.of("name");
        StepVerifier.create(pageableUtilsCustom.isSortingCriteriaValid(criteria, allowedFields))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void isSortingCriteriaValid_invalidSortingCriteria() {
        Map<String, String> criteria = Map.of(
                "name", "asc",
                "age", "invalid"
        );
        List<String> allowedFields = List.of("name", "age");
        StepVerifier.create(pageableUtilsCustom.isSortingCriteriaValid(criteria, allowedFields))
                .expectErrorMatches(
                        throwable -> throwable instanceof SortingCriteriaException &&
                                ((SortingCriteriaException) throwable).getInvalidCriteria().equals(Map.of("age", "invalid"))
                )
                .verify();
    }

    @Test
    void createSortFromMap() {
        Sort expectedSort = Sort.by(Sort.Order.asc("name"),
                Sort.Order.desc("age"));
        LinkedHashMap<String, String> sortCriteria = new LinkedHashMap<>();
        sortCriteria.put("name", "Asc");
        sortCriteria.put("age", "desc");
        sortCriteria.put("invalidField", "invalid");

        StepVerifier.create(pageableUtilsCustom.createSortFromMap(sortCriteria))
                .expectSubscription()
                .expectNext(expectedSort)
                .verifyComplete();
    }

    @Test
    void createPageRequest() {
        Sort sort = Sort.by(Sort.Order.asc("name"),
                Sort.Order.desc("age"));
        LinkedHashMap<String, String> sortCriteria = new LinkedHashMap<>();
        sortCriteria.put("name", "Asc");
        sortCriteria.put("age", "desc");
        sortCriteria.put("invalidField", "invalid");
        PageableBody pageableBody = PageableBody.builder()
                .sortingCriteria(sortCriteria)
                .page(10)
                .size(20)
                .build();
        PageRequest expected = PageRequest.of(10, 20, sort);

        StepVerifier.create(pageableUtilsCustom.createPageRequest(pageableBody))
                .expectSubscription()
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void createPageRequestQuery_noSortingCriteria() {
        PageRequest pr = PageRequest.of(0, 10);
        String expected = " LIMIT 10 OFFSET 0";
        assertEquals(expected, pageableUtilsCustom.createPageRequestQuery(pr));
    }

    @Test
    void createPageRequestQuery_sortingCriteria() {
        PageRequest pr = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("name"),
                Sort.Order.desc("age")));
        String expected = " ORDER BY name ASC, age DESC LIMIT 10 OFFSET 0";
        assertEquals(expected, pageableUtilsCustom.createPageRequestQuery(pr));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createPageRequestQueryExtraOrderEmpty_noSortingCriteria(String extraOrder) {
        PageRequest pr = PageRequest.of(0, 10);
        String expected = " LIMIT 10 OFFSET 0";
        assertEquals(expected, pageableUtilsCustom.createPageRequestQuery(pr, extraOrder));
    }

    @Test
    void createPageRequestQueryExtraOrder_noSortingCriteria() {
        PageRequest pr = PageRequest.of(0, 10);
        String extraOrder = "name ASC, age DESC";
        String expected = " ORDER BY " + extraOrder + " LIMIT 10 OFFSET 0";
        assertEquals(expected, pageableUtilsCustom.createPageRequestQuery(pr, extraOrder));
    }

    @Test
    void createPageRequestQueryExtraOrder_sortingCriteria() {
        PageRequest pr = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("p1"),
                Sort.Order.desc("p2")));
        String extraOrder = "name ASC, age DESC";
        String expected = " ORDER BY p1 ASC, p2 DESC , " + extraOrder + " LIMIT 10 OFFSET 0";
        assertEquals(expected, pageableUtilsCustom.createPageRequestQuery(pr, extraOrder));
    }

    @Test
    void createPageableRequest() {
        Flux<Integer> content = Flux.range(0, 10);
        Pageable pageable = PageRequest.of(0, 3);
        List<PageableResponse<Long>> expected = IntStream.range(0, 10)
                .mapToObj(i -> PageableResponse.<Long>builder()
                        .pageInfo(
                                PageInfo.builder()
                                        .currentPage(0)
                                        .pageSize(3)
                                        .totalElements(10L)
                                        .totalPages(4)
                                        .build()
                        )
                        .content((long) i)
                        .build()).toList();
        StepVerifier.create(pageableUtilsCustom.createPageableResponse(content, Mono.just(10L), pageable))
                .expectSubscription()
                .expectNextCount(10)
                .verifyComplete();
    }

    @Test
    void createPageInfo() {
        Page<Integer> page = new PageImpl<>(List.of(1, 2), PageRequest.of(0, 10), 100);
        PageInfo expected = PageInfo.builder()
                .currentPage(0)
                .pageSize(10)
                .totalElements(100L)
                .totalPages(10)
                .build();

        StepVerifier.create(pageableUtilsCustom.createPageInfo(page))
                .expectSubscription()
                .expectNext(expected)
                .verifyComplete();
    }

    public static Stream<Arguments> stringProvider() {
        return Stream.of(
                Arguments.of(null, false),
                Arguments.of("", false),
                Arguments.of(" ", false),
                Arguments.of("  ", false),
                Arguments.of("a", true),
                Arguments.of("a ", true)
        );
    }

    @ParameterizedTest
    @MethodSource("stringProvider")
    void isStringEmptyOrNull(String input, boolean expected) {
        assertEquals(expected, pageableUtilsCustom.isNotNullOrEmpty(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void appendPageRequestQueryCallbackIfFieldIsNotEmpty_emptyField(String field) {
        StringBuilder builder = new StringBuilder();
        PageRequest pageRequest = PageRequest.of(0, 10);
        AtomicBoolean called = new AtomicBoolean(false);
        Function<String, String> cb = s -> {
            called.set(true);
            return s;
        };

        pageableUtilsCustom.appendPageRequestQueryCallbackIfFieldIsNotEmpty(builder, pageRequest, field, cb);
        assertFalse(called.get());
    }

    @Test
    void appendPageRequestQueryCallbackIfFieldIsNotEmpty_nonEmptyField() {
        StringBuilder builder = new StringBuilder();
        PageRequest pageRequest = PageRequest.of(0, 10);
        String field = "name";
        AtomicBoolean called = new AtomicBoolean(false);
        Function<String, String> cb = s -> {
            called.set(true);
            return s;
        };

        pageableUtilsCustom.appendPageRequestQueryCallbackIfFieldIsNotEmpty(builder, pageRequest, field, cb);
        assertTrue(called.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"fieldCamel1", "field2"})
    void getOrderColumnDefNormalColumn(String field) {
        String expected = EntitiesUtils.camelToSnake(field);
        String result = pageableUtilsCustom.getOrderColumnDef(field);
        assertEquals(expected, result);
    }

    @Test
    void getOrderColumnDefUserLikesDislikes() {
        assertEquals("cardinality(user_likes)", pageableUtilsCustom.getOrderColumnDef(PageableUtilsCustom.USER_LIKES_LENGTH_SORT_PROPERTY));
        assertEquals("cardinality(user_dislikes)", pageableUtilsCustom.getOrderColumnDef(PageableUtilsCustom.USER_DISLIKES_LENGTH_SORT_PROPERTY));
    }

}