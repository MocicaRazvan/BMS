package com.mocicarazvan.websocketservice.rpc;


import com.mocicarazvan.websocketservice.dtos.message.RpcResponse;
import com.mocicarazvan.websocketservice.dtos.user.reactive.ReactiveUserDto;
import com.mocicarazvan.websocketservice.exceptions.reactive.CannotGetUsersByEmail;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.RabbitConverterFuture;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class UserRPCClient {
    private static final String RETRY_NAME = "userRpcRetry";
    private final AsyncRabbitTemplate asyncRabbitTemplate;
    @Value("${rpc.timeout:15}")
    private int rpcTimeout;
    private static final String USER_RPC_EXCHANGE = "users.rpc.exchange";
    private final Retry retry;
    private final ThreadPoolTaskScheduler scheduler;

    public UserRPCClient(AsyncRabbitTemplate asyncRabbitTemplate, RetryRegistry retryRegistry,
                         @Qualifier("threadPoolTaskSchedulerVirtual") ThreadPoolTaskScheduler scheduler) {
        this.asyncRabbitTemplate = asyncRabbitTemplate;
        this.retry = retryRegistry.retry(RETRY_NAME);
        this.scheduler = scheduler;
    }


    public CompletableFuture<List<ReactiveUserDto>> getUsersByEmails(Set<String> emails) {
        return
                retry.executeCompletionStage(scheduler.getScheduledExecutor(), () -> {
                            RabbitConverterFuture<RpcResponse<List<ReactiveUserDto>>> users = asyncRabbitTemplate.convertSendAndReceiveAsType(
                                    USER_RPC_EXCHANGE,
                                    "users.rpc.getUsersByEmails",
                                    emails,
                                    new ParameterizedTypeReference<>() {
                                    }
                            );
                            return handleResponse(users, emails);
                        }
                ).toCompletableFuture();


    }

    private <T> CompletableFuture<T> handleResponse(RabbitConverterFuture<RpcResponse<T>> response, Collection<String> emails) {
        return response
                .orTimeout(rpcTimeout, TimeUnit.SECONDS)
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        log.error(throwable.getMessage(), throwable);
                        throw new CannotGetUsersByEmail(emails, throwable);
                    }
                    if (result.isErrored() || !result.isSuccessful()) {
                        throw new CannotGetUsersByEmail(emails, result.getError());
                    }
                    return result.getData();
                });
    }

    public CompletableFuture<Boolean> existsUserByEmail(String email) {
        return retry.executeCompletionStage(scheduler.getScheduledExecutor(), () -> {
                    RabbitConverterFuture<RpcResponse<Boolean>> exists = asyncRabbitTemplate.convertSendAndReceiveAsType(
                            USER_RPC_EXCHANGE,
                            "users.rpc.existsUserByEmail",
                            email,
                            new ParameterizedTypeReference<>() {
                            }
                    );

                    return handleResponse(exists, Collections.singleton(email));
                }
        ).toCompletableFuture();
    }
}
