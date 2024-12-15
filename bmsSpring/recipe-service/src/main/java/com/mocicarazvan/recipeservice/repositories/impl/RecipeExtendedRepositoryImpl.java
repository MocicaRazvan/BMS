package com.mocicarazvan.recipeservice.repositories.impl;

import com.mocicarazvan.ollamasearch.cache.EmbedCache;
import com.mocicarazvan.ollamasearch.service.OllamaAPIService;
import com.mocicarazvan.ollamasearch.utils.OllamaQueryUtils;
import com.mocicarazvan.recipeservice.enums.DietType;
import com.mocicarazvan.recipeservice.mappers.RecipeMapper;
import com.mocicarazvan.recipeservice.models.Recipe;
import com.mocicarazvan.recipeservice.repositories.RecipeExtendedRepository;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import com.mocicarazvan.templatemodule.utils.RepositoryUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor

public class RecipeExtendedRepositoryImpl implements RecipeExtendedRepository {
    private final DatabaseClient databaseClient;
    private final RecipeMapper modelMapper;
    private final PageableUtilsCustom pageableUtils;
    private final RepositoryUtils repositoryUtils;
    private final OllamaQueryUtils ollamaQueryUtils;
    private final OllamaAPIService ollamaAPIService;
    private final EmbedCache embedCache;


    private static final String SELECT_ALL = "SELECT r.* FROM recipe r join recipe_embedding e on r.id = e.entity_id ";
    private static final String COUNT_ALL = "SELECT COUNT(r.id) FROM recipe r join recipe_embedding e on r.id = e.entity_id ";


    @Override
    public Flux<Recipe> getRecipesFiltered(String title, Boolean approved, DietType type, PageRequest pageRequest) {

        Mono<String> embeddingsMono = repositoryUtils.isNotNullOrEmpty(title)
                ? ollamaAPIService.generateEmbeddingMono(title, embedCache).map(ollamaQueryUtils::getEmbeddingsAsString)
                : Mono.just("");

        StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);

        return embeddingsMono.flatMapMany(embeddings -> {
            appendWhereClause(queryBuilder, title, embeddings, approved, type);

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
            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, type, queryBuilder);

            return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
        });
    }

    @Override
    public Flux<Recipe> getRecipesFilteredTrainer(String title, Boolean approved, DietType type, Long trainerId, PageRequest pageRequest) {

        Mono<String> embeddingsMono = repositoryUtils.isNotNullOrEmpty(title)
                ? ollamaAPIService.generateEmbeddingMono(title, embedCache).map(ollamaQueryUtils::getEmbeddingsAsString)
                : Mono.just("");

        return embeddingsMono.flatMapMany(embeddings -> {


            StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);

            appendWhereClause(queryBuilder, title, embeddings, approved, type);

//        if (queryBuilder.length() > SELECT_ALL.length()) {
//            queryBuilder.append(" AND");
//        } else {
//            queryBuilder.append(" WHERE");
//        }
//        queryBuilder.append(" user_id = :trainerId");

            repositoryUtils.addNotNullField(trainerId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > SELECT_ALL.length()),
                    " user_id = :trainerId");

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

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, type, queryBuilder);

//        executeSpec = executeSpec.bind("trainerId", trainerId);

            executeSpec = repositoryUtils.bindNotNullField(trainerId, executeSpec, "trainerId");

            return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
        });
    }

    @Override
    public Mono<Long> countRecipesFiltered(String title, Boolean approved, DietType type) {
        Mono<String> embeddingsMono = repositoryUtils.isNotNullOrEmpty(title)
                ? ollamaAPIService.generateEmbeddingMono(title, embedCache).map(ollamaQueryUtils::getEmbeddingsAsString)
                : Mono.just("");

        return embeddingsMono.flatMap(embeddings -> {

            StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);

            appendWhereClause(queryBuilder, title, embeddings, approved, type);

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, type, queryBuilder);

            return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
        });
    }

    @Override
    public Mono<Long> countRecipesFilteredTrainer(String title, Boolean approved, Long trainerId, DietType type) {
        Mono<String> embeddingsMono = repositoryUtils.isNotNullOrEmpty(title)
                ? ollamaAPIService.generateEmbeddingMono(title, embedCache).map(ollamaQueryUtils::getEmbeddingsAsString)
                : Mono.just("");

        return embeddingsMono.flatMap(embeddings -> {


            StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);

            appendWhereClause(queryBuilder, title, embeddings, approved, type);

//        if (queryBuilder.length() > COUNT_ALL.length()) {
//            queryBuilder.append(" AND");
//        } else {
//            queryBuilder.append(" WHERE");
//        }
//        queryBuilder.append(" user_id = :trainerId");

            repositoryUtils.addNotNullField(trainerId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > COUNT_ALL.length()),
                    " user_id = :trainerId");

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, type, queryBuilder);

//        executeSpec = executeSpec.bind("trainerId", trainerId);


            executeSpec = repositoryUtils.bindNotNullField(trainerId, executeSpec, "trainerId");

            return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
        });
    }

    @Override
    public Mono<DietType> determineMostRestrictiveDietType(List<Long> recipeIds) {
        String DETERMINE_MOST_RESTRICTIVE_DIET_QUERY = """
                 WITH DietPriority AS (
                     SELECT
                         id,
                         CASE
                             WHEN type = 'OMNIVORE' THEN 3
                             WHEN type = 'VEGETARIAN' THEN 2
                             WHEN type = 'VEGAN' THEN 1
                             ELSE 0
                         END AS diet_priority
                     FROM recipe
                     WHERE id = ANY(:ids)
                 )
                 SELECT
                     CASE
                         WHEN MAX(diet_priority) = 3 THEN 'OMNIVORE'
                         WHEN MAX(diet_priority) = 2 THEN 'VEGETARIAN'
                         WHEN MAX(diet_priority) = 1 THEN 'VEGAN'
                         ELSE NULL
                     END AS most_restrictive_diet
                 FROM DietPriority;
                """;

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(DETERMINE_MOST_RESTRICTIVE_DIET_QUERY);

        executeSpec = repositoryUtils.bindArrayField(recipeIds, executeSpec, "ids", Long.class);
        return executeSpec.map((row, metadata) -> DietType.valueOf(
                Objects.requireNonNull(row.get("most_restrictive_diet", String.class)).toUpperCase()
        )).first();
    }

    private DatabaseClient.GenericExecuteSpec getGenericExecuteSpec(String title, Boolean approved, DietType type, StringBuilder queryBuilder) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(queryBuilder.toString());


        executeSpec = repositoryUtils.bindNotNullField(approved, executeSpec, "approved");

        executeSpec = repositoryUtils.bindStringField(title, executeSpec, "title");


        executeSpec = repositoryUtils.bindEnumField(type, executeSpec, "type");

        return executeSpec;
    }

    private void appendWhereClause(StringBuilder queryBuilder, String title, String embeddings, Boolean approved, DietType type) {

        RepositoryUtils.MutableBoolean hasPreviousCriteria = new RepositoryUtils.MutableBoolean(false);


        repositoryUtils.addNotNullField(approved, queryBuilder, hasPreviousCriteria, " approved = :approved");


        repositoryUtils.addStringField(title, queryBuilder, hasPreviousCriteria, ollamaQueryUtils.addThresholdFilter(embeddings, " OR title ILIKE '%' || :title || '%'"));


        repositoryUtils.addNotNullField(type, queryBuilder, hasPreviousCriteria, " type = :type");
    }
}
