package com.mocicarazvan.websocketservice.utils;

import com.mocicarazvan.websocketservice.dtos.PageInfo;
import com.mocicarazvan.websocketservice.dtos.PageableBody;
import com.mocicarazvan.websocketservice.dtos.PageableResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PageableUtilsCustomTest {

    private final PageableUtilsCustom pageableUtils = new PageableUtilsCustom();

    @Test
    void createSortFromMapShouldReturnCorrectSortOrdersForValidSortCriteria() {
        Map<String, String> sortCriteria = new LinkedHashMap<>();
        sortCriteria.put("name", "asc");
        sortCriteria.put("date", "desc");

        Sort sort = pageableUtils.createSortFromMap(sortCriteria);
        List<Sort.Order> orders = sort.get().toList();

        assertEquals(2, orders.size());
        assertEquals("name", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("date", orders.get(1).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(1).getDirection());
    }

    @Test
    void createSortFromMapShouldIgnoreInvalidSortDirections() {
        Map<String, String> sortCriteria = new LinkedHashMap<>();
        sortCriteria.put("name", "asc");
        sortCriteria.put("date", "invalid");

        Sort sort = pageableUtils.createSortFromMap(sortCriteria);
        List<Sort.Order> orders = sort.get().toList();

        assertEquals(1, orders.size());
        assertEquals("name", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
    }

    @Test
    void createPageRequestBuildsCorrectPageRequest() {
        Map<String, String> sortCriteria = new LinkedHashMap<>();
        sortCriteria.put("id", "desc");
        PageableBody pageableBody = new PageableBody();
        pageableBody.setPage(1);
        pageableBody.setSize(20);
        pageableBody.setSortingCriteria(sortCriteria);

        var pageRequest = pageableUtils.createPageRequest(pageableBody);

        assertEquals(1, pageRequest.getPageNumber());
        assertEquals(20, pageRequest.getPageSize());
        List<Sort.Order> orders = pageRequest.getSort().get().toList();
        assertEquals(1, orders.size());
        assertEquals("id", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());
    }

    @Test
    void createPageInfoReturnsExpectedPageInfo() {
        List<String> content = Arrays.asList("a", "b", "c");
        Page<String> page = new PageImpl<>(content, PageRequest.of(2, 3), 9);
        PageInfo pageInfo = pageableUtils.createPageInfo(page);

        assertEquals(2, pageInfo.getCurrentPage());
        assertEquals(3, pageInfo.getPageSize());
        assertEquals(9, pageInfo.getTotalElements());
        assertEquals(3, pageInfo.getTotalPages());
    }

    @Test
    void createPageableResponseCorrectlyMapsContent() {
        List<Integer> content = Arrays.asList(1, 2, 3);
        Page<Integer> page = new PageImpl<>(content, PageRequest.of(0, 3), 3);
        PageableResponse<List<String>> response = pageableUtils.createPageableResponse(page, Object::toString);

        assertNotNull(response);
        assertNotNull(response.getPageInfo());
        assertEquals(0, response.getPageInfo().getCurrentPage());
        assertEquals(3, response.getPageInfo().getPageSize());
        assertEquals(3, response.getContent().size());
        assertEquals("1", response.getContent().get(0));
        assertTrue(response.getLinks().isEmpty());
    }
}