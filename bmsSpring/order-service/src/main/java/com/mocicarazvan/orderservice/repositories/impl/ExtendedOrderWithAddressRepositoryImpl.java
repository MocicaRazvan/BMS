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
    public Flux<OrderWithAddress> getModelsFiltered(String city, String state, String country, PageRequest pageRequest) {
        StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);

        appendWhereClause(queryBuilder, city, state, country);

        queryBuilder.append(pageableUtilsCustom.createPageRequestQuery(pageRequest));

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(city, state, country, queryBuilder);

        return executeSpec.map((row, metadata) -> orderWithAddressMapper.fromRow(row)).all();
    }

    @Override
    public Mono<Long> countModelsFiltered(String city, String state, String country) {
        StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);

        appendWhereClause(queryBuilder, city, state, country);

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(city, state, country, queryBuilder);

        return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
    }

    @Override
    public Flux<OrderWithAddress> getModelsFilteredUser(String city, String state, String country, Long userId, PageRequest pageRequest) {
        StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);

        appendWhereClause(queryBuilder, city, state, country);

//        if (queryBuilder.length() > SELECT_ALL.length()) {
//            queryBuilder.append(" AND");
//        } else {
//            queryBuilder.append(" WHERE");
//        }
//        queryBuilder.append(" user_id = :userId");

        repositoryUtils.addNotNullField(userId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > SELECT_ALL.length()),
                " user_id = :userId");

        queryBuilder.append(pageableUtilsCustom.createPageRequestQuery(pageRequest));

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(city, state, country, queryBuilder);

//        executeSpec = executeSpec.bind("userId", userId);

        executeSpec = repositoryUtils.bindNotNullField(userId, executeSpec, "userId");

        return executeSpec.map((row, metadata) -> orderWithAddressMapper.fromRow(row)).all();
    }


    @Override
    public Mono<Long> countModelsFilteredUser(String city, String state, String country, Long userId) {
        StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);

//        if (queryBuilder.length() > COUNT_ALL.length()) {
//            queryBuilder.append(" AND");
//        } else {
//            queryBuilder.append(" WHERE");
//        }
//        queryBuilder.append(" user_id = :userId");

        repositoryUtils.addNotNullField(userId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > COUNT_ALL.length()),
                " user_id = :userId");

        appendWhereClause(queryBuilder, city, state, country);

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(city, state, country, queryBuilder);

//        executeSpec = executeSpec.bind("userId", userId);

        executeSpec = repositoryUtils.bindNotNullField(userId, executeSpec, "userId");

        return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
    }

    private void appendWhereClause(StringBuilder queryBuilder, String city, String state, String country) {
//        boolean hasPreviousCriteria = false;

        RepositoryUtils.MutableBoolean hasPreviousCriteria = new RepositoryUtils.MutableBoolean(false);

//        if (city != null && !city.isEmpty()) {
//            queryBuilder.append(" WHERE");
//            queryBuilder.append(" UPPER(city) LIKE UPPER(:city)");
//            hasPreviousCriteria = true;
//        }

        repositoryUtils.addStringField(city, queryBuilder, hasPreviousCriteria, " UPPER(city) LIKE UPPER(:city)");

//        if (state != null && !state.isEmpty()) {
//            queryBuilder.append(hasPreviousCriteria ? " AND" : " WHERE");
//            queryBuilder.append(" UPPER(state) LIKE UPPER(:state)");
//            hasPreviousCriteria = true;
//        }

        repositoryUtils.addStringField(state, queryBuilder, hasPreviousCriteria, " UPPER(state) LIKE UPPER(:state)");

//        if (country != null && !country.isEmpty()) {
//            queryBuilder.append(hasPreviousCriteria ? " AND" : " WHERE");
//            queryBuilder.append(" UPPER(country) LIKE UPPER(:country)");
//        }

        repositoryUtils.addStringField(country, queryBuilder, hasPreviousCriteria, " UPPER(country) LIKE UPPER(:country)");
    }


    private DatabaseClient.GenericExecuteSpec getGenericExecuteSpec(String city, String state, String country, StringBuilder queryBuilder) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(queryBuilder.toString());

//        if (city != null && !city.isEmpty()) {
//            executeSpec = executeSpec.bind("city", "%" + city + "%");
//        }

        executeSpec = repositoryUtils.bindStringSearchField(city, executeSpec, "city");

//        if (state != null && !state.isEmpty()) {
//            executeSpec = executeSpec.bind("state", "%" + state + "%");
//        }

        executeSpec = repositoryUtils.bindStringSearchField(state, executeSpec, "state");

//        if (country != null && !country.isEmpty()) {
//            executeSpec = executeSpec.bind("country", "%" + country + "%");
//        }

        executeSpec = repositoryUtils.bindStringSearchField(country, executeSpec, "country");

        return executeSpec;
    }
}
