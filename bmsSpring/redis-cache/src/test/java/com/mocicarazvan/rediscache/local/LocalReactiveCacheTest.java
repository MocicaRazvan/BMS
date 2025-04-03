package com.mocicarazvan.rediscache.local;

import com.mocicarazvan.rediscache.config.LocalCacheConfig;
import com.mocicarazvan.rediscache.dtos.CacheRemoveKeyRemoveType;
import com.mocicarazvan.rediscache.dtos.CacheRemoveType;
import com.mocicarazvan.rediscache.dtos.NotifyCacheRemoveDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.shaded.org.apache.commons.lang3.tuple.Pair;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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

    private Pair<List<String>, List<String>> insertDummies() {
        localReactiveCache.put("dummies", List.of("value1", "value2", "value3"));
        localReactiveCache.put("dummies1", List.of("value1", "value2", "value3"));
        localReactiveCache.put("dummies2", "value1");
        localReactiveCache.put("dummies3*", "value1");
        return Pair.of(List.of("dummies", "dummies1", "dummies2", "dummies3*"),
                List.of("dummies", "dummies1", "dummies2", "dummies3"));
    }

    @Test
    public void whenRemoveNotifyCalled_thenNotifyRemoveLocalIsCalled() {
        var keys = insertDummies();
        doNothing().when(notifyLocalRemove).notifyRemove(any());
        localReactiveCache.removeNotify(keys.getLeft());

        ArgumentCaptor<NotifyCacheRemoveDto> argument = ArgumentCaptor.forClass(NotifyCacheRemoveDto.class);
        verify(notifyLocalRemove, times(1)).notifyRemove(argument.capture());
        assertEquals(
                new NotifyCacheRemoveDto(keys.getRight(), CacheRemoveType.LOCAL, CacheRemoveKeyRemoveType.NORMAL)
                , argument.getValue());
    }

    @Test
    public void whenRemoveByPrefixCalled_thenNotifyRemoveLocalIsCalled() {
        var keys = insertDummies();
        doNothing().when(notifyLocalRemove).notifyRemove(any());
        localReactiveCache.removeByPrefixNotify(List.of("dummies"));

        ArgumentCaptor<NotifyCacheRemoveDto> argument = ArgumentCaptor.forClass(NotifyCacheRemoveDto.class);
        verify(notifyLocalRemove, times(1)).notifyRemove(argument.capture());
        argument.getValue().setKeys(
                argument.getValue().getKeys().stream().sorted().toList()
        );
        assertEquals(
                new NotifyCacheRemoveDto(keys.getRight()
                        .stream().sorted().toList()
                        , CacheRemoveType.LOCAL, CacheRemoveKeyRemoveType.PREFIX)
                , argument.getValue());
    }
}