package com.mocicarazvan.templatemodule.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.exceptions.common.WrappingMonoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObjectMapperMonoWrapperTest {

    @Mock
    private ObjectMapper objectMapper;

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

    @InjectMocks
    private ObjectMapperMonoWrapper objectMapperMonoWrapper;

    @BeforeEach
    void setUp() {
        // Initialize the ObjectMapperMonoWrapper with a mock ThreadPoolTaskScheduler
        threadPoolTaskScheduler.initialize();
        objectMapperMonoWrapper = new ObjectMapperMonoWrapper(objectMapper, threadPoolTaskScheduler);
    }

    @Test
    void wrapBlockingFunction_returnsSerializedString_whenObjectIsValid() throws Exception {
        String expectedJson = "{\"key\":\"value\"}";
        Object inputObject = Map.of("key", "value");

        when(objectMapper.writeValueAsString(inputObject)).thenReturn(expectedJson);

        StepVerifier.create(objectMapperMonoWrapper.wrapBlockingFunction(inputObject))
                .expectNext(expectedJson)
                .verifyComplete();
    }

    @Test
    void wrapBlockingFunction_throwsWrappingMonoException_whenSerializationFails() throws Exception {
        Object inputObject = new Object();
        RuntimeException serializationException = new RuntimeException("Error during writing function execution");

        when(objectMapper.writeValueAsString(inputObject)).thenThrow(serializationException);

        StepVerifier.create(objectMapperMonoWrapper.wrapBlockingFunction(inputObject))
                .expectErrorMatches(throwable -> throwable instanceof WrappingMonoException &&
                        throwable.getMessage().equals("Error during writing function execution") &&
                        throwable.getCause() == serializationException)
                .verify();
    }


}