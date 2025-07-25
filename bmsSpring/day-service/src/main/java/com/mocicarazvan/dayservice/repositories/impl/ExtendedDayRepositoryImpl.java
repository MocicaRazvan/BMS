package com.mocicarazvan.dayservice.repositories.impl;

import com.mocicarazvan.dayservice.enums.DayType;
import com.mocicarazvan.dayservice.mappers.DayMapper;
import com.mocicarazvan.dayservice.models.Day;
import com.mocicarazvan.dayservice.repositories.ExtendedDayRepository;
import com.mocicarazvan.ollamasearch.cache.EmbedCache;
import com.mocicarazvan.ollamasearch.services.OllamaAPIService;
import com.mocicarazvan.ollamasearch.utils.OllamaQueryUtils;
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
public class ExtendedDayRepositoryImpl implements ExtendedDayRepository {

    private final DatabaseClient databaseClient;
    private final DayMapper modelMapper;
    private final PageableUtilsCustom pageableUtils;
    private final RepositoryUtils repositoryUtils;
    private final OllamaQueryUtils ollamaQueryUtils;
    private final OllamaAPIService ollamaAPIService;
    private final EmbedCache embedCache;

    private final String SELECT_ALL = "SELECT d.* FROM day d join day_embedding e on d.id = e.entity_id ";
    private final String COUNT_ALL = "SELECT COUNT(d.id) FROM day d join day_embedding e on d.id = e.entity_id ";

    @Override
    public Flux<Day> getDaysFiltered(String title, DayType type, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                     LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageRequest pageRequest, List<Long> excludeIds) {


        return ollamaAPIService.getEmbedding(title, embedCache).flatMapMany(embeddings -> {
            StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);
            appendWhereClause(queryBuilder, title, embeddings, type, excludeIds, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

            pageableUtils.appendPageRequestQueryCallbackIfFieldIsNotEmpty(queryBuilder, pageRequest, embeddings, ollamaQueryUtils::addOrder);


            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, type, excludeIds, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound, queryBuilder);

            return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
        });
    }

    @Override
    public Flux<Day> getDaysFilteredTrainer(String title, DayType type, PageRequest pageRequest, List<Long> excludeIds, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                            LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, Long trainerId) {
        return ollamaAPIService.getEmbedding(title, embedCache).flatMapMany(embeddings -> {
            StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);
            appendWhereClause(queryBuilder, title, embeddings, type, excludeIds, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);
            repositoryUtils.addNotNullField(trainerId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > SELECT_ALL.length()),
                    " user_id = :trainerId");

            pageableUtils.appendPageRequestQueryCallbackIfFieldIsNotEmpty(queryBuilder, pageRequest, embeddings, ollamaQueryUtils::addOrder);


            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, type, excludeIds, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound, queryBuilder);
            executeSpec = repositoryUtils.bindNotNullField(trainerId, executeSpec, "trainerId");

            return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
        });
    }

    @Override
    public Flux<Day> getDaysFilteredByIds(String title, DayType dayType, List<Long> ids, List<Long> excludeIds, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                          LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageRequest pageRequest) {

        return ollamaAPIService.getEmbedding(title, embedCache).flatMapMany(embeddings -> {

            StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);
            appendWhereClause(queryBuilder, title, embeddings, dayType, excludeIds, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

            repositoryUtils.addListField(ids, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > SELECT_ALL.length()),
                    " d.id IN (:ids)");

            queryBuilder.append(pageableUtils.createPageRequestQuery(pageRequest));

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, dayType, excludeIds, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound, queryBuilder);

            executeSpec = repositoryUtils.bindListField(ids, executeSpec, "ids");

            return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
        });
    }


    @Override
    public Mono<Long> countDayFiltered(String title, DayType type, List<Long> excludeIds, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                       LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound) {

        return ollamaAPIService.getEmbedding(title, embedCache).flatMap(embeddings -> {
            StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);

            appendWhereClause(queryBuilder, title, embeddings, type, excludeIds, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, type, excludeIds, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound, queryBuilder);

            return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
        });

    }

    @Override
    public Mono<Long> countDayFilteredTrainer(String title, DayType type, List<Long> excludeIds, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                              LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, Long trainerId) {

        StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);
        return ollamaAPIService.getEmbedding(title, embedCache).flatMap(embeddings -> {
            appendWhereClause(queryBuilder, title, embeddings, type, excludeIds, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);
            repositoryUtils.addNotNullField(trainerId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > COUNT_ALL.length()),
                    " user_id = :trainerId");

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, type, excludeIds, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound, queryBuilder);
            executeSpec = repositoryUtils.bindNotNullField(trainerId, executeSpec, "trainerId");

            return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
        });
    }

    @Override
    public Mono<Long> countDayFilteredByIds(String title, DayType dayType, List<Long> ids, List<Long> excludeIds, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                            LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound) {
        return ollamaAPIService.getEmbedding(title, embedCache).flatMap(embeddings -> {

            StringBuilder qStringBuilder = new StringBuilder(COUNT_ALL);
            appendWhereClause(qStringBuilder, title, embeddings, dayType, excludeIds, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);
            repositoryUtils.addListField(ids, qStringBuilder, new RepositoryUtils.MutableBoolean(qStringBuilder.length() > COUNT_ALL.length()),
                    " d.id IN (:ids)");

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, dayType, excludeIds, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound, qStringBuilder);
            executeSpec = repositoryUtils.bindListField(ids, executeSpec, "ids");

            return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
        });


    }


    private DatabaseClient.GenericExecuteSpec getGenericExecuteSpec(String title, DayType type, List<Long> excludeIds, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                    LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, StringBuilder queryBuilder) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(queryBuilder.toString());
        executeSpec = repositoryUtils.bindStringField(title, executeSpec, "title");
        executeSpec = repositoryUtils.bindEnumField(type, executeSpec, "type");
        executeSpec = repositoryUtils.bindListField(excludeIds, executeSpec, "excludeIds");
        executeSpec = repositoryUtils.bindCreatedAtBound(createdAtLowerBound, createdAtUpperBound, executeSpec);
        executeSpec = repositoryUtils.bindUpdatedAtBound(updatedAtLowerBound, updatedAtUpperBound, executeSpec);
        return executeSpec;
    }

    private void appendWhereClause(StringBuilder queryBuilder, String title, String embeddings, DayType type, List<Long> excludeIds,
                                   LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                   LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound
    ) {
        RepositoryUtils.MutableBoolean hasPreviousCriteria = new RepositoryUtils.MutableBoolean(false);
        repositoryUtils.addStringField(title, queryBuilder, hasPreviousCriteria, ollamaQueryUtils.addThresholdFilter(embeddings, " OR title ILIKE '%' || :title || '%' OR title % :title  "));
        repositoryUtils.addCreatedAtBound("d", createdAtLowerBound, createdAtUpperBound, queryBuilder, hasPreviousCriteria);
        repositoryUtils.addUpdatedAtBound("d", updatedAtLowerBound, updatedAtUpperBound, queryBuilder, hasPreviousCriteria);

        repositoryUtils.addNotNullField(type, queryBuilder, hasPreviousCriteria, " type = :type");
        repositoryUtils.addListField(excludeIds, queryBuilder, hasPreviousCriteria,
                " d.id NOT IN (:excludeIds)");
    }
}
