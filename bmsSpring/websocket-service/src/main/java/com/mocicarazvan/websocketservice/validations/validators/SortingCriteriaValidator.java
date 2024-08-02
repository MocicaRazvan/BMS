package com.mocicarazvan.websocketservice.validations.validators;

import com.mocicarazvan.websocketservice.validations.SortingCriteria;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

public class SortingCriteriaValidator implements ConstraintValidator<SortingCriteria, Map<String, String>> {

    @Override
    public boolean isValid(Map<String, String> sortingCriteria, ConstraintValidatorContext context) {
        if (sortingCriteria == null || sortingCriteria.isEmpty()) {
            return true;
        }

        String uri = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getRequestURI();

        List<String> allowedFields = fields(uri);
        if (allowedFields == null) {
            return true;
        }

        return sortingCriteria.keySet()
                .stream()
                .allMatch(key ->
                        allowedFields.contains(key) &&
                                (Objects.equals(sortingCriteria.get(key), "asc")
                                        || Objects.equals(sortingCriteria.get(key), "desc")));
    }

    private List<String> fields(String uri) {
        List<String> resp = new ArrayList<>(Arrays.asList("createdAt", "updatedAt", "id"));

        String[] split = uri.split("/");
        String modelName_s = split.length > 1 ? split[1] : null;
        if (modelName_s == null) {
            return null;
        } else if (Objects.equals(modelName_s, "users")) {

            addUserCriteria(resp, "firstName", "lastName", "email");
            return resp;

        } else {
            addUserCriteria(resp, "user.firstName", "user.lastName", "user.email");
            resp.add("title");
            resp.add("body");
        }
        return resp;
    }

    private void addUserCriteria(List<String> resp, String e, String e1, String e2) {
        resp.add(e);
        resp.add(e1);
        resp.add(e2);
    }
}
