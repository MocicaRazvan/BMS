package com.mocicarazvan.websocketservice.annotations;

import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.SQLException;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
        retryFor = {OptimisticLockException.class,
                PessimisticLockException.class,
                CannotAcquireLockException.class,
                JpaSystemException.class,
                LockAcquisitionException.class,
                ObjectOptimisticLockingFailureException.class,
                CannotAcquireLockException.class, SQLException.class, StaleObjectStateException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 200, multiplier = 2, maxDelay = 1000))
public @interface CustomRetryable {
}
