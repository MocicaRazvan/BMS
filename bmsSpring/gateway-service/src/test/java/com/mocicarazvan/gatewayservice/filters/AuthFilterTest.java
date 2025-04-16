package com.mocicarazvan.gatewayservice.filters;

import com.mocicarazvan.gatewayservice.clients.UserClient;
import com.mocicarazvan.gatewayservice.dtos.token.TokenValidationResponse;
import com.mocicarazvan.gatewayservice.enums.Role;
import com.mocicarazvan.gatewayservice.routing.RouteValidator;
import com.mocicarazvan.gatewayservice.services.ErrorHandler;
import com.mocicarazvan.gatewayservice.services.NextJWEDecrypt;
import com.mocicarazvan.gatewayservice.utils.CookieUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthFilterTest {

    @Mock
    private UserClient userClient;
    @Mock
    private RouteValidator routeValidator;
    @Mock
    private ErrorHandler errorHandler;
    @Mock
    private NextJWEDecrypt nextJWEDecrypt;
    @Mock
    private CookieUtils cookieUtils;
    @Mock
    private GatewayFilterChain chain;

    private String[] NEXT_AUTH_COOKIES;


    @BeforeEach
    public void setUp() {
        NEXT_AUTH_COOKIES = (String[]) ReflectionTestUtils.getField(authFilter, "NEXT_AUTH_COOKIES");
    }

    @InjectMocks
    private AuthFilter authFilter;

    @Test
    public void optionsRequestShouldPassThroughWithoutValidation() {
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.OPTIONS, "/path").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(authFilter.filter(exchange, chain))
                .expectComplete()
                .verify();
    }

    @Test
    public void requestWithNoRequiredRoleShouldProceedWithoutValidation() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/path").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(routeValidator.getMinRole(request)).thenReturn(null);
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(authFilter.filter(exchange, chain))
                .expectComplete()
                .verify();
    }

    @Test
    public void validAuthorizationHeaderTokenShouldBeValidatedAndPass() {
        String token = "validToken";
        MockServerHttpRequest request = MockServerHttpRequest.get("/path")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(routeValidator.getMinRole(request)).thenReturn(Role.ROLE_USER);
        when(userClient.validateToken(any())).thenReturn(Mono.just(new TokenValidationResponse(true, 1L)));
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(authFilter.filter(exchange, chain))
                .expectComplete()
                .verify();
    }

    @Test
    public void missingTokenShouldReturnTokenNotFoundError() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/path").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(routeValidator.getMinRole(request)).thenReturn(Role.ROLE_USER);
        when(cookieUtils.getCookie(exchange, NEXT_AUTH_COOKIES)).thenReturn(null);
        when(nextJWEDecrypt.getTokenPayload(null)).thenReturn(Mono.just(NextJWEDecrypt.NULL_COOKIE));
        when(errorHandler.handleError("Token not found", exchange)).thenReturn(Mono.empty());
        StepVerifier.create(authFilter.filter(exchange, chain))
                .expectComplete()
                .verify();
    }

    @Test
    public void invalidTokenShouldReturnTokenNotValidError() {
        String token = "invalidToken";
        MockServerHttpRequest request = MockServerHttpRequest.get("/path")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(routeValidator.getMinRole(request)).thenReturn(Role.ROLE_USER);
        when(userClient.validateToken(any())).thenReturn(Mono.just(new TokenValidationResponse(false, null)));
        when(errorHandler.handleError("Token is not valid", exchange)).thenReturn(Mono.empty());
        StepVerifier.create(authFilter.filter(exchange, chain))
                .expectComplete()
                .verify();
    }

    @Test
    public void validCookieTokenShouldBeValidatedAndPass() {
        String cookieToken = "cookieValidToken";
        MockServerHttpRequest request = MockServerHttpRequest.get("/path").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(routeValidator.getMinRole(request)).thenReturn(Role.ROLE_USER);
        when(cookieUtils.getCookie(exchange, NEXT_AUTH_COOKIES)).thenReturn("encodedCookie");
        when(nextJWEDecrypt.getTokenPayload("encodedCookie")).thenReturn(Mono.just(cookieToken));
        when(userClient.validateToken(any())).thenReturn(Mono.just(new TokenValidationResponse(true, 2L)));
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(authFilter.filter(exchange, chain))
                .expectComplete()
                .verify();
    }
}


