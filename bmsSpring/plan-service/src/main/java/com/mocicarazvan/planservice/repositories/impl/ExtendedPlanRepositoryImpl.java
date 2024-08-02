package com.mocicarazvan.planservice.repositories.impl;

import com.mocicarazvan.planservice.enums.DietType;
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

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ExtendedPlanRepositoryImpl implements ExtendedPlanRepository {

    private final DatabaseClient databaseClient;
    private final PlanMapper modelMapper;
    private final PageableUtilsCustom pageableUtils;
    private final RepositoryUtils repositoryUtils;

    private static final String SELECT_ALL = "SELECT * FROM plan";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM plan";

    @Override
    public Flux<Plan> getPlansFiltered(String title, Boolean approved, Boolean display, DietType type, PageRequest pageRequest, List<Long> excludeIds) {
        StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);

        appendWhereClause(queryBuilder, title, approved, display, type);

//        if (excludeIds != null && !excludeIds.isEmpty()) {
//            if (queryBuilder.length() > SELECT_ALL.length()) {
//                queryBuilder.append(" AND");
//            } else {
//                queryBuilder.append(" WHERE");
//            }
//            queryBuilder.append(" id NOT IN (:excludeIds)");
//        }

        repositoryUtils.addListField(excludeIds, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > SELECT_ALL.length()),
                " id NOT IN (:excludeIds)");

        queryBuilder.append(pageableUtils.createPageRequestQuery(pageRequest));

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, type, display, queryBuilder);

//        if (excludeIds != null && !excludeIds.isEmpty()) {
//            executeSpec = executeSpec.bind("excludeIds", excludeIds);
//        }

        executeSpec = repositoryUtils.bindListField(excludeIds, executeSpec, "excludeIds");

        return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
    }

    @Override
    public Flux<Plan> getPlansFilteredTrainer(String title, Boolean approved, Boolean display, DietType type, Long trainerId, PageRequest pageRequest) {
        StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);

        appendWhereClause(queryBuilder, title, approved, display, type);

//        if (queryBuilder.length() > SELECT_ALL.length()) {
//            queryBuilder.append(" AND");
//        } else {
//            queryBuilder.append(" WHERE");
//        }
//        queryBuilder.append(" user_id = :trainerId");

        repositoryUtils.addNotNullField(trainerId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > SELECT_ALL.length()),
                " user_id = :trainerId");

        queryBuilder.append(pageableUtils.createPageRequestQuery(pageRequest));

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, type, display, queryBuilder);

//        executeSpec = executeSpec.bind("trainerId", trainerId);

        executeSpec = repositoryUtils.bindNotNullField(trainerId, executeSpec, "trainerId");
        return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
    }

    @Override
    public Mono<Long> countPlansFiltered(String title, Boolean approved, Boolean display, DietType type, List<Long> excludeIds) {
        StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);

        appendWhereClause(queryBuilder, title, approved, display, type);

//        if (excludeIds != null && !excludeIds.isEmpty()) {
//            if (queryBuilder.length() > COUNT_ALL.length()) {
//                queryBuilder.append(" AND");
//            } else {
//                queryBuilder.append(" WHERE");
//            }
//            queryBuilder.append(" id NOT IN (:excludeIds)");
//        }

        repositoryUtils.addListField(excludeIds, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > COUNT_ALL.length()),
                " id NOT IN (:excludeIds)");

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, type, display, queryBuilder);


//        if (excludeIds != null && !excludeIds.isEmpty()) {
//            executeSpec = executeSpec.bind("excludeIds", excludeIds);
//        }

        executeSpec = repositoryUtils.bindListField(excludeIds, executeSpec, "excludeIds");

        return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
    }

    @Override
    public Mono<Long> countPlansFilteredTrainer(String title, Boolean approved, Boolean display, Long trainerId, DietType type) {
        StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);

        appendWhereClause(queryBuilder, title, approved, display, type);

//        if (queryBuilder.length() > COUNT_ALL.length()) {
//            queryBuilder.append(" AND");
//        } else {
//            queryBuilder.append(" WHERE");
//        }
//        queryBuilder.append(" user_id = :trainerId");

        repositoryUtils.addNotNullField(trainerId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > COUNT_ALL.length()),
                " user_id = :trainerId");

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, type, display, queryBuilder);

//        executeSpec = executeSpec.bind("trainerId", trainerId);

        executeSpec = repositoryUtils.bindNotNullField(trainerId, executeSpec, "trainerId");
        return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
    }

    @Override
    public Flux<Plan> getPlansFilteredByIds(String title, Boolean approved, Boolean display, DietType type, List<Long> ids, PageRequest pageRequest) {
        StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);

        appendWhereClause(queryBuilder, title, approved, display, type);

//        if (queryBuilder.length() > SELECT_ALL.length()) {
//            queryBuilder.append(" AND");
//        } else {
//            queryBuilder.append(" WHERE");
//        }
//        queryBuilder.append(" id IN (:ids)");

        repositoryUtils.addListField(ids, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > SELECT_ALL.length()),
                " id IN (:ids)");

        queryBuilder.append(pageableUtils.createPageRequestQuery(pageRequest));

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, type, display, queryBuilder);

//        executeSpec = executeSpec.bind("ids", ids);

        executeSpec = repositoryUtils.bindListField(ids, executeSpec, "ids");

        return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
    }

    @Override
    public Mono<Long> countPlansFilteredByIds(String title, Boolean approved, Boolean display, DietType type, List<Long> ids) {
        StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);

        appendWhereClause(queryBuilder, title, approved, display, type);

//        if (queryBuilder.length() > COUNT_ALL.length()) {
//            queryBuilder.append(" AND");
//        } else {
//            queryBuilder.append(" WHERE");
//        }
//        queryBuilder.append(" id IN (:ids)");


        repositoryUtils.addListField(ids, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > COUNT_ALL.length()),
                " id IN (:ids)");

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, type, display, queryBuilder);

//        executeSpec = executeSpec.bind("ids", ids);

        executeSpec = repositoryUtils.bindListField(ids, executeSpec, "ids");

        return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
    }

    private DatabaseClient.GenericExecuteSpec getGenericExecuteSpec(String title, Boolean approved, DietType type, Boolean display, StringBuilder queryBuilder) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(queryBuilder.toString());

//        if (approved != null) {
//            executeSpec = executeSpec.bind("approved", approved);
//        }

        executeSpec = repositoryUtils.bindNotNullField(approved, executeSpec, "approved");
//        if (display != null) {
//            executeSpec = executeSpec.bind("display", display);
//        }

        executeSpec = repositoryUtils.bindNotNullField(display, executeSpec, "display");
//        if (title != null && !title.isEmpty()) {
//            executeSpec = executeSpec.bind("title", "%" + title + "%");
//        }

        executeSpec = repositoryUtils.bindStringSearchField(title, executeSpec, "title");

//        if (type != null) {
//            executeSpec = executeSpec.bind("type", type.name());
//        }

        executeSpec = repositoryUtils.bindEnumField(type, executeSpec, "type");

        return executeSpec;
    }

    private void appendWhereClause(StringBuilder queryBuilder, String title, Boolean approved, Boolean display, DietType type) {
//        boolean hasPreviousCriteria = false;

        RepositoryUtils.MutableBoolean hasPreviousCriteria = new RepositoryUtils.MutableBoolean(false);

//        if (approved != null) {
//            queryBuilder.append(" WHERE approved = :approved");
//            hasPreviousCriteria = true;
//        }

        repositoryUtils.addNotNullField(approved, queryBuilder, hasPreviousCriteria, "approved = :approved");

//        if (display != null) {
//            if (hasPreviousCriteria) {
//                queryBuilder.append(" AND");
//            } else {
//                queryBuilder.append(" WHERE");
//            }
//            queryBuilder.append(" display = :display");
//            hasPreviousCriteria = true;
//        }

        repositoryUtils.addNotNullField(display, queryBuilder, hasPreviousCriteria, "display = :display");

//        if (title != null && !title.isEmpty()) {
//            if (hasPreviousCriteria) {
//                queryBuilder.append(" AND");
//            } else {
//                queryBuilder.append(" WHERE");
//            }
//            queryBuilder.append(" UPPER(title) LIKE UPPER(:title)");
//            hasPreviousCriteria = true;
//        }

        repositoryUtils.addStringField(title, queryBuilder, hasPreviousCriteria, " UPPER(title) LIKE UPPER(:title)");

//        if (type != null) {
//            if (hasPreviousCriteria) {
//                queryBuilder.append(" AND");
//            } else {
//                queryBuilder.append(" WHERE");
//            }
//            queryBuilder.append(" type = :type");
//        }

        repositoryUtils.addNotNullField(type, queryBuilder, hasPreviousCriteria, " type = :type");
    }
}
