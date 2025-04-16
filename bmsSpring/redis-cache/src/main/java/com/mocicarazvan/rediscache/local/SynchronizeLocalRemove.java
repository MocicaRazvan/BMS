package com.mocicarazvan.rediscache.local;


import com.mocicarazvan.rediscache.dtos.CacheRemoveKeyRemoveType;
import com.mocicarazvan.rediscache.dtos.CacheRemoveType;
import com.mocicarazvan.rediscache.dtos.NotifyCacheRemoveDto;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class SynchronizeLocalRemove {
    private final LocalReactiveCache localReactiveCache;
    private final ReverseKeysLocalCache reverseKeysLocalCache;

    public void handleNotification(NotifyCacheRemoveDto notifyCacheRemoveDto, boolean notRemove) {
        if (notRemove) {
            return;
        }
        RemoveFromCache removeFromCache = getCacheFromNotification(notifyCacheRemoveDto);
        getFunctionToCall(notifyCacheRemoveDto, removeFromCache);
    }

    public void handleNotification(NotifyCacheRemoveDto notifyCacheRemoveDto) {
        handleNotification(notifyCacheRemoveDto, false);
    }

    public RemoveFromCache getCacheFromNotification(NotifyCacheRemoveDto notifyCacheRemoveDto) {
        return switch (notifyCacheRemoveDto.getType()) {
            case CacheRemoveType.LOCAL -> localReactiveCache;
            case CacheRemoveType.REVERSE -> reverseKeysLocalCache;
            default -> throw new RuntimeException("Invalid cache type");
        };
    }

    public void getFunctionToCall(NotifyCacheRemoveDto notifyCacheRemoveDto, RemoveFromCache removeFromCache) {
        switch (notifyCacheRemoveDto.getKeyRemoveType()) {
            case CacheRemoveKeyRemoveType.NORMAL -> {
                removeFromCache.remove(notifyCacheRemoveDto.getKeys());
            }
            case CacheRemoveKeyRemoveType.PREFIX -> {
                removeFromCache.removeByPrefix(notifyCacheRemoveDto.getKeys());
            }
        }
    }
}
