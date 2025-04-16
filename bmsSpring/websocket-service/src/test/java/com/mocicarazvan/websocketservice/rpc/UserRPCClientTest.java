package com.mocicarazvan.websocketservice.rpc;

import com.mocicarazvan.websocketservice.config.AsyncConfig;
import com.mocicarazvan.websocketservice.dtos.message.RpcResponse;
import com.mocicarazvan.websocketservice.dtos.user.reactive.ReactiveUserDto;
import com.mocicarazvan.websocketservice.exceptions.reactive.CannotGetUsersByEmail;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.RabbitConverterFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@ContextConfiguration(classes = {AsyncConfig.class, UserRPCClient.class})
@TestPropertySource(properties = {
        "resilience4j.retry.instances.userRpcRetry.max-attempts=3",
        "resilience4j.retry.instances.userRpcRetry.wait-duration=20ms",
        "resilience4j.retry.instances.userRpcRetry.enable-exponential-backoff=true",
        "resilience4j.retry.instances.userRpcRetry.exponential-backoff-multiplier=2",
        "resilience4j.retry.instances.userRpcRetry.fail-after-max-attempts=true",
        "resilience4j.retry.instances.userRpcRetry.retry-exceptions=java.lang.Exception",
        "rpc.timeout=1"
})
@ImportAutoConfiguration(classes = {
        RetryAutoConfiguration.class
})
@Execution(ExecutionMode.SAME_THREAD)
public class UserRPCClientTest {


    @MockBean
    private AsyncRabbitTemplate asyncRabbitTemplate;

    @Autowired
    private UserRPCClient userRPCClient;

    private String USER_RPC_EXCHANGE;

    private final String email = "test@example.com";
    private final Set<String> emails = Set.of(email);

    private static final String USER_RPC_GET = "users.rpc.getUsersByEmails";
    private static final String USER_RPC_EXISTS = "users.rpc.existsUserByEmail";

    @BeforeEach
    void setup() {
        USER_RPC_EXCHANGE = (String) ReflectionTestUtils.getField(userRPCClient, "USER_RPC_EXCHANGE");
    }


    @Test
    public void getUsersByEmails_shouldRetryAndThrowCustomExceptionAfterFailures() {

        RabbitConverterFuture<RpcResponse<List<ReactiveUserDto>>> future = mock(RabbitConverterFuture.class);
        when(asyncRabbitTemplate.convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_GET),
                any(Set.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(future);

        when(future.orTimeout(anyLong(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Simulated failure")));

        var ex = assertThrows(CompletionException.class, () -> userRPCClient.getUsersByEmails(emails).join());
        assertInstanceOf(CannotGetUsersByEmail.class, ex.getCause());
        assertTrue(ex.getMessage().contains("test@example.com"));

        verify(asyncRabbitTemplate, times(3)).convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_GET),
                any(Set.class),
                any(ParameterizedTypeReference.class)
        );
    }


    @Test
    public void getUsersByEmails_shouldRetryAndThrowCustomExceptionAfterFailuresRecovers() {

        RabbitConverterFuture<RpcResponse<List<ReactiveUserDto>>> future = mock(RabbitConverterFuture.class);
        when(asyncRabbitTemplate.convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_GET),
                any(Set.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(future);
        var successResponse = RpcResponse.<List<ReactiveUserDto>>builder()
                .data(List.of(ReactiveUserDto.builder()
                        .id(1L)
                        .build()))
                .build();

        when(future.orTimeout(anyLong(), any()))
                .thenReturn(
                        CompletableFuture.failedFuture(new RuntimeException("Fail 1")),
                        CompletableFuture.failedFuture(new RuntimeException("Fail 2")),
                        CompletableFuture.completedFuture(successResponse)
                );


        var resp = assertDoesNotThrow(() -> userRPCClient.getUsersByEmails(emails).join());

        assertNotNull(resp);
        assertEquals(1, resp.size());
        assertEquals(1L, resp.get(0).getId());


        verify(asyncRabbitTemplate, times(3)).convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_GET),
                any(Set.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    public void getUsersByEmails_noRetryAndSuccess() {

        RabbitConverterFuture<RpcResponse<List<ReactiveUserDto>>> future = mock(RabbitConverterFuture.class);
        when(asyncRabbitTemplate.convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_GET),
                any(Set.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(future);
        var successResponse = RpcResponse.<List<ReactiveUserDto>>builder()
                .data(List.of(ReactiveUserDto.builder()
                        .id(1L)
                        .build()))
                .build();
        when(future.orTimeout(anyLong(), any()))
                .thenReturn(CompletableFuture.completedFuture(
                        successResponse
                ));


        var resp = assertDoesNotThrow(() -> userRPCClient.getUsersByEmails(emails).join());

        assertNotNull(resp);
        assertEquals(1, resp.size());
        assertEquals(1L, resp.get(0).getId());

        verify(asyncRabbitTemplate, times(1)).convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_GET),
                any(Set.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    public void getUsersByEmails_resultIsErrored() {

        RabbitConverterFuture<RpcResponse<List<ReactiveUserDto>>> future = mock(RabbitConverterFuture.class);
        when(asyncRabbitTemplate.convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_GET),
                any(Set.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(future);
        var resp = RpcResponse.<List<ReactiveUserDto>>builder()
                .error(new RuntimeException("Fail"))
                .build();
        when(future.orTimeout(anyLong(), any()))
                .thenReturn(CompletableFuture.completedFuture(
                        resp
                ));
        var ex = assertThrows(CompletionException.class, () -> userRPCClient.getUsersByEmails(emails).join());
        assertInstanceOf(CannotGetUsersByEmail.class, ex.getCause());
        assertTrue(ex.getMessage().contains("test@example.com"));


        verify(asyncRabbitTemplate, times(3)).convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_GET),
                any(Set.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void getUsersByEmails_timeoutException() {

        CompletableFuture<RpcResponse<List<ReactiveUserDto>>> hanging = new CompletableFuture<>();

        RabbitConverterFuture<RpcResponse<List<ReactiveUserDto>>> rabbitFuture = mock(RabbitConverterFuture.class);
        when(rabbitFuture.orTimeout(anyLong(), any()))
                .thenAnswer(inv -> hanging.orTimeout(
                        inv.getArgument(0), inv.getArgument(1))
                );

        when(asyncRabbitTemplate.convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_GET),
                any(Set.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(rabbitFuture);

        CompletionException ex = assertThrows(CompletionException.class,
                () -> userRPCClient.getUsersByEmails(emails).join());
        assertInstanceOf(CannotGetUsersByEmail.class, ex.getCause());
        assertInstanceOf(TimeoutException.class, ex.getCause().getCause());

        verify(asyncRabbitTemplate, times(3)).convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_GET),
                any(Set.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    public void existsUserByEmail_shouldRetryAndThrowCustomExceptionAfterFailures() {

        RabbitConverterFuture<RpcResponse<Boolean>> future = mock(RabbitConverterFuture.class);
        when(asyncRabbitTemplate.convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_EXISTS),
                any(String.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(future);

        when(future.orTimeout(anyLong(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Simulated failure")));

        var ex = assertThrows(CompletionException.class, () -> userRPCClient.existsUserByEmail(email).join());
        assertInstanceOf(CannotGetUsersByEmail.class, ex.getCause());
        assertTrue(ex.getMessage().contains("test@example.com"));

        verify(asyncRabbitTemplate, times(3)).convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_EXISTS),
                any(String.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void existsUserByEmail_shouldRetryAndThrowCustomExceptionAfterFailuresRecovers(boolean data) {

        RabbitConverterFuture<RpcResponse<Boolean>> future = mock(RabbitConverterFuture.class);
        when(asyncRabbitTemplate.convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_EXISTS),
                any(String.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(future);
        var successResponse = RpcResponse.<Boolean>builder()
                .data(data)
                .build();

        when(future.orTimeout(anyLong(), any()))
                .thenReturn(
                        CompletableFuture.failedFuture(new RuntimeException("Fail 1")),
                        CompletableFuture.failedFuture(new RuntimeException("Fail 2")),
                        CompletableFuture.completedFuture(successResponse)
                );


        var resp = assertDoesNotThrow(() -> userRPCClient.existsUserByEmail(email).join());

        assertNotNull(resp);
        assertEquals(data, resp);


        verify(asyncRabbitTemplate, times(3)).convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_EXISTS),
                any(String.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void existsUserByEmail_noRetryAndSuccess(boolean data) {

        RabbitConverterFuture<RpcResponse<Boolean>> future = mock(RabbitConverterFuture.class);
        when(asyncRabbitTemplate.convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_EXISTS),
                any(String.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(future);
        var successResponse = RpcResponse.<Boolean>builder()
                .data(data)
                .build();

        when(future.orTimeout(anyLong(), any()))
                .thenReturn(CompletableFuture.completedFuture(
                        successResponse
                ));


        var resp = assertDoesNotThrow(() -> userRPCClient.existsUserByEmail(email).join());

        assertNotNull(resp);
        assertEquals(data, resp);

        verify(asyncRabbitTemplate, times(1)).convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_EXISTS),
                any(String.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    public void existsUserByEmail_resultIsErrored() {

        RabbitConverterFuture<RpcResponse<Boolean>> future = mock(RabbitConverterFuture.class);
        when(asyncRabbitTemplate.convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_EXISTS),
                any(String.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(future);
        var resp = RpcResponse.<Boolean>builder()
                .error(new RuntimeException("Fail"))
                .build();
        when(future.orTimeout(anyLong(), any()))
                .thenReturn(CompletableFuture.completedFuture(
                        resp
                ));
        var ex = assertThrows(CompletionException.class, () -> userRPCClient.existsUserByEmail(email).join());
        assertInstanceOf(CannotGetUsersByEmail.class, ex.getCause());
        assertTrue(ex.getMessage().contains("test@example.com"));


        verify(asyncRabbitTemplate, times(3)).convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_EXISTS),
                any(String.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void existsUserByEmail_timeoutException() {

        CompletableFuture<RpcResponse<Boolean>> hanging = new CompletableFuture<>();

        RabbitConverterFuture<RpcResponse<Boolean>> rabbitFuture = mock(RabbitConverterFuture.class);
        when(rabbitFuture.orTimeout(anyLong(), any()))
                .thenAnswer(inv -> hanging.orTimeout(
                        inv.getArgument(0), inv.getArgument(1))
                );

        when(asyncRabbitTemplate.convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_EXISTS),
                any(String.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(rabbitFuture);

        CompletionException ex = assertThrows(CompletionException.class,
                () -> userRPCClient.existsUserByEmail(email).join());
        assertInstanceOf(CannotGetUsersByEmail.class, ex.getCause());
        assertInstanceOf(TimeoutException.class, ex.getCause().getCause());

        verify(asyncRabbitTemplate, times(3)).convertSendAndReceiveAsType(
                eq(USER_RPC_EXCHANGE),
                eq(USER_RPC_EXISTS),
                any(String.class),
                any(ParameterizedTypeReference.class)
        );
    }
}
