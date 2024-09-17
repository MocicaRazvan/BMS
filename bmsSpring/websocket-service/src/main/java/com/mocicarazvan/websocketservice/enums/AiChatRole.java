package com.mocicarazvan.websocketservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AiChatRole {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
    FUNCTION("function"),
    DATA("data"),
    TOOL("tool");

    private final String role;

    AiChatRole(String role) {
        this.role = role;
    }

    @JsonValue
    public String getRole() {
        return role;
    }

    @JsonCreator
    public static AiChatRole fromRole(String role) {
        for (AiChatRole aiChatRole : AiChatRole.values()) {
            if (aiChatRole.role.equalsIgnoreCase(role)) {
                return aiChatRole;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + role);
    }
}