package com.mocicarazvan.templatemodule.controllers;

import com.mocicarazvan.templatemodule.services.MockItemService;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public abstract class MockItemController<T> {

    private final MockItemService<T> mockItemService;
    private final RequestsUtils requestsUtils;

    @PostMapping("/{itemId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Flux<T> mockPost(
            @PathVariable Long itemId,
            @RequestParam(required = false, defaultValue = "1") @Min(1) @Max(100) int n,
            ServerWebExchange exchange
    ) {
        return mockItemService.mockItems(itemId, requestsUtils.extractAuthUser(exchange), n);
    }
}
