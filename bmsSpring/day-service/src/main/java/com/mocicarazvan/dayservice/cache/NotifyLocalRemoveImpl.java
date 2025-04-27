package com.mocicarazvan.dayservice.cache;

import com.mocicarazvan.rediscache.dtos.NotifyCacheRemoveDto;
import com.mocicarazvan.rediscache.local.NotifyLocalRemove;
import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.utils.AppInstanceId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotifyLocalRemoveImpl implements NotifyLocalRemove {
    private final RabbitMqSender rabbitMqSender;

    public NotifyLocalRemoveImpl(
            @Qualifier("dayCacheInvalidateSender") RabbitMqSender rabbitMqSender) {
        this.rabbitMqSender = rabbitMqSender;
    }

    @Override
    public void notifyRemove(NotifyCacheRemoveDto cacheRemove) {
//        log.info("Sending message to RabbitMQ: {}", cacheRemove);
        rabbitMqSender.sendMessageWithHeaders(cacheRemove, AppInstanceId.getAppInstanceIdHeader());
    }
}
