package com.mocicarazvan.rediscache.enums;

import lombok.Getter;

@Getter
public enum BooleanEnum {
    TRUE("true"),
    FALSE("false"),
    NULL("null");

    private final String value;

    BooleanEnum(String value) {
        this.value = value;
    }


    public static BooleanEnum fromBoolean(Boolean value) {
        if (value == null) {
            return NULL;
        }
        return value ? TRUE : FALSE;
    }

    public static BooleanEnum fromString(String value) {
        if (value == null) {
            return NULL;
        }
        if (value.trim().equalsIgnoreCase("true")) {
            return TRUE;
        } else if (value.trim().equalsIgnoreCase("false")) {
            return FALSE;
        } else {
            throw new IllegalArgumentException("Invalid value for BooleanEnum: " + value);
        }
    }

    public static BooleanEnum fromObject(Object value) {
        return switch (value) {
            case null -> NULL;
            case Boolean b -> fromBoolean(b);
            case String s -> fromString(s);
            default -> throw new IllegalArgumentException("Invalid value for BooleanEnum: " + value);
        };
    }

    public static Boolean toBoolean(BooleanEnum value) {
        if (value == null) {
            return null;
        }

        return value == TRUE;
    }
}
