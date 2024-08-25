package com.mocicarazvan.dayservice.repositories.impl;

import com.mocicarazvan.dayservice.enums.DayType;
import com.mocicarazvan.dayservice.mappers.DayMapper;
import com.mocicarazvan.dayservice.models.Day;
import com.mocicarazvan.dayservice.repositories.ExtendedDayRepository;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import com.mocicarazvan.templatemodule.utils.RepositoryUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class ExtendedDayRepositoryImpl implements ExtendedDayRepository {

    private final DatabaseClient databaseClient;
    private final DayMapper modelMapper;
    private final PageableUtilsCustom pageableUtils;
    private final RepositoryUtils repositoryUtils;

    private final String SELECT_ALL = "SELECT * FROM day";
    private final String COUNT_ALL = "SELECT COUNT(*) FROM day";

    @Override
    public Flux<Day> getDaysFiltered(String title, DayType type, PageRequest pageRequest, List<Long> excludeIds) {
        StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);
        appendWhereClause(queryBuilder, title, type, excludeIds);

        queryBuilder.append(pageableUtils.createPageRequestQuery(pageRequest));

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, type, excludeIds, queryBuilder);

        return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
    }

    @Override
    public Flux<Day> getDaysFilteredTrainer(String title, DayType type, PageRequest pageRequest, List<Long> excludeIds, Long trainerId) {
        StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);
        appendWhereClause(queryBuilder, title, type, excludeIds);
        repositoryUtils.addNotNullField(trainerId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > SELECT_ALL.length()),
                " user_id = :trainerId");
        queryBuilder.append(pageableUtils.createPageRequestQuery(pageRequest));

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, type, excludeIds, queryBuilder);
        executeSpec = repositoryUtils.bindNotNullField(trainerId, executeSpec, "trainerId");

        return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
    }

    @Override
    public Flux<Day> getDaysFilteredByIds(String title, DayType dayType, List<Long> ids, List<Long> excludeIds, PageRequest pageRequest) {
        StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);
        appendWhereClause(queryBuilder, title, dayType, excludeIds);

        repositoryUtils.addListField(ids, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > SELECT_ALL.length()),
                " id IN (:ids)");

        queryBuilder.append(pageableUtils.createPageRequestQuery(pageRequest));

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, dayType, excludeIds, queryBuilder);

        executeSpec = repositoryUtils.bindListField(ids, executeSpec, "ids");

        return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
    }


    @Override
    public Mono<Long> countDayFiltered(String title, DayType type, List<Long> excludeIds) {
        StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);

        appendWhereClause(queryBuilder, title, type, excludeIds);

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, type, excludeIds, queryBuilder);

        return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();

    }

    @Override
    public Mono<Long> countDayFilteredTrainer(String title, DayType type, List<Long> excludeIds, Long trainerId) {
        StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);

        appendWhereClause(queryBuilder, title, type, excludeIds);
        repositoryUtils.addNotNullField(trainerId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > COUNT_ALL.length()),
                " user_id = :trainerId");

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, type, excludeIds, queryBuilder);
        executeSpec = repositoryUtils.bindNotNullField(trainerId, executeSpec, "trainerId");

        return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
    }

    @Override
    public Mono<Long> countDayFilteredByIds(String title, DayType dayType, List<Long> ids, List<Long> excludeIds) {
        StringBuilder qStringBuilder = new StringBuilder(COUNT_ALL);
        appendWhereClause(qStringBuilder, title, dayType, excludeIds);
        repositoryUtils.addListField(ids, qStringBuilder, new RepositoryUtils.MutableBoolean(qStringBuilder.length() > COUNT_ALL.length()),
                " id IN (:ids)");

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, dayType, excludeIds, qStringBuilder);
        executeSpec = repositoryUtils.bindListField(ids, executeSpec, "ids");

        return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();


    }


    private DatabaseClient.GenericExecuteSpec getGenericExecuteSpec(String title, DayType type, List<Long> excludeIds, StringBuilder queryBuilder) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(queryBuilder.toString());
        executeSpec = repositoryUtils.bindStringSearchField(title, executeSpec, "title");
        executeSpec = repositoryUtils.bindEnumField(type, executeSpec, "type");
        executeSpec = repositoryUtils.bindListField(excludeIds, executeSpec, "excludeIds");
        return executeSpec;
    }

    private void appendWhereClause(StringBuilder queryBuilder, String title, DayType type, List<Long> excludeIds) {
        RepositoryUtils.MutableBoolean hasPreviousCriteria = new RepositoryUtils.MutableBoolean(false);
        repositoryUtils.addStringField(title, queryBuilder, hasPreviousCriteria, " UPPER(title) LIKE UPPER(:title)");
        repositoryUtils.addNotNullField(type, queryBuilder, hasPreviousCriteria, " type = :type");
        repositoryUtils.addListField(excludeIds, queryBuilder, hasPreviousCriteria,
                " id NOT IN (:excludeIds)");
    }
}
