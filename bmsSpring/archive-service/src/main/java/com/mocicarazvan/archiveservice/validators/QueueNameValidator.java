package com.mocicarazvan.archiveservice.validators;

import com.mocicarazvan.archiveservice.config.rabbit.QueuesPropertiesConfig;
import com.mocicarazvan.archiveservice.validators.annotations.ValidQueueName;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@RequiredArgsConstructor
@Component
public class QueueNameValidator implements ConstraintValidator<ValidQueueName, String> {

    private final QueuesPropertiesConfig queuesPropertiesConfig;

    @Override
    public boolean isValid(String queueName, ConstraintValidatorContext context) {
        Set<String> queues = queuesPropertiesConfig.getQueues();
        return queues != null && queues.contains(queueName);
    }
}