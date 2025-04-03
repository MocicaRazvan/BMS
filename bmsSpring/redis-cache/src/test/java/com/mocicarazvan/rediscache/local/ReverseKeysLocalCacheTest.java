package com.mocicarazvan.rediscache.local;

import com.mocicarazvan.rediscache.config.LocalCacheConfig;
import com.mocicarazvan.rediscache.dtos.CacheRemoveKeyRemoveType;
import com.mocicarazvan.rediscache.dtos.CacheRemoveType;
import com.mocicarazvan.rediscache.dtos.NotifyCacheRemoveDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = LocalCacheConfig.class)
public class ReverseKeysLocalCacheTest {
    @MockBean
    NotifyLocalRemove notifyLocalRemove;

    @Autowired
    ReverseKeysLocalCache reverseKeysLocalCache;

    @BeforeEach
    void setUp() {
        reverseKeysLocalCache.clearAll();
    }

    @Test
    void shouldStoreAddCollection_andReturnSet() {
        var items = List.of("1", "2", "3", "1");
        var key = "key";
        reverseKeysLocalCache.put(key, items);
        var result = reverseKeysLocalCache.get(key);
        assertEquals(
                new HashSet<>(items)
                , result);
    }

    static Stream<Arguments> provideAddInputs() {
        return Stream.of(
                arguments("key1", Set.of("one"), false),
                arguments("key2", Set.of("a", "b", "c"), true),
                arguments("key3", Set.of("z"), false),
                arguments("key4", Set.of("x", "y"), true)
        );
    }

    @ParameterizedTest(name = "add to {0}, values={1}, useCollection={2}")
    @MethodSource("provideAddInputs")
    void testAddMethods(String key, Set<String> expectedValues, boolean isCollection) {
        if (isCollection) {
            reverseKeysLocalCache.add(key, expectedValues);
        } else {
            // one val
            reverseKeysLocalCache.add(key, expectedValues.iterator().next());
        }

        assertEquals(expectedValues, reverseKeysLocalCache.get(key));
    }

    private List<String> insertDummies() {
        reverseKeysLocalCache.put("dummies", List.of("value1", "value2", "value3"));
        reverseKeysLocalCache.put("dummies1*", List.of("value1", "value2", "value3"));
        return List.of("dummies", "dummies1*");
    }

    @Test
    public void whenRemoveNotifyCalled_thenRemoveKeys() {
        var keys = insertDummies();
        doNothing().when(notifyLocalRemove).notifyRemove(any());
        reverseKeysLocalCache.removeNotify(keys);
        ArgumentCaptor<NotifyCacheRemoveDto> argument = ArgumentCaptor.forClass(NotifyCacheRemoveDto.class);
        verify(notifyLocalRemove, times(1)).notifyRemove(argument.capture());
        assertEquals(
                new NotifyCacheRemoveDto(keys, CacheRemoveType.REVERSE, CacheRemoveKeyRemoveType.NORMAL)
                , argument.getValue());
        keys.forEach(
                k -> assertEquals(new HashSet<>(), reverseKeysLocalCache.get(k))
        );
    }

    @Test
    public void whenRemoveByPrefixNotifyCalled_thenRemoveKeys() {
        var keys = insertDummies();
        doNothing().when(notifyLocalRemove).notifyRemove(any());
        reverseKeysLocalCache.removeByPrefixNotify(keys);
        ArgumentCaptor<NotifyCacheRemoveDto> argument = ArgumentCaptor.forClass(NotifyCacheRemoveDto.class);
        verify(notifyLocalRemove, times(1)).notifyRemove(argument.capture());
        argument.getValue().setKeys(
                argument.getValue().getKeys().stream().sorted().toList()
        );
        assertEquals(
                new NotifyCacheRemoveDto(keys
                        .stream().sorted().toList(), CacheRemoveType.REVERSE, CacheRemoveKeyRemoveType.PREFIX)
                , argument.getValue());
        keys.forEach(
                k -> assertEquals(new HashSet<>(), reverseKeysLocalCache.get(k))
        );
    }

    @Test
    void shouldNotNotifyForEmptyKeys() {
        doNothing().when(notifyLocalRemove).notifyRemove(any());
        reverseKeysLocalCache.remove(List.of());
        verify(notifyLocalRemove, never()).notifyRemove(any());
    }

    @Test
    void shouldNotNotifyForEmptyPrefix() {
        doNothing().when(notifyLocalRemove).notifyRemove(any());
        reverseKeysLocalCache.removeByPrefix(List.of());
        verify(notifyLocalRemove, never()).notifyRemove(any());
    }
}
