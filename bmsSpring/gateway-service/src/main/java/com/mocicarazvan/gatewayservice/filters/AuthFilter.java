package com.mocicarazvan.gatewayservice.filters;


import com.mocicarazvan.gatewayservice.clients.UserClient;
import com.mocicarazvan.gatewayservice.dtos.TokenValidationRequest;
import com.mocicarazvan.gatewayservice.enums.Role;
import com.mocicarazvan.gatewayservice.routing.RouteValidator;
import com.mocicarazvan.gatewayservice.services.ErrorHandler;
import com.mocicarazvan.gatewayservice.services.NextJWEDecrypt;
import com.mocicarazvan.gatewayservice.utils.CookieUtils;
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
    private final NextJWEDecrypt nextJWEDecrypt;
    private final CookieUtils cookieUtils;

    private static final String[] NEXT_AUTH_COOKIES = {
            "__Secure-next-auth.session-token"
            , "next-auth.session-token"
    };


    public AuthFilter(UserClient userClient, RouteValidator routeValidator, ErrorHandler errorHandler,
                      NextJWEDecrypt nextJWEDecrypt, CookieUtils cookieUtils) {
        this.userClient = userClient;
        this.routeValidator = routeValidator;
        this.errorHandler = errorHandler;
        this.nextJWEDecrypt = nextJWEDecrypt;
        this.cookieUtils = cookieUtils;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            log.error("Request: {}", exchange.getRequest().getMethod());
            return chain.filter(exchange);
        }


        return validateExchange(exchange, chain);


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

        final String encodedCookie = cookieUtils.getCookie(exchange, NEXT_AUTH_COOKIES);

        log.info("Encoded Cookie: {}", encodedCookie);
//        if ((authHeader == null || !authHeader.startsWith("Bearer ")) && (authCookie == null || authCookie.isEmpty())) {
//            return handleError("Token not found", exchange);
//        }

        boolean isAuthHeaderPresent = authHeader != null && authHeader.startsWith("Bearer ");

        Mono<String> authCookieMono = isAuthHeaderPresent ? Mono.just(NextJWEDecrypt.NULL_COOKIE) : nextJWEDecrypt
                .getTokenPayload(encodedCookie);


        return authCookieMono
                .flatMap(authCookie -> {

                    log.info("AuthCookie: {}", authCookie);

                    if (!isAuthHeaderPresent &&
                            (Objects.equals(authCookie, NextJWEDecrypt.NULL_COOKIE)) &&
                            (authQueryParam == null || authQueryParam.isEmpty())) {
                        return errorHandler.handleError("Token not found", exchange);
                    }

//        final String token = authHeader != null ? authHeader.substring(7) : authCookie;

                    final String token = isAuthHeaderPresent ? authHeader.substring(7) :
                            !Objects.equals(authCookie, NextJWEDecrypt.NULL_COOKIE) ? authCookie :
                                    authQueryParam;

                    TokenValidationRequest request = TokenValidationRequest.builder()
                            .token(token).minRoleRequired(role).build();

                    return validateToken(exchange, chain, request);

                });
    }

    private Mono<Void> validateToken(ServerWebExchange exchange, GatewayFilterChain chain, TokenValidationRequest request) {
        return userClient.validateToken(request)
                .flatMap(resp -> {
                    if (!resp.isValid()) {
                        return errorHandler.handleError("Token is not valid", exchange);
                    }
                    exchange.getRequest().mutate()
                            .header("x-auth-user-id", resp.getUserId().toString());
                    return chain.filter(exchange);

                });
    }


}
