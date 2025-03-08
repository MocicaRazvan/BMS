package com.mocicarazvan.cartservice.controllers;

import com.mocicarazvan.cartservice.dtos.CartDeletedResponse;
import com.mocicarazvan.cartservice.dtos.UserCartBody;
import com.mocicarazvan.cartservice.dtos.UserCartResponse;
import com.mocicarazvan.cartservice.hateos.UserCartReactiveResponseBuilder;
import com.mocicarazvan.cartservice.services.UserCartService;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class UserCartController {

    private final UserCartService userCartService;
    private final UserCartReactiveResponseBuilder userCartReactiveResponseBuilder;

    @PostMapping(value = "/getOrCreate/{userId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<UserCartResponse>>> getOrCreateCart(
            @Valid @Min(1) @PathVariable Long userId
    ) {
        return userCartService.getOrCreate(userId)
                .flatMap(r -> userCartReactiveResponseBuilder.toModel(r, UserCartController.class))
                .map(ResponseEntity::ok);
    }

    @PatchMapping(value = "/remove/{userId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<UserCartResponse>>> removeFromCart(
            @Valid @RequestBody UserCartBody userCartBody,
            @Valid @Min(1) @PathVariable Long userId
    ) {
        return
                userCartService.removeFromCart(userCartBody, userId)
                        .flatMap(r -> userCartReactiveResponseBuilder.toModel(r, UserCartController.class))
                        .map(ResponseEntity::ok);
    }

    @DeleteMapping(value = "/delete/{userId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CartDeletedResponse>> deleteCart(
            @Valid @Min(1) @PathVariable Long userId
    ) {
        return userCartService.deleteCart(userId)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(value = "/deleteCreateNew/{userId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<UserCartResponse>>> deleteCreateNew(
            @Valid @Min(1) @PathVariable Long userId
    ) {
        return userCartService.deleteCartWithNewCreated(userId)
                .flatMap(r -> userCartReactiveResponseBuilder.toModel(r, UserCartController.class))
                .map(ResponseEntity::ok);
    }

    @PutMapping(value = "/add/{userId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<UserCartResponse>>> addToCart(
            @Valid @RequestBody UserCartBody userCartBody,
            @Valid @Min(1) @PathVariable Long userId
    ) {
        return userCartService.addToCart(userCartBody, userId)
                .flatMap(r -> userCartReactiveResponseBuilder.toModel(r, UserCartController.class))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/clearOldCarts")
    public Mono<ResponseEntity<Long>> clearOldCarts(
            @RequestParam(required = false, defaultValue = "${cart.clear-old-carts.days-cutoff:60}") @Min(1) Long daysCutoff
    ) {
        return userCartService.clearOldCarts(daysCutoff)
                .map(ResponseEntity::ok);
    }

}
