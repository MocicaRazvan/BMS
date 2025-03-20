package com.mocicarazvan.websocketservice.rpc;


import com.mocicarazvan.websocketservice.dtos.message.RpcResponse;
import com.mocicarazvan.websocketservice.dtos.user.reactive.ReactiveUserDto;
import com.mocicarazvan.websocketservice.exceptions.reactive.CannotGetUsersByEmail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.RabbitConverterFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRPCClient {
    private final AsyncRabbitTemplate asyncRabbitTemplate;
    @Value("${rpc.timeout:15}")
    private int rpcTimeout;
    private static final String USER_RPC_EXCHANGE = "users.rpc.exchange";

    public CompletableFuture<List<ReactiveUserDto>> getUsersByEmails(Set<String> emails) {
        RabbitConverterFuture<RpcResponse<List<ReactiveUserDto>>> users = asyncRabbitTemplate.convertSendAndReceiveAsType(
                USER_RPC_EXCHANGE,
                "users.rpc.getUsersByEmails",
                emails,
                new ParameterizedTypeReference<>() {
                }
        );

        return users
                .orTimeout(rpcTimeout, TimeUnit.SECONDS)
                .whenCompleteAsync((reactiveUsersDto, throwable) -> {
                    if (throwable != null) {
                        log.error(throwable.getMessage(), throwable);
                        throw new CannotGetUsersByEmail(emails, throwable);
                    }
                    if (reactiveUsersDto.isErrored() || !reactiveUsersDto.isSuccessful()) {
                        throw new CannotGetUsersByEmail(emails, reactiveUsersDto.getError());
                    }
                })
                .thenApply(RpcResponse::getData);


    }

    public CompletableFuture<Boolean> existsUserByEmail(String email) {
        RabbitConverterFuture<RpcResponse<Boolean>> exists = asyncRabbitTemplate.convertSendAndReceiveAsType(
                USER_RPC_EXCHANGE,
                "users.rpc.existsUserByEmail",
                email,
                new ParameterizedTypeReference<>() {
                }
        );

        return exists
                .orTimeout(rpcTimeout, TimeUnit.SECONDS)
                .whenCompleteAsync((existsUser, throwable) -> {
                    if (throwable != null) {
                        log.error(throwable.getMessage(), throwable);
                        throw new CannotGetUsersByEmail(Set.of(email), throwable);
                    }
                    if (existsUser.isErrored() || !existsUser.isSuccessful()) {
                        throw new CannotGetUsersByEmail(Set.of(email), existsUser.getError());
                    }
                })
                .thenApply(RpcResponse::getData);
    }
}
