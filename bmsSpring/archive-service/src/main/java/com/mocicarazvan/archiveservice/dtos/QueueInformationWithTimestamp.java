package com.mocicarazvan.archiveservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.amqp.core.QueueInformation;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueInformationWithTimestamp {

    private String name;
    private int messageCount;
    private int consumerCount;
    private LocalDateTime timestamp;
    private String cronExpression;


    public static QueueInformationWithTimestamp fromQueueInformation(QueueInformation queueInformation, String cronExpression) {
        return QueueInformationWithTimestamp.builder()
                .name(queueInformation.getName())
                .messageCount(queueInformation.getMessageCount())
                .consumerCount(queueInformation.getConsumerCount())
                .timestamp(LocalDateTime.now())
                .cronExpression(cronExpression)
                .build();
    }

}
