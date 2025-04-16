package com.mocicarazvan.gatewayservice.routing;

import com.mocicarazvan.gatewayservice.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.util.AntPathMatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RouteValidatorTest {

    private final RouteValidator routeValidator = new RouteValidator(new AntPathMatcher());


    @Test
    void whitelistedUriReturnsNullRole() {
        ServerHttpRequest request = MockServerHttpRequest.get("/auth/login").build();
        assertNull(routeValidator.getMinRole(request));
    }

    @Test
    void nonMatchingUriReturnsUserRole() {
        ServerHttpRequest request = MockServerHttpRequest.get("/some/other/endpoint").build();
        assertEquals(Role.ROLE_USER, routeValidator.getMinRole(request));
    }

    @Test
    void trainerUriReturnsTrainerRole() {
        ServerHttpRequest request = MockServerHttpRequest.get("/posts/create").build();
        assertEquals(Role.ROLE_TRAINER, routeValidator.getMinRole(request));
    }

    @Test
    void adminUriReturnsAdminRole() {
        ServerHttpRequest request = MockServerHttpRequest.get("/users/admin/manage").build();
        assertEquals(Role.ROLE_ADMIN, routeValidator.getMinRole(request));
    }

    @Test
    void wildcardWhitelistedUriMatchesProperly() {
        ServerHttpRequest request = MockServerHttpRequest.get("/swagger-ui/index.html").build();
        assertNull(routeValidator.getMinRole(request));
    }

    @Test
    void edgeCaseUriMatchingOrderDoesNotOverrideWhitelist() {
        ServerHttpRequest request = MockServerHttpRequest.get("/auth/google/callback").build();
        assertNull(routeValidator.getMinRole(request));
    }
}