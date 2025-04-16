package com.mocicarazvan.gatewayservice.filters;

import com.mocicarazvan.gatewayservice.services.ErrorHandler;
import com.mocicarazvan.gatewayservice.services.NextCsrfValidator;
import com.mocicarazvan.gatewayservice.utils.CookieUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsrfFilterTest {

    @Mock
    private NextCsrfValidator nextCsrfValidator;

    @Mock
    private CookieUtils cookieUtils;

    @Mock
    private ErrorHandler errorHandler;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private HttpHeaders headers;

    @Mock
    private URI uri;

    @Mock
    private GatewayFilterChain chain;


    @InjectMocks
    private CsrfFilter csrfFilter;


    public static Stream<Arguments> notVerifyMethods() {
        return Stream.of(
                Arguments.of(HttpMethod.GET),
                Arguments.of(HttpMethod.HEAD),
                Arguments.of(HttpMethod.OPTIONS)
        );
    }

    @ParameterizedTest
    @MethodSource("notVerifyMethods")
    void allowsRequestForSafeHttpMethods(HttpMethod method) {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getMethod()).thenReturn(method);
        when(chain.filter(exchange)).thenReturn(Mono.empty());


        StepVerifier.create(csrfFilter.filter(exchange, chain))
                .verifyComplete();
        verify(chain).filter(exchange);
        verifyNoInteractions(nextCsrfValidator, cookieUtils, errorHandler);
    }

    @Test
    void rejectsRequestWhenCsrfTokenIsInvalid() {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getURI()).thenReturn(uri);
        when(uri.getPath()).thenReturn("");
        when(request.getHeaders()).thenReturn(headers);

        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(headers.getFirst(NextCsrfValidator.NEXT_CSRF_HEADER_TOKEN)).thenReturn(null);
        when(cookieUtils.getCookie(exchange, NextCsrfValidator.NEXT_CSRF_COOKIES)).thenReturn(null);
        when(nextCsrfValidator.validateCsrf(null, null, uri.getPath()))
                .thenReturn(Mono.just(false));
        when(errorHandler.handleError(anyString(), eq(exchange))).thenReturn(Mono.empty());

        StepVerifier.create(csrfFilter.filter(exchange, chain))
                .verifyComplete();
        verify(errorHandler).handleError("CSRF token is not valid", exchange);
        verifyNoInteractions(chain);
    }

    @Test
    void allowsRequestWhenCsrfTokenIsValid() {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getURI()).thenReturn(uri);
        when(uri.getPath()).thenReturn("");
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(headers.getFirst(NextCsrfValidator.NEXT_CSRF_HEADER_TOKEN)).thenReturn("validToken");
        when(headers.getFirst(NextCsrfValidator.NEXT_CSRF_HEADER)).thenReturn("rawToken");
        when(nextCsrfValidator.validateCsrf("validToken", "rawToken", uri.getPath()))
                .thenReturn(Mono.just(true));


        StepVerifier.create(csrfFilter.filter(exchange, chain))
                .verifyComplete();
        verify(chain).filter(exchange);
        verifyNoInteractions(errorHandler);
    }

    @Test
    void retrievesCsrfTokenFromCookieWhenHeaderIsMissing() {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getURI()).thenReturn(uri);
        when(uri.getPath()).thenReturn("");
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(headers.getFirst(NextCsrfValidator.NEXT_CSRF_HEADER_TOKEN)).thenReturn(null);
        when(cookieUtils.getCookie(exchange, NextCsrfValidator.NEXT_CSRF_COOKIES)).thenReturn("cookieToken");
        when(headers.getFirst(NextCsrfValidator.NEXT_CSRF_HEADER)).thenReturn("rawToken");
        when(nextCsrfValidator.validateCsrf("cookieToken", "rawToken", uri.getPath()))
                .thenReturn(Mono.just(true));


        StepVerifier.create(csrfFilter.filter(exchange, chain))
                .verifyComplete();
        verify(chain).filter(exchange);
        verifyNoInteractions(errorHandler);
    }
}