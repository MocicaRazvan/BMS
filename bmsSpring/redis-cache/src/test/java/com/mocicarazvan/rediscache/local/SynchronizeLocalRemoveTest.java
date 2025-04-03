package com.mocicarazvan.rediscache.local;

import com.mocicarazvan.rediscache.config.LocalCacheConfig;
import com.mocicarazvan.rediscache.dtos.CacheRemoveKeyRemoveType;
import com.mocicarazvan.rediscache.dtos.CacheRemoveType;
import com.mocicarazvan.rediscache.dtos.NotifyCacheRemoveDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = LocalCacheConfig.class)
public class SynchronizeLocalRemoveTest {

    @MockBean
    private LocalReactiveCache localReactiveCache;

    @MockBean
    private ReverseKeysLocalCache reverseKeysLocalCache;

    @Autowired
    private SynchronizeLocalRemove synchronizeLocalRemove;


    @Test
    void handleNotification_localNormal_callsRemoveOnLocalCache() {
        var dto = new NotifyCacheRemoveDto(List.of("a", "b"), CacheRemoveType.LOCAL, CacheRemoveKeyRemoveType.NORMAL);
        synchronizeLocalRemove.handleNotification(dto);
        verify(localReactiveCache, times(1)).remove(dto.getKeys());
        verifyNoInteractions(reverseKeysLocalCache);
    }

    @Test
    void handleNotification_reverseNormal_callsRemoveOnReverseCache() {
        var dto = new NotifyCacheRemoveDto(List.of("x", "y"), CacheRemoveType.REVERSE, CacheRemoveKeyRemoveType.NORMAL);
        synchronizeLocalRemove.handleNotification(dto);
        verify(reverseKeysLocalCache, times(1)).remove(dto.getKeys());
        verifyNoInteractions(localReactiveCache);
    }

    @Test
    void handleNotification_localPrefix_callsRemoveByPrefixOnLocalCache() {
        var dto = new NotifyCacheRemoveDto(List.of("prefix"), CacheRemoveType.LOCAL, CacheRemoveKeyRemoveType.PREFIX);
        synchronizeLocalRemove.handleNotification(dto);
        verify(localReactiveCache, times(1)).removeByPrefix(dto.getKeys());
        verifyNoInteractions(reverseKeysLocalCache);
    }

    @Test
    void handleNotification_reversePrefix_callsRemoveByPrefixOnReverseCache() {
        var dto = new NotifyCacheRemoveDto(List.of("pre"), CacheRemoveType.REVERSE, CacheRemoveKeyRemoveType.PREFIX);
        synchronizeLocalRemove.handleNotification(dto);
        verify(reverseKeysLocalCache, times(1)).removeByPrefix(dto.getKeys());
        verifyNoInteractions(localReactiveCache);
    }


}
