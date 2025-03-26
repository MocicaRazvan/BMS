package com.mocicarazvan.gatewayservice.routing;

import com.mocicarazvan.gatewayservice.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class RouteValidator {

    private final AntPathMatcher antPathMatcher;

    private static final List<String> AUTH_WHITELIST = List.of(
            "/auth/login",
            "/auth/register",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/**",
            "/user-service/v3/api-docs",
            "/post-service/v3/api-docs",
            "/comment-service/v3/api-docs",
            "/recipe-service/v3/api-docs",
            "/websocket-service/v3/api-docs",
            "/file-service/v3/api-docs",
            "/order-service/v3/api-docs",
            "/plan-service/v3/api-docs",
            "/ingredient-service/v3/api-docs",
            "/day-service/v3/api-docs",
            "/archive-service/v3/api-docs",
            "/user-service/actuator/**",
            "/post-service/actuator/**",
            "/comment-service/actuator/**",
            "/recipe-service/actuator/**",
            "/websocket-service/actuator/**",
            "/file-service/actuator/**",
            "/order-service/actuator/**",
            "/plan-service/actuator/**",
            "/ingredient-service/actuator/**",
            "/day-service/actuator/**",
            "/archive-service/actuator/**",
            "/webjars/**",
            "/favicon.ico",
            "/auth/github/callback",
            "/auth/google/callback", "/auth/google/login",
            "/auth/resetPassword", "/auth/changePassword", "/auth/**",
            "/files/download/**", "/orders/webhook/**",
            "/orders/overall", "/posts/viewCount/**"
    );

    private static final List<String> TRAINER_LIST = List.of("/test/trainer",

            "/posts/create",
            "/posts/create/withImages",
            "/posts/update/**",
            "/posts/trainer/**",
            "/posts/delete/**",
            "/posts/createWithImages",
            "/posts/createWithImages/**",
//            "/posts/withUser", "/posts/tags/withUser",

            "/recipes/create",
            "/recipes/create/withImages",
            "/recipes/update/**",
            "/recipes/trainer/**",
            "/recipes/delete/**",
            "/recipes/createWithImages",
            "/recipes/createWithImages/**",
            "/recipes/createWithImagesAndVideos",
            "/recipes/createWithImagesAndVideos/**",

            "/plans/create",
            "/plans/update/**",
            "/plans/trainer/**",
            "/plans/delete/**",
            "/plans/createWithImages",
            "/plans/createWithImages/**",
            "/plans/createWithImagesAndVideos",
            "/plans/createWithImagesAndVideos/**",

            "/days/create",
            "/days/update/**",
            "/days/trainer/**",
            "/days/delete/**",


            "/meals/create",
            "/meals/update/**",
            "/meals/trainer/**",
            "/meals/delete/**",


            "/orders/create",
            "/orders/trainer/**",

            "/diffusion/**"
    );

    private static final List<String> ADMIN_LIST = List.of("/test/admin",
            "/posts/admin/**",
            "/ingredients/admin/**",
            "/users/admin/**",
            "/plans/admin/**",
            "/orders/admin/**",
            "/recipes/admin/**",
            "/invoices/**",

            "/ingredients/admin/**",
            "/ingredients/delete/**",
            "/ingredients/update/**",
            "/ingredients/create/**",
            "/ingredients/alterDisplay/**",

            "/kanban/admin/**",

            "/days/admin/**",
            "/meals/admin/**",


            "/archive/**"
    );

    public Predicate<ServerHttpRequest> isWhitelisted() {
        return r -> AUTH_WHITELIST.stream().anyMatch(uri -> antPathMatcher.match(uri, r.getURI().getPath()));
    }

    public Predicate<ServerHttpRequest> isTrainer() {
        return r -> TRAINER_LIST.stream().anyMatch(uri -> antPathMatcher.match(uri, r.getURI().getPath()));
    }

    public Predicate<ServerHttpRequest> isAdmin() {
        return r -> ADMIN_LIST.stream().anyMatch(uri -> antPathMatcher.match(uri, r.getURI().getPath()));
    }

    public Predicate<ServerHttpRequest> isUser() {
        return r -> !isWhitelisted().test(r) && !isTrainer().test(r) && !isAdmin().test(r);
    }

    public Role getMinRole(ServerHttpRequest request) {
        if (isWhitelisted().test(request)) {
            return null;
        }
        if (isUser().test(request)) {
            return Role.ROLE_USER;
        }
        if (isTrainer().test(request)) {
            return Role.ROLE_TRAINER;
        }
        return Role.ROLE_ADMIN;
    }
}
