package com.mocicarazvan.rediscache.local;

import com.mocicarazvan.rediscache.dtos.NotifyCacheRemoveDto;

@FunctionalInterface
public interface NotifyLocalRemove {
    void notifyRemove(NotifyCacheRemoveDto cacheRemove);
}
