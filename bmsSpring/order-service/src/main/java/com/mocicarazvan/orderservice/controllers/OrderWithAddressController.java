package com.mocicarazvan.orderservice.controllers;


import com.mocicarazvan.orderservice.dtos.OrderDtoWithAddress;
import com.mocicarazvan.orderservice.hateos.OrderWithAddressReactiveResponseBuilder;
import com.mocicarazvan.orderservice.services.OrderWithAddressService;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderWithAddressController {


    private final OrderWithAddressService orderWithAddressService;
    private final RequestsUtils requestsUtils;
    private final OrderWithAddressReactiveResponseBuilder orderWithAddressReactiveResponseBuilder;

    @GetMapping(value = "{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<OrderDtoWithAddress>>> getModelById(@PathVariable Long id, ServerWebExchange exchange) {
        return orderWithAddressService.getModelById(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(o -> orderWithAddressReactiveResponseBuilder.toModel(o, OrderWithAddressController.class))
                .map(ResponseEntity::ok);
    }

    @PatchMapping(value = "/admin/filtered", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<OrderDtoWithAddress>>> getModelsFiltered(@RequestParam(required = false) String city,
                                                                                            @RequestParam(required = false) String state,
                                                                                            @RequestParam(required = false) String country,
                                                                                            @Valid @RequestBody PageableBody pageableBody,
                                                                                            ServerWebExchange exchange) {
        return orderWithAddressService.getModelsFiltered(city, state, country, pageableBody, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> orderWithAddressReactiveResponseBuilder.toModelPageable(m, OrderWithAddressController.class));
    }

    @PatchMapping(value = "/filtered/{userId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<OrderDtoWithAddress>>> getModelsFilteredUser(@RequestParam(required = false) String city,
                                                                                                @RequestParam(required = false) String state,
                                                                                                @RequestParam(required = false) String country,
                                                                                                @PathVariable Long userId,
                                                                                                @Valid @RequestBody PageableBody pageableBody,
                                                                                                ServerWebExchange exchange) {
        return orderWithAddressService.getModelsFilteredUser(city, state, country, pageableBody, userId, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> orderWithAddressReactiveResponseBuilder.toModelPageable(m, OrderWithAddressController.class));
    }
}
