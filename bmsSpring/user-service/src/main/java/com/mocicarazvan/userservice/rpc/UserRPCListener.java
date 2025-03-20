package com.mocicarazvan.userservice.rpc;


import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.userservice.dtos.messages.RpcResponse;
import com.mocicarazvan.userservice.services.UserService;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class UserRPCListener {

    private final UserService userService;
    private final SimpleAsyncTaskExecutor rabbitMqAsyncTaskExecutor;

    public UserRPCListener(UserService userService,
                           @Qualifier("rabbitMqAsyncTaskExecutor") SimpleAsyncTaskExecutor rabbitMqAsyncTaskExecutor
    ) {
        this.userService = userService;
        this.rabbitMqAsyncTaskExecutor = rabbitMqAsyncTaskExecutor;
    }

    @RabbitListener(
            queues = "#{usersEmailsRpcQueue.name}"
            , executor = "rabbitMqAsyncTaskExecutor"
            , ackMode = "MANUAL"
    )
    public Mono<RpcResponse<List<UserDto>>> getUsersByEmails(Set<String> emails

    ) {


        if (emails.isEmpty()) {
            return Mono.just(
                    RpcResponse.success(List.of())
            );
        }

        return userService.findAllByEmailIn(emails)
                .collectList()
                .map(RpcResponse::success)
                .onErrorResume(e -> Mono.just(RpcResponse.error(e)));
    }

    @RabbitListener(
            queues = "#{usersEmailExistsQueue.name}"
            , executor = "rabbitMqAsyncTaskExecutor"
            , ackMode = "MANUAL"
    )
    public Mono<RpcResponse<Boolean>> existsUserByEmail(@NotBlank String email) {
        return userService.existsByEmail(email)
                .map(RpcResponse::success)
                .onErrorResume(e -> Mono.just(RpcResponse.error(e)))
                ;
    }


}
