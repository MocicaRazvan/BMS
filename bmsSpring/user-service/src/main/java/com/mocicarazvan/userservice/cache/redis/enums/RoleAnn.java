package com.mocicarazvan.userservice.cache.redis.enums;

import com.mocicarazvan.templatemodule.enums.Role;
import lombok.Getter;

@Getter
public enum RoleAnn {
    ROLE_USER_ANN("role_user"),
    ROLE_TRAINER_ANN("role_trainer"),
    ROLE_ADMIN_ANN("role_admin"),
    NULL_ANN("null");

    private final String role;

    RoleAnn(String roleUser) {
        this.role = roleUser;
    }

    public static RoleAnn fromRole(Role role) {
        if (role == null) {
            return NULL_ANN;
        }
        return switch (role) {
            case ROLE_USER -> ROLE_USER_ANN;
            case ROLE_TRAINER -> ROLE_TRAINER_ANN;
            case ROLE_ADMIN -> ROLE_ADMIN_ANN;
            default -> NULL_ANN;
        };
    }

    public static RoleAnn fromString(String role) {
        return switch (role) {
            case "role_user" -> ROLE_USER_ANN;
            case "role_trainer" -> ROLE_TRAINER_ANN;
            case "role_admin" -> ROLE_ADMIN_ANN;
            case "null" -> NULL_ANN;
            default -> throw new IllegalArgumentException("Unexpected value: " + role);
        };
    }

    public static RoleAnn fromObject(Object role) {
        return switch (role) {
            case null -> NULL_ANN;
            case String s -> fromString(s);
            case Role r -> fromRole(r);
            default -> throw new IllegalArgumentException("Unexpected value: " + role);
        };
    }
}
