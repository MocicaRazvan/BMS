package com.mocicarazvan.kanbanservice.cache;

import com.mocicarazvan.rediscache.dtos.NotifyCacheRemoveDto;
import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.ReverseKeysLocalCache;
import com.mocicarazvan.rediscache.local.SynchronizeLocalRemove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SynchronizeLocalRemoveImpl extends SynchronizeLocalRemove {
    public SynchronizeLocalRemoveImpl(LocalReactiveCache localReactiveCache, ReverseKeysLocalCache reverseKeysLocalCache) {
        super(localReactiveCache, reverseKeysLocalCache);
    }

    @Override
    @RabbitListener(queues = "#{kanbanCacheInvalidateQueue.name}", executor = "rabbitMqAsyncTaskExecutor")
    public void handleNotification(NotifyCacheRemoveDto notifyCacheRemoveDto) {
//        log.info("Received notification for cache: {}", notifyCacheRemoveDto);
        super.handleNotification(notifyCacheRemoveDto);
    }
}
