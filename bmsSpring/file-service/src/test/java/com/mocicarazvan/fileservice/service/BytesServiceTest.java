package com.mocicarazvan.fileservice.service;

import com.mocicarazvan.fileservice.config.AsyncConfig;
import com.mocicarazvan.fileservice.service.impl.BytesServiceImpl;
import com.mocicarazvan.fileservice.testUtils.ReactiveTestGridFsResource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {AsyncConfig.class})
@Import({BytesServiceImpl.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BytesServiceTest {
    @Autowired
    private BytesService bytesService;

    @Test
    @Order(1)
    void loads() {
        assertNotNull(bytesService);
    }

    @Test
    void getVideoByRange_fullRange() {
        DataBufferFactory factory = new DefaultDataBufferFactory();
        var string1 = "hello";
        var string2 = "world";
        DataBuffer buffer1 = factory.wrap(string1.getBytes(StandardCharsets.UTF_8));
        DataBuffer buffer2 = factory.wrap(string2.getBytes(StandardCharsets.UTF_8));
        ReactiveGridFsResource gridFsResource = new ReactiveTestGridFsResource(buffer1, buffer2);
        AtomicLong rangeStart = new AtomicLong(0);
        AtomicLong rangeEnd = new AtomicLong(buffer1.readableByteCount() + buffer2.readableByteCount() - 1);

        StepVerifier.create(bytesService.getVideoByRange(gridFsResource, rangeStart, rangeEnd)
                        .collectList()
                )
                .assertNext(list -> {
                    assertEquals(2, list.size());
                    assertEquals(string1, list.get(0).toString(StandardCharsets.UTF_8));
                    assertEquals(string2, list.get(1).toString(StandardCharsets.UTF_8));
                })
                .verifyComplete();


    }

    @Test
    void getVideoByRange_discardFirst_dontTouchSecond() {
        DataBufferFactory factory = new DefaultDataBufferFactory();
        var string1 = "hello";
        var string2 = "world";
        DataBuffer buffer1 = factory.wrap(string1.getBytes(StandardCharsets.UTF_8));
        DataBuffer buffer2 = factory.wrap(string2.getBytes(StandardCharsets.UTF_8));
        ReactiveGridFsResource gridFsResource = new ReactiveTestGridFsResource(buffer1, buffer2);
        AtomicLong rangeStart = new AtomicLong(buffer1.readableByteCount());
        AtomicLong rangeEnd = new AtomicLong(buffer1.readableByteCount() + buffer2.readableByteCount() - 1);

        StepVerifier.create(bytesService.getVideoByRange(gridFsResource, rangeStart, rangeEnd)
                        .collectList()
                )
                .assertNext(list -> {
                    assertEquals(1, list.size());
                    assertEquals(string2, list.get(0).toString(StandardCharsets.UTF_8));
                })
                .verifyComplete();


    }

    @Test
    void getVideoByRange_discardFirst_truncateSecond() {
        DataBufferFactory factory = new DefaultDataBufferFactory();
        var string1 = "hello";
        var string2 = "world";
        DataBuffer buffer1 = factory.wrap(string1.getBytes(StandardCharsets.UTF_8));
        DataBuffer buffer2 = factory.wrap(string2.getBytes(StandardCharsets.UTF_8));
        ReactiveGridFsResource gridFsResource = new ReactiveTestGridFsResource(buffer1, buffer2);
        AtomicLong rangeStart = new AtomicLong(buffer1.readableByteCount());
        AtomicLong rangeEnd = new AtomicLong(buffer1.readableByteCount() + buffer2.readableByteCount() - 2);

        StepVerifier.create(bytesService.getVideoByRange(gridFsResource, rangeStart, rangeEnd)
                        .collectList()
                )
                .assertNext(list -> {
                    assertEquals(1, list.size());
                    assertEquals(
                            string2.substring(0, string2.length() - 1)
                            , list.get(0).toString(StandardCharsets.UTF_8));
                })
                .verifyComplete();


    }

    @Test
    void getVideoByRange_truncateFirst_truncateSecond() {
        DataBufferFactory factory = new DefaultDataBufferFactory();
        var string1 = "hello";
        var string2 = "world";
        DataBuffer buffer1 = factory.wrap(string1.getBytes(StandardCharsets.UTF_8));
        DataBuffer buffer2 = factory.wrap(string2.getBytes(StandardCharsets.UTF_8));
        ReactiveGridFsResource gridFsResource = new ReactiveTestGridFsResource(buffer1, buffer2);
        AtomicLong rangeStart = new AtomicLong(1);
        AtomicLong rangeEnd = new AtomicLong(buffer1.readableByteCount() + buffer2.readableByteCount() - 2);

        StepVerifier.create(bytesService.getVideoByRange(gridFsResource, rangeStart, rangeEnd)
                        .collectList()
                )
                .assertNext(list -> {
                    assertEquals(2, list.size());
                    assertEquals(
                            string1.substring(1)
                            , list.get(0).toString(StandardCharsets.UTF_8));
                    assertEquals(
                            string2.substring(0, string2.length() - 1)
                            , list.get(1).toString(StandardCharsets.UTF_8));
                })
                .verifyComplete();


    }

    @Test
    void getVideoByRange_exactFirst() {
        DataBufferFactory factory = new DefaultDataBufferFactory();
        var string1 = "hello";
        var string2 = "world";
        DataBuffer buffer1 = factory.wrap(string1.getBytes(StandardCharsets.UTF_8));
        DataBuffer buffer2 = factory.wrap(string2.getBytes(StandardCharsets.UTF_8));
        ReactiveGridFsResource gridFsResource = new ReactiveTestGridFsResource(buffer1, buffer2);
        AtomicLong rangeStart = new AtomicLong(0);
        AtomicLong rangeEnd = new AtomicLong(buffer1.readableByteCount() - 1);

        StepVerifier.create(bytesService.getVideoByRange(gridFsResource, rangeStart, rangeEnd)
                        .collectList()
                )
                .assertNext(list -> {
                    System.out.println(list.stream().map(b -> b.toString(StandardCharsets.UTF_8)).toList());
                    assertEquals(1, list.size());
                    assertEquals(string1, list.get(0).toString(StandardCharsets.UTF_8));
                })
                .verifyComplete();

    }

    @Test
    void getVideoByRange_exactSecond() {
        DataBufferFactory factory = new DefaultDataBufferFactory();
        var string1 = "hello";
        var string2 = "world";
        DataBuffer buffer1 = factory.wrap(string1.getBytes(StandardCharsets.UTF_8));
        DataBuffer buffer2 = factory.wrap(string2.getBytes(StandardCharsets.UTF_8));
        ReactiveGridFsResource gridFsResource = new ReactiveTestGridFsResource(buffer1, buffer2);
        AtomicLong rangeStart = new AtomicLong(buffer1.readableByteCount());
        AtomicLong rangeEnd = new AtomicLong(buffer1.readableByteCount() + buffer2.readableByteCount() - 1);

        StepVerifier.create(bytesService.getVideoByRange(gridFsResource, rangeStart, rangeEnd)
                        .collectList()
                )
                .assertNext(list -> {
                    assertEquals(1, list.size());
                    assertEquals(string2, list.get(0).toString(StandardCharsets.UTF_8));
                })
                .verifyComplete();

    }

    @Test
    void getVideoByRange_empty() {
        DataBufferFactory factory = new DefaultDataBufferFactory();
        var string1 = "";
        var buffer1 = factory.wrap(string1.getBytes(StandardCharsets.UTF_8));

        ReactiveGridFsResource gridFsResource = new ReactiveTestGridFsResource(buffer1);

        StepVerifier.create(bytesService.getVideoByRange(gridFsResource, new AtomicLong(0), new AtomicLong(0))
                        .collectList()
                )
                .assertNext(list -> {
                    assertEquals(0, list.size());
                })
                .verifyComplete();

    }

    @Test
    void getVideoByRange_startRangeLargerThenBuffer() {
        DataBufferFactory factory = new DefaultDataBufferFactory();
        var string1 = "hello";
        var string2 = "world";
        DataBuffer buffer1 = factory.wrap(string1.getBytes(StandardCharsets.UTF_8));
        DataBuffer buffer2 = factory.wrap(string2.getBytes(StandardCharsets.UTF_8));
        ReactiveGridFsResource gridFsResource = new ReactiveTestGridFsResource(buffer1, buffer2);
        AtomicLong rangeStart = new AtomicLong(buffer1.readableByteCount() + 10000);
        AtomicLong rangeEnd = new AtomicLong(rangeStart.get() + 10000);

        StepVerifier.create(bytesService.getVideoByRange(gridFsResource, rangeStart, rangeEnd)
                        .collectList()
                )
                .assertNext(list -> {
                    assertEquals(0, list.size());
                })
                .verifyComplete();
    }

    @Test
    void getVideoByRange_startAndEndGenerous() {

        DataBufferFactory factory = new DefaultDataBufferFactory();
        var string1 = "hello";
        var string2 = "world";
        DataBuffer buffer1 = factory.wrap(string1.getBytes(StandardCharsets.UTF_8));
        DataBuffer buffer2 = factory.wrap(string2.getBytes(StandardCharsets.UTF_8));
        ReactiveGridFsResource gridFsResource = new ReactiveTestGridFsResource(buffer1, buffer2);
        AtomicLong rangeStart = new AtomicLong(0);
        AtomicLong rangeEnd = new AtomicLong(buffer1.readableByteCount() + buffer2.readableByteCount() + 10000);

        StepVerifier.create(bytesService.getVideoByRange(gridFsResource, rangeStart, rangeEnd)
                        .collectList()
                )
                .assertNext(list -> {
                    assertEquals(2, list.size());
                    assertEquals(string1, list.get(0).toString(StandardCharsets.UTF_8));
                    assertEquals(string2, list.get(1).toString(StandardCharsets.UTF_8));
                })
                .verifyComplete();
    }

    @Test
    void getVideoByRange_shouldEmitError_whenBufferThrows() {
        AtomicLong rangeStart = new AtomicLong(0);
        AtomicLong rangeEnd = new AtomicLong(5);

        DataBuffer buffer = mock(DataBuffer.class);
        when(buffer.readableByteCount()).thenThrow(new RuntimeException("Simulated read error"));

        ReactiveGridFsResource gridFsResource = new ReactiveTestGridFsResource(buffer);

        StepVerifier.create(bytesService.getVideoByRange(gridFsResource, rangeStart, rangeEnd))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Simulated read error"))
                .verify();
    }

    @Test
    void getVideoByRange_enormousBuffer() {
        var string = "hello";
        DataBufferFactory factory = new DefaultDataBufferFactory();
        DataBuffer[] buffers = IntStream.range(0, 1_000_00)
                .mapToObj(i -> factory.wrap(string.getBytes(StandardCharsets.UTF_8)))
                .toArray(DataBuffer[]::new);
        ReactiveGridFsResource gridFsResource = new ReactiveTestGridFsResource(buffers);

        AtomicLong rangeStart = new AtomicLong(0);
        AtomicLong rangeEnd = new AtomicLong((long) buffers[0].readableByteCount() * buffers.length - 1);

        StepVerifier.create(bytesService.getVideoByRange(gridFsResource, rangeStart, rangeEnd)
                        .collectList()
                )
                .assertNext(list -> {
                    assertEquals(buffers.length, list.size());
                    List<String> strings = list.stream()
                            .map(b -> b.toString(StandardCharsets.UTF_8))
                            .toList();
                    assertTrue(strings.stream().allMatch(s -> s.equals(string)));
                })
                .verifyComplete();


    }

}