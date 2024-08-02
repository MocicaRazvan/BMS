package com.mocicarazvan.templatemodule.exceptions.common;

import lombok.Getter;

import java.util.Map;

@Getter
public class SortingCriteriaException extends RuntimeException {
    private final Map<String, String> invalidCriteria;

    public SortingCriteriaException(String message, Map<String, String> invalidCriteria) {
        super(message);
        this.invalidCriteria = invalidCriteria;
    }

}