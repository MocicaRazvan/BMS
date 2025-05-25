package com.mocicarazvan.templatemodule.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.r2dbc.core.DatabaseClient;

import java.lang.reflect.Array;
import java.text.Normalizer;
import java.time.LocalDate;
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

    public void addField(StringBuilder queryBuilder, MutableBoolean hasPreviousCriteria, String clause) {
        addNotNullField(true, queryBuilder, hasPreviousCriteria, clause);
    }

    public <T> void addNotNullField(T field, StringBuilder queryBuilder, MutableBoolean hasPreviousCriteria, String clause, Boolean isOr) {
        if (field != null) {
            if (hasPreviousCriteria.isValue()) {
                queryBuilder.append(isOr ? " OR " : " AND ");
            } else {
                queryBuilder.append(" WHERE ");
                hasPreviousCriteria.setValue(true);
            }
            queryBuilder.append(clause);
        }
    }

    public void addStringField(String field, StringBuilder queryBuilder, MutableBoolean hasPreviousCriteria, String clause, Boolean isOr) {
        if (isNotNullOrEmpty(field)) {
            addNotNullField(field, queryBuilder, hasPreviousCriteria, clause, isOr);
        }
    }

    public void addStringField(String field, StringBuilder queryBuilder, MutableBoolean hasPreviousCriteria, String clause) {
        if (isNotNullOrEmpty(field)) {
            addNotNullField(field, queryBuilder, hasPreviousCriteria, clause);
        }
    }


    public void addListField(List<?> field, StringBuilder queryBuilder, MutableBoolean hasPreviousCriteria, String clause) {
        if (field != null && !field.isEmpty()) {
            addNotNullField(field, queryBuilder, hasPreviousCriteria, clause);
        }
    }

    public String createExtraOrder(String field, String fieldName, String clause) {
        if (isNotNullOrEmpty(field)) {
            return " similarity(" + fieldName + " , " + clause + " ) DESC ";
        }
        return "";
    }


    public <T> DatabaseClient.GenericExecuteSpec bindNotNullField(T field, DatabaseClient.GenericExecuteSpec executeSpec, String name) {
        if (field != null) {
            executeSpec = executeSpec.bind(name, field);
        }
        return executeSpec;
    }

    public DatabaseClient.GenericExecuteSpec bindStringField(String field, DatabaseClient.GenericExecuteSpec executeSpec, String name) {
        if (isNotNullOrEmpty(field)) {
            return bindNotNullField(Normalizer.normalize(field, Normalizer.Form.NFKD), executeSpec, name);
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
        if (isNotNullOrEmpty(field)) {
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

    @SuppressWarnings("unchecked")
    public <T> DatabaseClient.GenericExecuteSpec bindArrayField(List<?> field, DatabaseClient.GenericExecuteSpec executeSpec, String name, Class<T> arrayClass) {
        if (field != null && !field.isEmpty()) {
            T[] array = field.toArray((T[]) Array.newInstance(arrayClass, field.size()));
            return executeSpec.bind(name, array);
        }
        return executeSpec;
    }

    public boolean isNotNullOrEmpty(String field) {
        return field != null && !field.isBlank();
    }

    public void addCreatedAtUpperBoundField(String tablePrefix, LocalDate field, StringBuilder queryBuilder, MutableBoolean hasPreviousCriteria) {
        addNotNullField(field, queryBuilder, hasPreviousCriteria, " " + tablePrefix + ".created_at <= :createdAtUpperBound");
    }

    public void addCreatedAtLowerBoundField(String tablePrefix, LocalDate field, StringBuilder queryBuilder, MutableBoolean hasPreviousCriteria) {
        addNotNullField(field, queryBuilder, hasPreviousCriteria, " " + tablePrefix + ".created_at >= :createdAtLowerBound");
    }

    public void addUpdatedAtUpperBoundField(String tablePrefix, LocalDate field, StringBuilder queryBuilder, MutableBoolean hasPreviousCriteria) {
        addNotNullField(field, queryBuilder, hasPreviousCriteria, " " + tablePrefix + ".updated_at <= :updatedAtUpperBound");
    }

    public void addUpdatedAtLowerBoundField(String tablePrefix, LocalDate field, StringBuilder queryBuilder, MutableBoolean hasPreviousCriteria) {
        addNotNullField(field, queryBuilder, hasPreviousCriteria, " " + tablePrefix + ".updated_at >= :updatedAtLowerBound");
    }

    public DatabaseClient.GenericExecuteSpec bindCreatedAtUpperBoundField(LocalDate field, DatabaseClient.GenericExecuteSpec executeSpec) {
        return bindNotNullField(field, executeSpec, "createdAtUpperBound");
    }

    public DatabaseClient.GenericExecuteSpec bindCreatedAtLowerBoundField(LocalDate field, DatabaseClient.GenericExecuteSpec executeSpec) {
        return bindNotNullField(field, executeSpec, "createdAtLowerBound");
    }

    public DatabaseClient.GenericExecuteSpec bindUpdatedAtUpperBoundField(LocalDate field, DatabaseClient.GenericExecuteSpec executeSpec) {
        return bindNotNullField(field, executeSpec, "updatedAtUpperBound");
    }

    public DatabaseClient.GenericExecuteSpec bindUpdatedAtLowerBoundField(LocalDate field, DatabaseClient.GenericExecuteSpec executeSpec) {
        return bindNotNullField(field, executeSpec, "updatedAtLowerBound");
    }

    public void addCreatedAtBound(String tablePrefix, LocalDate lowerBound, LocalDate upperBound, StringBuilder queryBuilder, MutableBoolean hasPreviousCriteria) {
        addCreatedAtLowerBoundField(tablePrefix, lowerBound, queryBuilder, hasPreviousCriteria);
        addCreatedAtUpperBoundField(tablePrefix, upperBound, queryBuilder, hasPreviousCriteria);
    }

    public void addUpdatedAtBound(String tablePrefix, LocalDate lowerBound, LocalDate upperBound, StringBuilder queryBuilder, MutableBoolean hasPreviousCriteria) {
        addUpdatedAtLowerBoundField(tablePrefix, lowerBound, queryBuilder, hasPreviousCriteria);
        addUpdatedAtUpperBoundField(tablePrefix, upperBound, queryBuilder, hasPreviousCriteria);
    }

    public DatabaseClient.GenericExecuteSpec bindCreatedAtBound(LocalDate lowerBound, LocalDate upperBound, DatabaseClient.GenericExecuteSpec executeSpec) {
        executeSpec = bindCreatedAtLowerBoundField(lowerBound, executeSpec);
        executeSpec = bindCreatedAtUpperBoundField(upperBound, executeSpec);
        return executeSpec;
    }

    public DatabaseClient.GenericExecuteSpec bindUpdatedAtBound(LocalDate lowerBound, LocalDate upperBound, DatabaseClient.GenericExecuteSpec executeSpec) {
        executeSpec = bindUpdatedAtLowerBoundField(lowerBound, executeSpec);
        executeSpec = bindUpdatedAtUpperBoundField(upperBound, executeSpec);
        return executeSpec;
    }


}
