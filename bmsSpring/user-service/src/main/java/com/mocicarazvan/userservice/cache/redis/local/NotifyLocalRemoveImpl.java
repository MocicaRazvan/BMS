package com.mocicarazvan.userservice.cache.redis.local;

import com.mocicarazvan.rediscache.dtos.NotifyCacheRemoveDto;
import com.mocicarazvan.rediscache.local.NotifyLocalRemove;
import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotifyLocalRemoveImpl implements NotifyLocalRemove {
    private final RabbitMqSender rabbitMqSender;

    public NotifyLocalRemoveImpl(
            @Qualifier("userCacheInvalidateSender") RabbitMqSender rabbitMqSender) {
        this.rabbitMqSender = rabbitMqSender;
    }

    @Override
    public void notifyRemove(NotifyCacheRemoveDto cacheRemove) {
//        log.info("Sending message to RabbitMQ: {}", cacheRemove);
        rabbitMqSender.sendMessage(cacheRemove);
    }
}
