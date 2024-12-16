package com.mocicarazvan.dayservice.repositories.impl;

import com.mocicarazvan.dayservice.cache.EmbedDayCache;
import com.mocicarazvan.dayservice.enums.DayType;
import com.mocicarazvan.dayservice.mappers.DayMapper;
import com.mocicarazvan.dayservice.models.Day;
import com.mocicarazvan.dayservice.repositories.ExtendedDayRepository;
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
    private final EmbedDayCache embedDayCache;

    private final String SELECT_ALL = "SELECT d.* FROM day d join day_embedding e on d.id = e.entity_id ";
    private final String COUNT_ALL = "SELECT COUNT(d.id) FROM day d join day_embedding e on d.id = e.entity_id ";

    @Override
    public Flux<Day> getDaysFiltered(String title, DayType type, PageRequest pageRequest, List<Long> excludeIds) {

        Mono<String> embeddingsMono = repositoryUtils.isNotNullOrEmpty(title)
                ? ollamaAPIService.generateEmbeddingMono(title, embedDayCache).map(ollamaQueryUtils::getEmbeddingsAsString)
                : Mono.just("");

        return embeddingsMono.flatMapMany(embeddings -> {
            StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);
            appendWhereClause(queryBuilder, title, embeddings, type, excludeIds);

//        queryBuilder.append(pageableUtils.createPageRequestQuery(pageRequest));
            if (repositoryUtils.isNotNullOrEmpty(embeddings)) {
                queryBuilder.append(pageableUtils.createPageRequestQuery(pageRequest,
                        ollamaQueryUtils.addOrder(
                                embeddings
                        )
                ));
            } else {
                queryBuilder.append(pageableUtils.createPageRequestQuery(pageRequest));
            }


            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, type, excludeIds, queryBuilder);

            return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
        });
    }

    @Override
    public Flux<Day> getDaysFilteredTrainer(String title, DayType type, PageRequest pageRequest, List<Long> excludeIds, Long trainerId) {
        Mono<String> embeddingsMono = repositoryUtils.isNotNullOrEmpty(title)
                ? ollamaAPIService.generateEmbeddingMono(title, embedDayCache).map(ollamaQueryUtils::getEmbeddingsAsString)
                : Mono.just("");


        return embeddingsMono.flatMapMany(embeddings -> {
            StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);
            appendWhereClause(queryBuilder, title, embeddings, type, excludeIds);
            repositoryUtils.addNotNullField(trainerId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > SELECT_ALL.length()),
                    " user_id = :trainerId");

            if (repositoryUtils.isNotNullOrEmpty(embeddings)) {
                queryBuilder.append(pageableUtils.createPageRequestQuery(pageRequest,
                        ollamaQueryUtils.addOrder(
                                embeddings
                        )
                ));
            } else {
                queryBuilder.append(pageableUtils.createPageRequestQuery(pageRequest));
            }

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, type, excludeIds, queryBuilder);
            executeSpec = repositoryUtils.bindNotNullField(trainerId, executeSpec, "trainerId");

            return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
        });
    }

    @Override
    public Flux<Day> getDaysFilteredByIds(String title, DayType dayType, List<Long> ids, List<Long> excludeIds, PageRequest pageRequest) {

        Mono<String> embeddingsMono = repositoryUtils.isNotNullOrEmpty(title)
                ? ollamaAPIService.generateEmbeddingMono(title, embedDayCache).map(ollamaQueryUtils::getEmbeddingsAsString)
                : Mono.just("");

        return embeddingsMono.flatMapMany(embeddings -> {

            StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);
            appendWhereClause(queryBuilder, title, embeddings, dayType, excludeIds);

            repositoryUtils.addListField(ids, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > SELECT_ALL.length()),
                    " d.id IN (:ids)");

            queryBuilder.append(pageableUtils.createPageRequestQuery(pageRequest));

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, dayType, excludeIds, queryBuilder);

            executeSpec = repositoryUtils.bindListField(ids, executeSpec, "ids");

            return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
        });
    }


    @Override
    public Mono<Long> countDayFiltered(String title, DayType type, List<Long> excludeIds) {

        Mono<String> embeddingsMono = repositoryUtils.isNotNullOrEmpty(title)
                ? ollamaAPIService.generateEmbeddingMono(title, embedDayCache).map(ollamaQueryUtils::getEmbeddingsAsString)
                : Mono.just("");

        return embeddingsMono.flatMap(embeddings -> {
            StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);

            appendWhereClause(queryBuilder, title, embeddings, type, excludeIds);

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, type, excludeIds, queryBuilder);

            return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
        });

    }

    @Override
    public Mono<Long> countDayFilteredTrainer(String title, DayType type, List<Long> excludeIds, Long trainerId) {

        Mono<String> embeddingsMono = repositoryUtils.isNotNullOrEmpty(title)
                ? ollamaAPIService.generateEmbeddingMono(title, embedDayCache).map(ollamaQueryUtils::getEmbeddingsAsString)
                : Mono.just("");


        StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);
        return embeddingsMono.flatMap(embeddings -> {
            appendWhereClause(queryBuilder, title, embeddings, type, excludeIds);
            repositoryUtils.addNotNullField(trainerId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > COUNT_ALL.length()),
                    " user_id = :trainerId");

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, type, excludeIds, queryBuilder);
            executeSpec = repositoryUtils.bindNotNullField(trainerId, executeSpec, "trainerId");

            return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
        });
    }

    @Override
    public Mono<Long> countDayFilteredByIds(String title, DayType dayType, List<Long> ids, List<Long> excludeIds) {
        Mono<String> embeddingsMono = repositoryUtils.isNotNullOrEmpty(title)
                ? ollamaAPIService.generateEmbeddingMono(title, embedDayCache).map(ollamaQueryUtils::getEmbeddingsAsString)
                : Mono.just("");
        return embeddingsMono.flatMap(embeddings -> {

            StringBuilder qStringBuilder = new StringBuilder(COUNT_ALL);
            appendWhereClause(qStringBuilder, title, embeddings, dayType, excludeIds);
            repositoryUtils.addListField(ids, qStringBuilder, new RepositoryUtils.MutableBoolean(qStringBuilder.length() > COUNT_ALL.length()),
                    " d.id IN (:ids)");

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, dayType, excludeIds, qStringBuilder);
            executeSpec = repositoryUtils.bindListField(ids, executeSpec, "ids");

            return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
        });


    }


    private DatabaseClient.GenericExecuteSpec getGenericExecuteSpec(String title, DayType type, List<Long> excludeIds, StringBuilder queryBuilder) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(queryBuilder.toString());
//        executeSpec = repositoryUtils.bindStringSearchField(title, executeSpec, "title");
        executeSpec = repositoryUtils.bindStringField(title, executeSpec, "title");
        executeSpec = repositoryUtils.bindEnumField(type, executeSpec, "type");
        executeSpec = repositoryUtils.bindListField(excludeIds, executeSpec, "excludeIds");
        return executeSpec;
    }

    private void appendWhereClause(StringBuilder queryBuilder, String title, String embeddings, DayType type, List<Long> excludeIds) {
        RepositoryUtils.MutableBoolean hasPreviousCriteria = new RepositoryUtils.MutableBoolean(false);
//        repositoryUtils.addStringField(title, queryBuilder, hasPreviousCriteria, " UPPER(title) LIKE UPPER(:title)");
        repositoryUtils.addStringField(title, queryBuilder, hasPreviousCriteria, ollamaQueryUtils.addThresholdFilter(embeddings, " OR title ILIKE '%' || :title || '%' OR similarity(title, :title ) > 0.35  "));

        repositoryUtils.addNotNullField(type, queryBuilder, hasPreviousCriteria, " type = :type");
        repositoryUtils.addListField(excludeIds, queryBuilder, hasPreviousCriteria,
                " d.id NOT IN (:excludeIds)");
    }
}
