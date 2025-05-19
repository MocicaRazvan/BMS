package com.mocicarazvan.archiveservice.mappers;


import com.mocicarazvan.archiveservice.dtos.websocket.NotifyContainerAction;
import com.mocicarazvan.archiveservice.models.NotifyContainerModel;
import org.springframework.stereotype.Component;

@Component
public class NotifyContainerActionMapper {

    public NotifyContainerModel fromActionToModel(NotifyContainerAction notifyContainerAction) {
        return NotifyContainerModel.builder()
                .id(notifyContainerAction.getId())
                .queueName(notifyContainerAction.getQueueName())
                .timestamp(notifyContainerAction.getTimestamp())
                .action(notifyContainerAction.getAction())
                .build();
    }

    public NotifyContainerAction fromModelToAction(NotifyContainerModel notifyContainerModel) {
        return NotifyContainerAction.builder()
                .id(notifyContainerModel.getId())
                .queueName(notifyContainerModel.getQueueName())
                .timestamp(notifyContainerModel.getTimestamp())
                .action(notifyContainerModel.getAction())
                .build();
    }
}
