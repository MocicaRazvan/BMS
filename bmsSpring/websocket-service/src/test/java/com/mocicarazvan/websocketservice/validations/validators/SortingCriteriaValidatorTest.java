package com.mocicarazvan.websocketservice.validations.validators;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SortingCriteriaValidatorTest {

    private SortingCriteriaValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    public void setUp() {
        validator = new SortingCriteriaValidator();
        context = Mockito.mock(ConstraintValidatorContext.class);
    }

    @AfterEach
    public void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void isValidReturnsTrueWhenSortingCriteriaIsNull() {
        setRequestURI("/users");
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void isValidReturnsTrueWhenSortingCriteriaIsEmpty() {
        setRequestURI("/users");
        assertTrue(validator.isValid(new HashMap<>(), context));
    }

    @Test
    void isValidReturnsTrueForValidSortingCriteriaForUsersURI() {
        setRequestURI("/users");
        Map<String, String> sortingCriteria = new HashMap<>();
        sortingCriteria.put("id", "asc");
        sortingCriteria.put("firstName", "desc");
        sortingCriteria.put("lastName", "asc");
        sortingCriteria.put("email", "desc");
        assertTrue(validator.isValid(sortingCriteria, context));
    }

    @Test
    void isValidReturnsFalseForInvalidFieldInSortingCriteriaForUsersURI() {
        setRequestURI("/users");
        Map<String, String> sortingCriteria = new HashMap<>();
        sortingCriteria.put("nonExistingField", "asc");
        sortingCriteria.put("email", "desc");
        assertFalse(validator.isValid(sortingCriteria, context));
    }

    @Test
    void isValidReturnsFalseForInvalidSortOrderValue() {
        setRequestURI("/users");
        Map<String, String> sortingCriteria = new HashMap<>();
        sortingCriteria.put("firstName", "ascending");
        assertFalse(validator.isValid(sortingCriteria, context));
    }

    @Test
    void isValidReturnsTrueForValidSortingCriteriaForOtherURIs() {
        setRequestURI("/posts");
        Map<String, String> sortingCriteria = new HashMap<>();
        sortingCriteria.put("id", "asc");
        sortingCriteria.put("user.firstName", "desc");
        sortingCriteria.put("user.lastName", "asc");
        sortingCriteria.put("user.email", "desc");
        sortingCriteria.put("title", "asc");
        sortingCriteria.put("body", "desc");
        assertTrue(validator.isValid(sortingCriteria, context));
    }

    @Test
    void isValidReturnsFalseForInvalidFieldInSortingCriteriaForOtherURIs() {
        setRequestURI("/posts");
        Map<String, String> sortingCriteria = new HashMap<>();
        sortingCriteria.put("nonExistingField", "asc");
        assertFalse(validator.isValid(sortingCriteria, context));
    }

    private void setRequestURI(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}