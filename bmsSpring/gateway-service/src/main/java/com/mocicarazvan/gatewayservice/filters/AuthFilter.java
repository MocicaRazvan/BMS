package com.mocicarazvan.gatewayservice.filters;


import com.mocicarazvan.gatewayservice.clients.UserClient;
import com.mocicarazvan.gatewayservice.dtos.TokenValidationRequest;
import com.mocicarazvan.gatewayservice.enums.Role;
import com.mocicarazvan.gatewayservice.routing.RouteValidator;
import com.mocicarazvan.gatewayservice.services.ErrorHandler;
import com.mocicarazvan.gatewayservice.services.NextAuthDecrypt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
@Slf4j
public class AuthFilter implements GatewayFilter {


    private final UserClient userClient;
    private final RouteValidator routeValidator;
    private final ErrorHandler errorHandler;
    private final NextAuthDecrypt nextAuthDecrypt;

    private static final String[] NEXT_AUTH_COOKIES = {
            "__Secure-next-auth.session-token"
            , "next-auth.session-token"
    };

    private static final String[] NEXT_CSRF_COOKIES = {
            "__Host-next-auth.csrf-token"
            , "next-auth.csrf-token"
    };

    public AuthFilter(UserClient userClient, RouteValidator routeValidator, ErrorHandler errorHandler, NextAuthDecrypt nextAuthDecrypt) {
        this.userClient = userClient;
        this.routeValidator = routeValidator;
        this.errorHandler = errorHandler;
        this.nextAuthDecrypt = nextAuthDecrypt;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            log.error("Request: {}", exchange.getRequest().getMethod());
            return chain.filter(exchange);
        }

        if (exchange.getRequest().getMethod() == HttpMethod.GET
                || exchange.getRequest().getMethod() == HttpMethod.HEAD
        ) {
            return validateExchange(exchange, chain);
        }

        return nextAuthDecrypt.validateCsrf(getCookie(exchange, NEXT_CSRF_COOKIES), exchange.getRequest().getURI().getPath())
                .flatMap(isValid -> {
                    log.info("CSRF Token is valid: {} for request {} with method {}", isValid, exchange.getRequest().getURI().getPath(), exchange.getRequest().getMethod());
                    if (!isValid) {
                        return errorHandler.handleError("CSRF token is not valid", exchange);
                    }
                    return validateExchange(exchange, chain);
                });


    }

    private Mono<Void> validateExchange(ServerWebExchange exchange, GatewayFilterChain chain) {
        Role role = routeValidator.getMinRole(exchange.getRequest());
        log.info("Role: {}", role);
        if (role == null) {
            return chain.filter(exchange);
        }


        final String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        log.error(exchange.getRequest().getHeaders().toString());

        log.info("AuthHeader: {}", authHeader);

        final String authQueryParam = exchange.getRequest().getQueryParams().getFirst("authToken");

        log.info("AuthQueryParam: {}", authQueryParam);

        final String encodedCookie = getCookie(exchange, NEXT_AUTH_COOKIES);

        log.info("Encoded Cookie: {}", encodedCookie);
//        if ((authHeader == null || !authHeader.startsWith("Bearer ")) && (authCookie == null || authCookie.isEmpty())) {
//            return handleError("Token not found", exchange);
//        }

        boolean isAuthHeaderPresent = authHeader != null && authHeader.startsWith("Bearer ");

        Mono<String> authCookieMono = isAuthHeaderPresent ? Mono.just(NextAuthDecrypt.NULL_COOKIE) : nextAuthDecrypt
                .getTokenPayload(encodedCookie);


        return authCookieMono
                .flatMap(authCookie -> {

                    log.info("AuthCookie: {}", authCookie);

                    if (!isAuthHeaderPresent &&
                            (Objects.equals(authCookie, NextAuthDecrypt.NULL_COOKIE)) &&
                            (authQueryParam == null || authQueryParam.isEmpty())) {
                        return errorHandler.handleError("Token not found", exchange);
                    }

//        final String token = authHeader != null ? authHeader.substring(7) : authCookie;

                    final String token = isAuthHeaderPresent ? authHeader.substring(7) :
                            !Objects.equals(authCookie, NextAuthDecrypt.NULL_COOKIE) ? authCookie :
                                    authQueryParam;

                    TokenValidationRequest request = TokenValidationRequest.builder()
                            .token(token).minRoleRequired(role).build();

                    return userClient.validateToken(request)
                            .flatMap(resp -> {
                                if (!resp.isValid()) {
                                    return errorHandler.handleError("Token is not valid", exchange);
                                }
                                exchange.getRequest().mutate()
                                        .header("x-auth-user-id", resp.getUserId().toString());
                                return chain.filter(exchange);

                            });

                });
    }

    private String getCookie(ServerWebExchange exchange, String[] cookieNames) {
        for (String cookie : cookieNames) {
            if (exchange.getRequest().getCookies().getFirst(cookie) != null) {
                return Objects.requireNonNull(exchange.getRequest().getCookies().getFirst(cookie)).getValue();
            }
        }
        return null;
    }


}
