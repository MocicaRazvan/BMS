package com.mocicarazvan.templatemodule.config;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.test.StepVerifier;


@RequiredArgsConstructor
public class TrxStepVerifier {

    private final ReactiveTransactionManager reactiveTransactionManager;

    public <T> StepVerifier.FirstStep<T> create(Publisher<? extends T> publisher) {
        return StepVerifier.create(
                TransactionalOperator.create(reactiveTransactionManager)
                        .execute(trx -> {
                            trx.setRollbackOnly();
                            return publisher;
                        })
        );
    }

}
