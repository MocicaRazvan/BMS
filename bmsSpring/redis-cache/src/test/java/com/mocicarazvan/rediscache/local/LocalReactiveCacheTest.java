package com.mocicarazvan.rediscache.local;

import com.mocicarazvan.rediscache.config.LocalCacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.test.StepVerifier;

import java.util.List;

@Slf4j
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = LocalCacheConfig.class)
class LocalReactiveCacheTest {


    @MockBean
    NotifyLocalRemove notifyLocalRemove;

    @Autowired
    LocalReactiveCache localReactiveCache;

    @BeforeEach
    void setUp() {
        localReactiveCache.clearAll();
    }

    @Test
    void shouldStoreAndRetrieveMono() {
        String key = "mono-key";
        String value = "value";
        localReactiveCache.put(key, value);

        StepVerifier.create(localReactiveCache.getMonoOrEmpty(key))
                .expectNext(value)
                .verifyComplete();
    }

    @Test
    void shouldNotAddNullMono() {

        String key = "mono-key";
        localReactiveCache.put(key, null);

        StepVerifier.create(localReactiveCache.getMonoOrEmpty(key))
                .verifyComplete();

    }

    @Test
    void shouldStoreAndRetrieveFlux() {
        String key = "flux-key";
        List<Object> values = List.of("value1", "value2", "value3");

        localReactiveCache.put(key, values);
        

        StepVerifier.create(localReactiveCache.getFluxOrEmpty(key))
                .expectNext("value1")
                .expectNext("value2")
                .expectNext("value3")
                .verifyComplete();
    }

    @Test
    void shouldNotAddNullFlux() {

        String key = "flux-key";
        localReactiveCache.put(key, null);

        StepVerifier.create(localReactiveCache.getFluxOrEmpty(key))
                .verifyComplete();

    }
    //todo check how many times notify remove is caleed
}