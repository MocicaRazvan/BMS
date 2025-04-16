package com.mocicarazvan.templatemodule.utils;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.exceptions.notFound.AuthHeaderNotFound;
import com.mocicarazvan.templatemodule.models.IdGeneratedImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestsUtilsTest {

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    private final RequestsUtils requestsUtils = new RequestsUtils();

    @Test
    void extractAuthUser_present() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(RequestsUtils.AUTH_HEADER, "value");
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(new HttpHeaders(headers));
        assertDoesNotThrow(() -> requestsUtils.extractAuthUser(exchange));
    }

    @Test
    void extractAuthUser_notPresent() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("key", "value");
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(new HttpHeaders(headers));
        assertThrows(AuthHeaderNotFound.class, () -> requestsUtils.extractAuthUser(exchange));
    }

    @Test
    void getBodyFromJson() {
        var date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        var model = IdGeneratedImpl.builder()
                .id(1L)
                .createdAt(date)
                .updatedAt(date)
                .build();
        String json = "{\"id\":1,\"createdAt\":\"" + date + "\",\"updatedAt\":\"" + date + "\"}";
        var objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        StepVerifier.create(requestsUtils.getBodyFromJson(json, IdGeneratedImpl.class, objectMapper))
                .expectSubscription()
                .expectNext(model)
                .verifyComplete();

    }

    @Test
    void getBodyFromJson_propagatesError() {
        var date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String invalidJson = "{\"id\":1,\"createdAt\":\"" + date + "\",\"updatedAt\":\"" + date + "\"";
        var objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        StepVerifier.create(requestsUtils.getBodyFromJson(invalidJson, IdGeneratedImpl.class, objectMapper))
                .expectSubscription()
                .expectErrorMatches(throwable -> {
                    assertInstanceOf(JsonEOFException.class, throwable);
                    return true;
                })
                .verify();

    }

    @ParameterizedTest
    @MethodSource("provideObjectsForFiltering")
    void testGetListOfNotNullObjects(Object[] input, List<Object> expected) {
        List<Object> result = requestsUtils.getListOfNotNullObjects(input);
        assertEquals(expected, result);
    }

    static Stream<Arguments> provideObjectsForFiltering() {
        Object customObj = new Object();

        return Stream.of(
                Arguments.of(new Object[]{"a", 1, true}, List.of("a", 1, true)),
                Arguments.of(new Object[]{"a", null, 42, null}, List.of("a", 42)),
                Arguments.of(new Object[]{null, null, null}, List.of()),
                Arguments.of(new Object[]{}, List.of()),
                Arguments.of(new Object[]{"text", 123, null, customObj}, List.of("text", 123, customObj))
        );
    }

}