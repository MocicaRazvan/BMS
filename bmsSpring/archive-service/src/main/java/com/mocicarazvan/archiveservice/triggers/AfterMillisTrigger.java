package com.mocicarazvan.archiveservice.triggers;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

import java.time.Instant;

@RequiredArgsConstructor
public class AfterMillisTrigger implements Trigger {
    private final long millis;
    private boolean executed = false;

    @Override
    public Instant nextExecution(TriggerContext triggerContext) {
        if (!executed) {
            executed = true;
            return Instant.now().plusMillis(millis);
        }
        return null;
    }
}
