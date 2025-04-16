package com.mocicarazvan.templatemodule.repositories;

import com.mocicarazvan.templatemodule.config.LocalTestConfig;
import com.mocicarazvan.templatemodule.config.TestContainerImages;
import com.mocicarazvan.templatemodule.config.TrxStepVerifier;
import com.mocicarazvan.templatemodule.models.IdGeneratedImpl;
import com.mocicarazvan.templatemodule.repositories.beans.IdGeneratedBeanRepository;
import com.mocicarazvan.templatemodule.testUtils.ItemSeeder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.ReactiveTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DataR2dbcTest
@Import(LocalTestConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IdGeneratedRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(TestContainerImages.POSTGRES_IMAGE)
            .withInitScript("schema-test.sql");
    @Autowired
    private ReactiveTransactionManager transactionManager;

    @Autowired
    @Qualifier("IdGeneratedBeanRepository")
    private IdGeneratedBeanRepository idGeneratedBeanRepository;

    @Autowired
    private TrxStepVerifier trxStepVerifier;

    private List<IdGeneratedImpl> items;


    public void seed() {
        if (items == null) {
            items = idGeneratedBeanRepository.deleteAll()
                    .thenMany(idGeneratedBeanRepository.saveAll(ItemSeeder.generateIdGenerated()))
                    .collectList()
                    .block();
        }
    }

    @BeforeEach
    void setUp() {
        seed();
    }

    @Test
    @Order(1)
        // todo rollback doesnt save
    void testSave() {
        var idGenerated = IdGeneratedImpl.builder()
                .createdAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .updatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();

        AtomicReference<Long> savedIdRef = new AtomicReference<>();
        StepVerifier.create(idGeneratedBeanRepository.save(idGenerated))
                .expectSubscription()
                .assertNext(savedIdGenerated -> {
                    assertNotNull(savedIdGenerated.getId());
                    assertTrue(savedIdGenerated.getId() > 0);
                    savedIdRef.set(savedIdGenerated.getId());
                })
                .verifyComplete();

        log.info("Saved {}", savedIdRef.get());

        StepVerifier.create(idGeneratedBeanRepository.deleteById(savedIdRef.get()))
                .expectSubscription()
                .verifyComplete();

    }

    @Test
    void loads() {
        assertNotNull(transactionManager);
        assertNotNull(trxStepVerifier);

        trxStepVerifier.create(idGeneratedBeanRepository.count())
                .expectSubscription()
                .expectNext(10L)
                .verifyComplete();
    }

    @Test
    void findAllByPageRequest() {
        var pr = PageRequest.of(0, 2, Sort.by("createdAt").descending());
        var expectedItems = items.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(2)
                .toList();
        trxStepVerifier.create(idGeneratedBeanRepository.findAllBy(pr))
                .expectSubscription()
                .expectNextSequence(expectedItems)
                .verifyComplete();

    }

    @Test
    void findALlByIdIn() {
        var ids = items.stream()
                .map(IdGeneratedImpl::getId)
                .collect(Collectors.toList());
        ids.add(123L);
        trxStepVerifier.create(idGeneratedBeanRepository.findAllByIdIn(ids))
                .expectSubscription()
                .expectNextCount(ids.size() - 1)
                .verifyComplete();
    }

    @Test
    void findALlByIdInPageRequest() {
        var ids = items.stream()
                .map(IdGeneratedImpl::getId)
                .collect(Collectors.toList());
        ids.add(123L);
        var pr = PageRequest.of(0, 2, Sort.by("createdAt").descending());
        var expectedItems = items.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(2)
                .toList();
        trxStepVerifier.create(idGeneratedBeanRepository.findAllByIdIn(ids, pr))
                .expectSubscription()
                .expectNextSequence(expectedItems)
                .verifyComplete();
    }

    @Test
    void countAllByIdIn() {
        var ids = items.stream()
                .map(IdGeneratedImpl::getId)
                .collect(Collectors.toList());
        ids.add(123L);
        trxStepVerifier.create(idGeneratedBeanRepository.countAllByIdIn(ids))
                .expectSubscription()
                .expectNext((long) ids.size() - 1)
                .verifyComplete();
    }

}