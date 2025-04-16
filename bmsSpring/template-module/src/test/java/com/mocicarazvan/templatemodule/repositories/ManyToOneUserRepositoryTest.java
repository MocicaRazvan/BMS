package com.mocicarazvan.templatemodule.repositories;

import com.mocicarazvan.templatemodule.config.LocalTestConfig;
import com.mocicarazvan.templatemodule.config.TestContainerImages;
import com.mocicarazvan.templatemodule.config.TrxStepVerifier;
import com.mocicarazvan.templatemodule.models.IdGenerated;
import com.mocicarazvan.templatemodule.models.ManyToOneUserImpl;
import com.mocicarazvan.templatemodule.repositories.beans.ManyToOneUserBeanRepository;
import com.mocicarazvan.templatemodule.testUtils.ItemSeeder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.ReactiveTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DataR2dbcTest
@Import(LocalTestConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ManyToOneUserRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(TestContainerImages.POSTGRES_IMAGE)
            .withInitScript("schema-test.sql");
    @Autowired
    private ReactiveTransactionManager transactionManager;

    @Autowired
    @Qualifier("manyToOneUserBeanRepository")
    private ManyToOneUserBeanRepository manyToOneUserBeanRepository;

    @Autowired
    private TrxStepVerifier trxStepVerifier;

    private List<ManyToOneUserImpl> items;

    @BeforeEach
    void init() {
        if (items == null) {
            items = manyToOneUserBeanRepository.deleteAll()
                    .thenMany(manyToOneUserBeanRepository.saveAll(ItemSeeder.generateManyToOne()))
                    .collectList().block();
        }
    }

    @Test
    @Order(1)
    void loads() {
        assertNotNull(manyToOneUserBeanRepository);
        assertNotNull(transactionManager);
        trxStepVerifier.create(manyToOneUserBeanRepository.count())
                .expectSubscription()
                .expectNext(10L)
                .verifyComplete();
    }

    public static Stream<Arguments> byUserIdArgs() {
        return Stream.of(
                Arguments.of(1L, PageRequest.of(0, 10), 5),
                Arguments.of(2L, PageRequest.of(0, 10), 5),
                Arguments.of(3L, PageRequest.of(0, 10), 0),
                Arguments.of(1L, PageRequest.of(0, 3), 3)
        );
    }

    @ParameterizedTest
    @MethodSource("byUserIdArgs")
    void findAllByUserId(Long userId, PageRequest pr, int expectedSize) {
        trxStepVerifier.create(manyToOneUserBeanRepository.findAllByUserId(userId, pr).collectList())
                .assertNext(list -> {
                    assertEquals(expectedSize, list.size());
//                    list.forEach(item -> assertEquals(userId, item.getUserId()));
                })
                .verifyComplete();
    }

    @Test
    void findModelByMonth_found() {
        var curMonth = LocalDateTime.now().getMonthValue();
        var curYear = LocalDateTime.now().getYear();
        var expectedItems = items.stream()
                .filter(item -> item.getCreatedAt().getMonthValue() == curMonth && item.getCreatedAt().getYear() == curYear)
                .toList();
        trxStepVerifier.create(manyToOneUserBeanRepository.findModelByMonth(curMonth, curYear).collectList())
                .assertNext(list -> {
                    assertEquals(expectedItems.size(), list.size());
                    var sortedModels = list.stream()
                            .sorted(Comparator.comparing(IdGenerated::getCreatedAt))
                            .toList();
                    var sortedExpected = expectedItems.stream()
                            .sorted(Comparator.comparing(IdGenerated::getCreatedAt))
                            .toList();
                    assertEquals(sortedExpected, sortedModels);
                })
                .verifyComplete();
    }

    @Test
    void findModelByMonth_notFound() {
        var curMonth = LocalDateTime.now().getMonthValue();
        var curYear = LocalDateTime.now().getYear() + 1;
        trxStepVerifier.create(manyToOneUserBeanRepository.findModelByMonth(curMonth, curYear))
                .verifyComplete();
    }
}