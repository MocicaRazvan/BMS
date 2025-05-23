package com.mocicarazvan.templatemodule.repositories.impl;

import com.mocicarazvan.templatemodule.config.LocalTestConfig;
import com.mocicarazvan.templatemodule.config.TestContainerImages;
import com.mocicarazvan.templatemodule.config.TrxStepVerifier;
import com.mocicarazvan.templatemodule.models.AssociativeEntityImpl;
import com.mocicarazvan.templatemodule.repositories.beans.AssociativeEntityRepositoryImplImpl;
import com.mocicarazvan.templatemodule.testUtils.ItemSeeder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DataR2dbcTest
@Import({LocalTestConfig.class, AssociativeEntityRepositoryImplImpl.class})
@Execution(ExecutionMode.SAME_THREAD)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AssociativeEntityRepositoryImplTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(TestContainerImages.POSTGRES_IMAGE)
            .withInitScript("schema-test.sql");


    private List<AssociativeEntityImpl> items;


    @Autowired
    @Qualifier("associativeEntityBeanRepositoryImplImpl")
    private AssociativeEntityRepositoryImplImpl entityRepositoryImpl;

    @Autowired
    private TrxStepVerifier trxStepVerifier;

    @Autowired
    private DatabaseClient databaseClient;

    private Mono<Long> countAll() {
        return databaseClient.sql("select count(*) from associative_entity")
                .map(row -> row.get(0, Long.class))
                .first();
    }

    private Mono<Long> deleteAll() {
        return databaseClient.sql("truncate table associative_entity")
                .fetch()
                .rowsUpdated();
    }

    @BeforeEach
    void init() {
        if (items == null) {
            databaseClient.sql("truncate table associative_entity")
                    .fetch()
                    .rowsUpdated()
                    .block();
            items = ItemSeeder.generateAssociativeEntity();
        }
    }

    @Test
    @Order(1)
    void loads() {
        assertNotNull(entityRepositoryImpl);
        assertNotNull(databaseClient);
        assertEquals(0, countAll().block());
    }

    @Test
    public void insertForMasterAndChildren() {
        var masterId = 1L;
        var childIds = List.of(1L, 2L, 3L);
        trxStepVerifier.create(entityRepositoryImpl.insertForMasterAndChildren(masterId, childIds))
                .expectSubscription()
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    public void insertForMasterAndChildren_primaryKeyError() {
        var masterId = 1L;
        var childIds = List.of(1L, 2L, 1L);
        trxStepVerifier.create(entityRepositoryImpl.insertForMasterAndChildren(masterId, childIds))
                .expectSubscription()
                .expectNext(2L)
                .verifyComplete();
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void insertForMasterAndChildren_emptyChildren(Collection<Long> childIds) {
        var masterId = 1L;
        trxStepVerifier.create(entityRepositoryImpl.insertForMasterAndChildren(masterId, childIds))
                .expectSubscription()
                .expectErrorMatches(throwable -> {
                    assertInstanceOf(IllegalArgumentException.class, throwable);
                    assertEquals("Collection must not be null or empty", throwable.getMessage());
                    return true;
                })
                .verify();
    }

    @Test
    public void consensusChildrenForMaster_noOverlap() {
        var masterId = 1L;
        var oldChildIds = List.of(1L, 2L, 3L);
        var newChildIds = List.of(4L, 5L, 6L, 7L, 8L);
        entityRepositoryImpl.insertForMasterAndChildren(masterId, oldChildIds).block();
        trxStepVerifier.create(entityRepositoryImpl.consensusChildrenForMaster(masterId, newChildIds))
                .expectSubscription()
                .expectNext((long) oldChildIds.size() + newChildIds.size())
                .verifyComplete();

        deleteAll().block();
    }

    @Test
    public void consensusChildrenForMaster_overlap() {
        var masterId = 1L;
        var oldChildIds = List.of(1L, 2L, 3L);
        var newChildIds = List.of(2L, 3L, 6L, 7L, 8L, 8L, 8L, 8L);
        entityRepositoryImpl.insertForMasterAndChildren(masterId, oldChildIds).block();
        trxStepVerifier.create(entityRepositoryImpl.consensusChildrenForMaster(masterId, newChildIds))
                .expectSubscription()
                .expectNext(5L + 1L)
                .verifyComplete();

        deleteAll().block();
    }

    @Test
    public void addChild() {
        var masterId = 1L;
        var childId = 2L;
        trxStepVerifier.create(entityRepositoryImpl.addChild(masterId, childId))
                .expectSubscription()
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    public void addChild_alreadyExisting() {
        var masterId = 1L;
        var childId = 2L;
        entityRepositoryImpl.addChild(masterId, childId).block();
        trxStepVerifier.create(entityRepositoryImpl.addChild(masterId, childId))
                .expectSubscription()
                .expectNext(1L)
                .verifyComplete();
        deleteAll().block();
    }

    @Test
    public void removeChild() {
        var masterId = 1L;
        var childId = 2L;
        entityRepositoryImpl.addChild(masterId, childId).block();
        trxStepVerifier.create(entityRepositoryImpl.removeChild(masterId, childId))
                .expectSubscription()
                .expectNext(1L)
                .verifyComplete();
        deleteAll().block();
    }
}