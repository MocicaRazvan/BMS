package com.mocicarazvan.gatewayservice.filters;


import com.mocicarazvan.gatewayservice.services.ErrorHandler;
import com.mocicarazvan.gatewayservice.services.NextCsrfValidator;
import com.mocicarazvan.gatewayservice.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CsrfFilter implements GatewayFilter {
    private final NextCsrfValidator nextCsrfValidator;
    private final CookieUtils cookieUtils;
    private final ErrorHandler errorHandler;


    public CsrfFilter(NextCsrfValidator nextCsrfValidator, CookieUtils cookieUtils, ErrorHandler errorHandler) {
        this.nextCsrfValidator = nextCsrfValidator;
        this.cookieUtils = cookieUtils;
        this.errorHandler = errorHandler;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS
                || exchange.getRequest().getMethod() == HttpMethod.GET
                || exchange.getRequest().getMethod() == HttpMethod.HEAD
        ) {
            return chain.filter(exchange);
        }

        String csrfToken = exchange.getRequest().getHeaders().getFirst(NextCsrfValidator.NEXT_CSRF_HEADER_TOKEN);
        if (csrfToken == null) {
            csrfToken = cookieUtils.getCookie(exchange, NextCsrfValidator.NEXT_CSRF_COOKIES);
        }
        String rawToken = exchange.getRequest().getHeaders().getFirst(NextCsrfValidator.NEXT_CSRF_HEADER);


        return nextCsrfValidator.validateCsrf(csrfToken, rawToken, exchange.getRequest().getURI().getPath())
                .flatMap(isValid -> {
//                    log.info("CSRF Token is valid: {} for request {} with method {}", isValid, exchange.getRequest().getURI().getPath(), exchange.getRequest().getMethod());
                    if (!isValid) {
                        return errorHandler.handleError("CSRF token is not valid", exchange);
                    }
                    return chain.filter(exchange);
                });
    }


}
