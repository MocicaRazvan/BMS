package com.mocicarazvan.planservice.repositories.impl;

import com.mocicarazvan.ollamasearch.cache.EmbedCache;
import com.mocicarazvan.ollamasearch.services.OllamaAPIService;
import com.mocicarazvan.ollamasearch.utils.OllamaQueryUtils;
import com.mocicarazvan.planservice.enums.DietType;
import com.mocicarazvan.planservice.enums.ObjectiveType;
import com.mocicarazvan.planservice.mappers.PlanMapper;
import com.mocicarazvan.planservice.models.Plan;
import com.mocicarazvan.planservice.repositories.ExtendedPlanRepository;
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
public class ExtendedPlanRepositoryImpl implements ExtendedPlanRepository {

    private final DatabaseClient databaseClient;
    private final PlanMapper modelMapper;
    private final PageableUtilsCustom pageableUtils;
    private final RepositoryUtils repositoryUtils;
    private final OllamaQueryUtils ollamaQueryUtils;
    private final OllamaAPIService ollamaAPIService;
    private final EmbedCache embedCache;

    private static final String SELECT_ALL = "SELECT p.* FROM plan p join plan_embedding e on p.id = e.entity_id ";
    private static final String COUNT_ALL = "SELECT COUNT(p.id) FROM plan p join plan_embedding e on p.id = e.entity_id ";

    @Override
    public Flux<Plan> getPlansFiltered(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, PageRequest pageRequest, List<Long> excludeIds,
                                       LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                       LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound
    ) {
        return ollamaAPIService.getEmbedding(title, embedCache).flatMapMany(embeddings -> {
            StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);


            appendWhereClause(queryBuilder, title, embeddings, approved, display, type, objective, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

            repositoryUtils.addListField(excludeIds, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > SELECT_ALL.length()),
                    " p.id NOT IN (:excludeIds)");

            pageableUtils.appendPageRequestQueryCallbackIfFieldIsNotEmpty(queryBuilder, pageRequest, embeddings, ollamaQueryUtils::addOrder);


            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, type, objective, display,
                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                    queryBuilder);


            executeSpec = repositoryUtils.bindListField(excludeIds, executeSpec, "excludeIds");

            return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
        });
    }

    @Override
    public Flux<Plan> getPlansFilteredTrainer(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, Long trainerId,
                                              LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                              LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                              PageRequest pageRequest) {

        return ollamaAPIService.getEmbedding(title, embedCache).flatMapMany(embeddings -> {

            StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);


            appendWhereClause(queryBuilder, title, embeddings, approved, display, type, objective, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);


            repositoryUtils.addNotNullField(trainerId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > SELECT_ALL.length()),
                    " user_id = :trainerId");

            pageableUtils.appendPageRequestQueryCallbackIfFieldIsNotEmpty(queryBuilder, pageRequest, embeddings, ollamaQueryUtils::addOrder);

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, type, objective, display,
                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                    queryBuilder);


            executeSpec = repositoryUtils.bindNotNullField(trainerId, executeSpec, "trainerId");
            return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
        });
    }

    @Override
    public Mono<Long> countPlansFiltered(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> excludeIds,
                                         LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                         LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound) {
        return ollamaAPIService.getEmbedding(title, embedCache).flatMap(embeddings -> {
            StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);

            appendWhereClause(queryBuilder, title, embeddings, approved, display, type, objective, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);


            repositoryUtils.addListField(excludeIds, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > COUNT_ALL.length()),
                    " p.id NOT IN (:excludeIds)");

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, type, objective, display,
                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                    queryBuilder);


            executeSpec = repositoryUtils.bindListField(excludeIds, executeSpec, "excludeIds");

            return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
        });
    }

    @Override
    public Mono<Long> countPlansFilteredTrainer(String title, Boolean approved, Boolean display, Long trainerId, DietType type, ObjectiveType objective,
                                                LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound
    ) {
        return ollamaAPIService.getEmbedding(title, embedCache).flatMap(embeddings -> {


            StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);


            appendWhereClause(queryBuilder, title, embeddings, approved, display, type, objective, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);


            repositoryUtils.addNotNullField(trainerId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > COUNT_ALL.length()),
                    " user_id = :trainerId");

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, type, objective, display,
                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                    queryBuilder);


            executeSpec = repositoryUtils.bindNotNullField(trainerId, executeSpec, "trainerId");
            return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
        });
    }

    @Override
    public Flux<Plan> getPlansFilteredByIds(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> ids,
                                            LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                            LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                            PageRequest pageRequest) {
        StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);
        return ollamaAPIService.getEmbedding(title, embedCache).flatMapMany(embeddings -> {
            appendWhereClause(queryBuilder, title, embeddings, approved, display, type, objective, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

            repositoryUtils.addListField(ids, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > SELECT_ALL.length()),
                    " p.id IN (:ids)");

            queryBuilder.append(pageableUtils.createPageRequestQuery(pageRequest));

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, type, objective, display,
                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                    queryBuilder);


            executeSpec = repositoryUtils.bindListField(ids, executeSpec, "ids");

            return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
        });
    }

    @Override
    public Mono<Long> countPlansFilteredByIds(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> ids, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                              LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound) {
        return ollamaAPIService.getEmbedding(title, embedCache).flatMap(embeddings -> {

            StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);

            appendWhereClause(queryBuilder, title, embeddings, approved, display, type, objective, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);


            repositoryUtils.addListField(ids, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > COUNT_ALL.length()),
                    " p.id IN (:ids)");

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, type, objective, display,
                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                    queryBuilder);


            executeSpec = repositoryUtils.bindListField(ids, executeSpec, "ids");

            return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
        });
    }

    private DatabaseClient.GenericExecuteSpec getGenericExecuteSpec(String title, Boolean approved, DietType type, ObjectiveType objective, Boolean display,
                                                                    LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                    LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                    StringBuilder queryBuilder) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(queryBuilder.toString());


        executeSpec = repositoryUtils.bindNotNullField(approved, executeSpec, "approved");
        executeSpec = repositoryUtils.bindNotNullField(display, executeSpec, "display");
        executeSpec = repositoryUtils.bindStringField(title, executeSpec, "title");
        executeSpec = repositoryUtils.bindEnumField(type, executeSpec, "type");
        executeSpec = repositoryUtils.bindEnumField(objective, executeSpec, "objective");
        executeSpec = repositoryUtils.bindCreatedAtBound(createdAtLowerBound, createdAtUpperBound, executeSpec);
        executeSpec = repositoryUtils.bindUpdatedAtBound(updatedAtLowerBound, updatedAtUpperBound, executeSpec);
        return executeSpec;
    }

    private void appendWhereClause(StringBuilder queryBuilder, String title, String embeddings, Boolean approved, Boolean display, DietType type, ObjectiveType objective,
                                   LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                   LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound
    ) {

        RepositoryUtils.MutableBoolean hasPreviousCriteria = new RepositoryUtils.MutableBoolean(false);
        repositoryUtils.addNotNullField(approved, queryBuilder, hasPreviousCriteria, "approved = :approved");
        repositoryUtils.addNotNullField(display, queryBuilder, hasPreviousCriteria, "display = :display");
        repositoryUtils.addStringField(title, queryBuilder, hasPreviousCriteria, ollamaQueryUtils.addThresholdFilter(embeddings, " OR title ILIKE '%' || :title || '%' OR title % :title "));
        repositoryUtils.addNotNullField(type, queryBuilder, hasPreviousCriteria, " type = :type");
        repositoryUtils.addNotNullField(objective, queryBuilder, hasPreviousCriteria, " objective = :objective");
        repositoryUtils.addCreatedAtBound("p", createdAtLowerBound, createdAtUpperBound, queryBuilder, hasPreviousCriteria);
        repositoryUtils.addUpdatedAtBound("p", updatedAtLowerBound, updatedAtUpperBound, queryBuilder, hasPreviousCriteria);
    }
}
