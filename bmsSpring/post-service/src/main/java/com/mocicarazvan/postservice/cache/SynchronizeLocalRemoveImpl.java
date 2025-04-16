package com.mocicarazvan.postservice.cache;

import com.mocicarazvan.rediscache.dtos.NotifyCacheRemoveDto;
import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.ReverseKeysLocalCache;
import com.mocicarazvan.rediscache.local.SynchronizeLocalRemove;
import com.mocicarazvan.templatemodule.utils.AppInstanceId;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SynchronizeLocalRemoveImpl extends SynchronizeLocalRemove {
    public SynchronizeLocalRemoveImpl(LocalReactiveCache localReactiveCache, ReverseKeysLocalCache reverseKeysLocalCache) {
        super(localReactiveCache, reverseKeysLocalCache);
    }

    @RabbitListener(queues = "#{postCacheInvalidateQueue.name}")
    public void handleNotification(@Valid @Payload NotifyCacheRemoveDto notifyCacheRemoveDto,
                                   @Header(AppInstanceId.APP_INSTANCE_ID_HEADER) String appInstanceId
    ) {
        super.handleNotification(notifyCacheRemoveDto, AppInstanceId.isSameAppInstanceId(appInstanceId));
    }
}
