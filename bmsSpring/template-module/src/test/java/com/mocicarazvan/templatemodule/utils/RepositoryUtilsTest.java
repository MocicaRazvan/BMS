package com.mocicarazvan.templatemodule.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositoryUtilsTest {

    private RepositoryUtils repositoryUtils;

    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;

    @BeforeEach
    void setUp() {
        repositoryUtils = new RepositoryUtils();
    }

    @Test
    void mutableBooleanWorksCorrectly() {
        RepositoryUtils.MutableBoolean mutableBoolean = new RepositoryUtils.MutableBoolean(false);
        assertFalse(mutableBoolean.isValue());

        mutableBoolean.setValue(true);
        assertTrue(mutableBoolean.isValue());
    }

    @Test
    void addNotNullFieldFirstCriteria() {
        StringBuilder query = new StringBuilder();
        RepositoryUtils.MutableBoolean hasPrevious = new RepositoryUtils.MutableBoolean(false);

        repositoryUtils.addNotNullField("test", query, hasPrevious, "name = test");

        assertEquals(" WHERE name = test", query.toString());
        assertTrue(hasPrevious.isValue());
    }

    @Test
    void addNotNullFieldSubsequentCriteria() {
        StringBuilder query = new StringBuilder();
        RepositoryUtils.MutableBoolean hasPrevious = new RepositoryUtils.MutableBoolean(true);

        repositoryUtils.addNotNullField("test", query, hasPrevious, "age = 25");

        assertEquals(" AND age = 25", query.toString());
    }

    @Test
    void addNotNullFieldWithOrClause() {
        StringBuilder query = new StringBuilder();
        RepositoryUtils.MutableBoolean hasPrevious = new RepositoryUtils.MutableBoolean(true);

        repositoryUtils.addNotNullField("test", query, hasPrevious, "color = red", true);

        assertEquals(" OR color = red", query.toString());
    }

    @Test
    void addNotNullFieldWithAndClause() {
        StringBuilder query = new StringBuilder();
        RepositoryUtils.MutableBoolean hasPrevious = new RepositoryUtils.MutableBoolean(true);

        repositoryUtils.addNotNullField("test", query, hasPrevious, "color = red", false);

        assertEquals(" AND color = red", query.toString());
    }

    @Test
    void addNotNullFieldWithAndClause_noBefore() {
        StringBuilder query = new StringBuilder();
        RepositoryUtils.MutableBoolean hasPrevious = new RepositoryUtils.MutableBoolean(false);

        repositoryUtils.addNotNullField("test", query, hasPrevious, "color = red", false);

        assertEquals(" WHERE color = red", query.toString());
    }

    @Test
    void addStringFieldWithValidString() {
        StringBuilder query = new StringBuilder();
        RepositoryUtils.MutableBoolean hasPrevious = new RepositoryUtils.MutableBoolean(false);

        repositoryUtils.addStringField("valid", query, hasPrevious, "name = 'valid'");

        assertEquals(" WHERE name = 'valid'", query.toString());
    }

    @Test
    void addStringFieldWithInvalidString() {
        StringBuilder query = new StringBuilder();
        RepositoryUtils.MutableBoolean hasPrevious = new RepositoryUtils.MutableBoolean(false);

        repositoryUtils.addStringField("  ", query, hasPrevious, "invalid clause");

        assertEquals("", query.toString());
        assertFalse(hasPrevious.isValue());
    }

    @Test
    void addListFieldWithNonEmptyList() {
        StringBuilder query = new StringBuilder();
        RepositoryUtils.MutableBoolean hasPrevious = new RepositoryUtils.MutableBoolean(false);

        repositoryUtils.addListField(List.of(1, 2, 3), query, hasPrevious, "id IN (:ids)");

        assertEquals(" WHERE id IN (:ids)", query.toString());
        assertTrue(hasPrevious.isValue());
    }

    @Test
    void createExtraOrderWithValidField() {
        String result = repositoryUtils.createExtraOrder("searchTerm", "column", ":param");
        assertEquals(" similarity(column , :param ) DESC ", result);
    }

    @Test
    void createExtraOrderWithInvalidField() {
        String result = repositoryUtils.createExtraOrder("  ", "column", ":param");
        assertEquals("", result);
    }

    @Test
    void bindNotNullFieldWithValue() {
        when(executeSpec.bind(eq("param"), eq("value"))).thenReturn(executeSpec);

        DatabaseClient.GenericExecuteSpec result = repositoryUtils.bindNotNullField("value", executeSpec, "param");

        verify(executeSpec).bind("param", "value");
        assertEquals(executeSpec, result);
    }

    @Test
    void bindStringFieldWithValidValue() {
        when(executeSpec.bind(eq("name"), eq("test"))).thenReturn(executeSpec);

        DatabaseClient.GenericExecuteSpec result = repositoryUtils.bindStringField("test", executeSpec, "name");

        verify(executeSpec).bind("name", "test");
        assertEquals(executeSpec, result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void bindStringFieldWithNullValue(String field) {

        DatabaseClient.GenericExecuteSpec result = repositoryUtils.bindStringField(field, executeSpec, "name");

        verify(executeSpec, never()).bind(any(), any());
        assertEquals(executeSpec, result);
    }

    @Test
    void bindEnumFieldWithValue() {
        enum TestEnum {VALUE}
        when(executeSpec.bind(eq("enum"), eq("VALUE"))).thenReturn(executeSpec);

        DatabaseClient.GenericExecuteSpec result = repositoryUtils.bindEnumField(TestEnum.VALUE, executeSpec, "enum");

        verify(executeSpec).bind("enum", "VALUE");
        assertEquals(executeSpec, result);
    }

    @Test
    void bindEnumFieldWithNullValue() {
        DatabaseClient.GenericExecuteSpec result = repositoryUtils.bindEnumField(null, executeSpec, "enum");

        verify(executeSpec, never()).bind(any(), any());
        assertEquals(executeSpec, result);
    }

    @Test
    void bindStringSearchField() {
        when(executeSpec.bind(eq("search"), eq("%test%"))).thenReturn(executeSpec);

        DatabaseClient.GenericExecuteSpec result = repositoryUtils.bindStringSearchField("test", executeSpec, "search");

        verify(executeSpec).bind("search", "%test%");
        assertEquals(executeSpec, result);
    }

    @Test
    void bindArrayFieldWithValues() {
        List<String> list = List.of("a", "b");
        when(executeSpec.bind(eq("arr"), any(String[].class))).thenReturn(executeSpec);

        DatabaseClient.GenericExecuteSpec result = repositoryUtils.bindArrayField(list, executeSpec, "arr", String.class);

        verify(executeSpec).bind(eq("arr"), eq(new String[]{"a", "b"}));
        assertEquals(executeSpec, result);
    }

    @Test
    void dateRangeMethods() {
        StringBuilder query = new StringBuilder();
        RepositoryUtils.MutableBoolean hasPrevious = new RepositoryUtils.MutableBoolean(false);
        LocalDate date = LocalDate.now();

        repositoryUtils.addCreatedAtLowerBoundField("t", date, query, hasPrevious);
        assertEquals(" WHERE  t.created_at >= :createdAtLowerBound", query.toString());
        assertTrue(hasPrevious.isValue());

        when(executeSpec.bind(eq("createdAtLowerBound"), eq(date))).thenReturn(executeSpec);
        DatabaseClient.GenericExecuteSpec result = repositoryUtils.bindCreatedAtLowerBoundField(date, executeSpec);
        verify(executeSpec).bind("createdAtLowerBound", date);
        assertEquals(executeSpec, result);
    }

    @Test
    void fullDateRangeBinding() {
        LocalDate start = LocalDate.of(2023, 1, 1);
        LocalDate end = LocalDate.of(2023, 12, 31);

        when(executeSpec.bind(eq("createdAtLowerBound"), eq(start))).thenReturn(executeSpec);
        when(executeSpec.bind(eq("createdAtUpperBound"), eq(end))).thenReturn(executeSpec);

        DatabaseClient.GenericExecuteSpec result = repositoryUtils.bindCreatedAtBound(
                start, end, executeSpec
        );

        verify(executeSpec).bind("createdAtLowerBound", start);
        verify(executeSpec).bind("createdAtUpperBound", end);
        assertEquals(executeSpec, result);
    }

    @Test
    void isNotNullOrEmpty() {
        assertTrue(repositoryUtils.isNotNullOrEmpty("valid"));
        assertFalse(repositoryUtils.isNotNullOrEmpty("  "));
        assertFalse(repositoryUtils.isNotNullOrEmpty(null));
    }

    @Test
    void combinedWhereClauseConstruction() {
        StringBuilder query = new StringBuilder();
        RepositoryUtils.MutableBoolean hasPrevious = new RepositoryUtils.MutableBoolean(false);

        repositoryUtils.addStringField("John", query, hasPrevious, "first_name = :name");
        repositoryUtils.addNotNullField(25, query, hasPrevious, "age = :age");
        repositoryUtils.addListField(List.of(1, 2, 3), query, hasPrevious, "id IN (:ids)");

        assertEquals(" WHERE first_name = :name AND age = :age AND id IN (:ids)", query.toString());
    }

    @Test
    void addNotNullFieldWithNullValue() {
        StringBuilder query = new StringBuilder();
        RepositoryUtils.MutableBoolean hasPrevious = new RepositoryUtils.MutableBoolean(false);

        repositoryUtils.addNotNullField(null, query, hasPrevious, "name = 'null'");

        assertEquals("", query.toString());
        assertFalse(hasPrevious.isValue());
    }


    @Test
    void addListFieldWithEmptyList() {
        StringBuilder query = new StringBuilder();
        RepositoryUtils.MutableBoolean hasPrevious = new RepositoryUtils.MutableBoolean(false);

        repositoryUtils.addListField(List.of(), query, hasPrevious, "id IN (:ids)");

        assertEquals("", query.toString());
        assertFalse(hasPrevious.isValue());
    }

    @Test
    void bindNotNullFieldWithNullValue() {
        DatabaseClient.GenericExecuteSpec result = repositoryUtils.bindNotNullField(null, executeSpec, "param");

        assertEquals(executeSpec, result);
    }

    @Test
    void bindListFieldWithEmptyList() {
        DatabaseClient.GenericExecuteSpec result = repositoryUtils.bindListField(List.of(), executeSpec, "param");

        assertEquals(executeSpec, result);
    }


    @Test
    void bindArrayFieldWithEmptyList() {
        List<String> list = List.of();

        DatabaseClient.GenericExecuteSpec result = repositoryUtils.bindArrayField(list, executeSpec, "arr", String.class);

        assertEquals(executeSpec, result);
    }

    @Test
    void addCreatedAtBoundWithNullValues() {
        StringBuilder query = new StringBuilder();
        RepositoryUtils.MutableBoolean hasPrevious = new RepositoryUtils.MutableBoolean(false);

        repositoryUtils.addCreatedAtBound("t", null, null, query, hasPrevious);

        assertEquals("", query.toString());
        assertFalse(hasPrevious.isValue());
    }

    @Test
    void bindCreatedAtBoundWithNullValues() {
        DatabaseClient.GenericExecuteSpec result = repositoryUtils.bindCreatedAtBound(null, null, executeSpec);

        assertEquals(executeSpec, result);
    }

    @Test
    void addUpdatedAtUpperBoundFieldWithValidDate() {
        StringBuilder query = new StringBuilder();
        RepositoryUtils.MutableBoolean hasPrevious = new RepositoryUtils.MutableBoolean(false);
        LocalDate date = LocalDate.now();

        repositoryUtils.addUpdatedAtUpperBoundField("t", date, query, hasPrevious);

        assertEquals(" WHERE  t.updated_at <= :updatedAtUpperBound", query.toString());
        assertTrue(hasPrevious.isValue());
    }

    @Test
    void addUpdatedAtLowerBoundFieldWithValidDate() {
        StringBuilder query = new StringBuilder();
        RepositoryUtils.MutableBoolean hasPrevious = new RepositoryUtils.MutableBoolean(false);
        LocalDate date = LocalDate.now();

        repositoryUtils.addUpdatedAtLowerBoundField("t", date, query, hasPrevious);

        assertEquals(" WHERE  t.updated_at >= :updatedAtLowerBound", query.toString());
        assertTrue(hasPrevious.isValue());
    }

    @Test
    void bindUpdatedAtUpperBoundFieldWithValidDate() {
        LocalDate date = LocalDate.now();
        when(executeSpec.bind(eq("updatedAtUpperBound"), eq(date))).thenReturn(executeSpec);

        DatabaseClient.GenericExecuteSpec result = repositoryUtils.bindUpdatedAtUpperBoundField(date, executeSpec);

        verify(executeSpec).bind("updatedAtUpperBound", date);
        assertEquals(executeSpec, result);
    }

    @Test
    void bindUpdatedAtLowerBoundFieldWithValidDate() {
        LocalDate date = LocalDate.now();
        when(executeSpec.bind(eq("updatedAtLowerBound"), eq(date))).thenReturn(executeSpec);

        DatabaseClient.GenericExecuteSpec result = repositoryUtils.bindUpdatedAtLowerBoundField(date, executeSpec);

        verify(executeSpec).bind("updatedAtLowerBound", date);
        assertEquals(executeSpec, result);
    }

    @Test
    void addUpdatedAtBoundWithValidDates() {
        StringBuilder query = new StringBuilder();
        RepositoryUtils.MutableBoolean hasPrevious = new RepositoryUtils.MutableBoolean(false);
        LocalDate lowerBound = LocalDate.of(2023, 1, 1);
        LocalDate upperBound = LocalDate.of(2023, 12, 31);

        repositoryUtils.addUpdatedAtBound("t", lowerBound, upperBound, query, hasPrevious);

        assertEquals(" WHERE  t.updated_at >= :updatedAtLowerBound AND  t.updated_at <= :updatedAtUpperBound", query.toString());
        assertTrue(hasPrevious.isValue());
    }

    @Test
    void bindUpdatedAtBoundWithValidDates() {
        LocalDate lowerBound = LocalDate.of(2023, 1, 1);
        LocalDate upperBound = LocalDate.of(2023, 12, 31);

        when(executeSpec.bind(eq("updatedAtLowerBound"), eq(lowerBound))).thenReturn(executeSpec);
        when(executeSpec.bind(eq("updatedAtUpperBound"), eq(upperBound))).thenReturn(executeSpec);

        DatabaseClient.GenericExecuteSpec result = repositoryUtils.bindUpdatedAtBound(lowerBound, upperBound, executeSpec);

        verify(executeSpec).bind("updatedAtLowerBound", lowerBound);
        verify(executeSpec).bind("updatedAtUpperBound", upperBound);
        assertEquals(executeSpec, result);
    }
}