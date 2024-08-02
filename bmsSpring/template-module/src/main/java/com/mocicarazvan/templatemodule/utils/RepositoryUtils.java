package com.mocicarazvan.templatemodule.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.r2dbc.core.DatabaseClient;

import java.util.List;

public class RepositoryUtils {

    @Data
    @AllArgsConstructor
    public static class MutableBoolean {
        private boolean value;
    }

    public <T> void addNotNullField(T field, StringBuilder queryBuilder, MutableBoolean hasPreviousCriteria, String clause) {
        if (field != null) {
            if (hasPreviousCriteria.isValue()) {
                queryBuilder.append(" AND ");
            } else {
                queryBuilder.append(" WHERE ");
                hasPreviousCriteria.setValue(true);
            }
            queryBuilder.append(clause);
        }
    }

    public void addStringField(String field, StringBuilder queryBuilder, MutableBoolean hasPreviousCriteria, String clause) {
        if (field != null && !field.isEmpty()) {
            addNotNullField(field, queryBuilder, hasPreviousCriteria, clause);
        }
    }


    public void addListField(List<?> field, StringBuilder queryBuilder, MutableBoolean hasPreviousCriteria, String clause) {
        if (field != null && !field.isEmpty()) {
            addNotNullField(field, queryBuilder, hasPreviousCriteria, clause);
        }
    }

    public <T> DatabaseClient.GenericExecuteSpec bindNotNullField(T field, DatabaseClient.GenericExecuteSpec executeSpec, String name) {
        if (field != null) {
            executeSpec = executeSpec.bind(name, field);
        }
        return executeSpec;
    }

    public DatabaseClient.GenericExecuteSpec bindStringField(String field, DatabaseClient.GenericExecuteSpec executeSpec, String name) {
        if (field != null && !field.isEmpty()) {
            return bindNotNullField(field, executeSpec, name);
        }
        return executeSpec;
    }

    public <E extends Enum<E>> DatabaseClient.GenericExecuteSpec bindEnumField(E field, DatabaseClient.GenericExecuteSpec executeSpec, String name) {
        if (field != null) {
            return executeSpec.bind(name, field.name());
        }
        return executeSpec;
    }

    public DatabaseClient.GenericExecuteSpec bindStringSearchField(String field, DatabaseClient.GenericExecuteSpec executeSpec, String name) {
        if (field != null && !field.isEmpty()) {
            return executeSpec.bind(name, "%" + field + "%");
        }
        return executeSpec;
    }

    public DatabaseClient.GenericExecuteSpec bindListField(List<?> field, DatabaseClient.GenericExecuteSpec executeSpec, String name) {
        if (field != null && !field.isEmpty()) {
            return bindNotNullField(field, executeSpec, name);
        }
        return executeSpec;
    }
}
