package com.mocicarazvan.orderservice.repositories.impl;

import com.mocicarazvan.orderservice.mappers.OrderWithAddressMapper;
import com.mocicarazvan.orderservice.models.OrderWithAddress;
import com.mocicarazvan.orderservice.repositories.ExtendedOrderWithAddressRepository;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import com.mocicarazvan.templatemodule.utils.RepositoryUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ExtendedOrderWithAddressRepositoryImpl implements ExtendedOrderWithAddressRepository {

    private final DatabaseClient databaseClient;
    private final OrderWithAddressMapper orderWithAddressMapper;
    private final PageableUtilsCustom pageableUtilsCustom;
    private final RepositoryUtils repositoryUtils;

    private static final String SELECT_ALL = """
            SELECT
                custom_order.*,
                address.id AS a_id, address.created_at AS a_created_at, address.updated_at AS a_updated_at,
                address.city AS a_city, address.country AS a_country, address.line1 AS a_line1,
                address.line2 AS a_line2, address.postal_code AS a_postal_code, address.state AS a_state
            FROM custom_order
            JOIN address ON custom_order.address_id = address.id
            """;

    private static final String COUNT_ALL = """
            SELECT COUNT(*)
            FROM custom_order
            LEFT JOIN address ON custom_order.address_id = address.id
            """;

    @Override
    public Mono<OrderWithAddress> getModelById(Long id) {
        return databaseClient.sql(SELECT_ALL + " WHERE custom_order.id = :id")
                .bind("id", id)
                .map((row, metadata) -> orderWithAddressMapper.fromRow(row))
                .one();
    }

    @Override
    public Flux<OrderWithAddress> getModelsFiltered(String city, String state, String country,
                                                    LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                    LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                    PageRequest pageRequest) {
        StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);

        appendWhereClause(queryBuilder, city, state, country, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

        queryBuilder.append(pageableUtilsCustom.createPageRequestQuery(pageRequest, List.of(
                repositoryUtils.createExtraOrder(city, "city", ":city"),
                repositoryUtils.createExtraOrder(state, "state", ":state"),
                repositoryUtils.createExtraOrder(country, "country", ":country")
        )));

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(city, state, country,
                createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                queryBuilder);

        return executeSpec.map((row, metadata) -> orderWithAddressMapper.fromRow(row)).all();
    }

    @Override
    public Mono<Long> countModelsFiltered(String city, String state, String country,
                                          LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                          LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound
    ) {
        StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);

        appendWhereClause(queryBuilder, city, state, country, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(city, state, country,
                createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                queryBuilder);

        return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
    }

    @Override
    public Flux<OrderWithAddress> getModelsFilteredUser(String city, String state, String country,
                                                        LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                        LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                        Long userId, PageRequest pageRequest) {
        StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);

        appendWhereClause(queryBuilder, city, state, country, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);


        repositoryUtils.addNotNullField(userId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > SELECT_ALL.length()),
                " user_id = :userId");

        queryBuilder.append(pageableUtilsCustom.createPageRequestQuery(pageRequest, List.of(
                repositoryUtils.createExtraOrder(city, "city", ":city"),
                repositoryUtils.createExtraOrder(state, "state", ":state"),
                repositoryUtils.createExtraOrder(country, "country", ":country")
        )));

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(city, state, country,
                createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                queryBuilder);

        executeSpec = repositoryUtils.bindNotNullField(userId, executeSpec, "userId");

        return executeSpec.map((row, metadata) -> orderWithAddressMapper.fromRow(row)).all();
    }


    @Override
    public Mono<Long> countModelsFilteredUser(String city, String state, String country,
                                              LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                              LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                              Long userId
    ) {
        StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);

        appendWhereClause(queryBuilder, city, state, country, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

        repositoryUtils.addNotNullField(userId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > COUNT_ALL.length()),
                " user_id = :userId");


        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(city, state, country,
                createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                queryBuilder);


        executeSpec = repositoryUtils.bindNotNullField(userId, executeSpec, "userId");

        return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
    }

    private void appendWhereClause(StringBuilder queryBuilder, String city, String state, String country,
                                   LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                   LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound
    ) {

        RepositoryUtils.MutableBoolean hasPreviousCriteria = new RepositoryUtils.MutableBoolean(false);

        repositoryUtils.addStringField(city, queryBuilder, hasPreviousCriteria, " ( city ILIKE '%' || :city || '%' OR similarity(city, :city ) > 0.3 )");
        repositoryUtils.addStringField(state, queryBuilder, hasPreviousCriteria, "( state ILIKE '%' || :state || '%' OR similarity(state, :state ) > 0.3 )");
        repositoryUtils.addStringField(country, queryBuilder, hasPreviousCriteria, "( country ILIKE '%' || :country || '%' OR similarity(country, :country ) > 0.3 )");
        repositoryUtils.addCreatedAtBound("custom_order", createdAtLowerBound, createdAtUpperBound, queryBuilder, hasPreviousCriteria);
        repositoryUtils.addUpdatedAtBound("custom_order", updatedAtLowerBound, updatedAtUpperBound, queryBuilder, hasPreviousCriteria);
    }


    private DatabaseClient.GenericExecuteSpec getGenericExecuteSpec(String city, String state, String country,
                                                                    LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                    LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                    StringBuilder queryBuilder) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(queryBuilder.toString());


        executeSpec = repositoryUtils.bindStringField(city, executeSpec, "city");
        executeSpec = repositoryUtils.bindStringField(state, executeSpec, "state");
        executeSpec = repositoryUtils.bindStringField(country, executeSpec, "country");
        executeSpec = repositoryUtils.bindCreatedAtBound(createdAtLowerBound, createdAtUpperBound, executeSpec);
        executeSpec = repositoryUtils.bindUpdatedAtBound(updatedAtLowerBound, updatedAtUpperBound, executeSpec);
        return executeSpec;
    }
}
