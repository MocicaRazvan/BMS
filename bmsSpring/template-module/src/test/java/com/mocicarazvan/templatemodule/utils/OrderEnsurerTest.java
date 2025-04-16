package com.mocicarazvan.templatemodule.utils;

import com.mocicarazvan.templatemodule.models.IdGenerated;
import com.mocicarazvan.templatemodule.models.IdGeneratedImpl;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

class OrderEnsurerTest {


    public static Flux<IdGeneratedImpl> getItems() {
        return Flux.range(0, 10)
                .map(i -> IdGeneratedImpl.builder().id(Long.valueOf(i)).build());
    }

    private final OrderEnsurer orderEnsurer = new OrderEnsurer();

    @Test
    void testOrderEnsurer() {
        Flux<IdGeneratedImpl> items = getItems();
        List<Long> ids = IntStream.range(0, 10)
                .mapToObj(Long::valueOf)
                .toList();

        StepVerifier.create(OrderEnsurer.orderFlux(items, ids, IdGenerated::getId))
                .expectNextSequence(Objects.requireNonNull(items.collectList().block()))
                .verifyComplete();
    }

    @Test
    void testOrderEnsurer2() {
        Flux<IdGeneratedImpl> items = getItems();
        List<Long> ids = IntStream.range(0, 10)
                .mapToObj(Long::valueOf)
                .toList().reversed();


        StepVerifier.create(OrderEnsurer.orderFlux(items, ids, IdGenerated::getId))
                .expectNextSequence(Objects.requireNonNull(Objects.requireNonNull(items.collectList().block()).reversed()))
                .verifyComplete();
    }
}