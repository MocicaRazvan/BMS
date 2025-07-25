package com.mocicarazvan.ingredientservice.repositories.impl;

import com.mocicarazvan.ingredientservice.enums.DietType;
import com.mocicarazvan.ingredientservice.mappers.IngredientNutritionalFactMapper;
import com.mocicarazvan.ingredientservice.models.IngredientNutritionalFact;
import com.mocicarazvan.ingredientservice.repositories.ExtendedIngredientNutritionalFactRepository;
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


@Repository
@RequiredArgsConstructor

public class ExtendedIngredientNutritionalFactRepositoryImpl implements ExtendedIngredientNutritionalFactRepository {
    private final DatabaseClient databaseClient;
    private final IngredientNutritionalFactMapper ingredientNutritionalFactMapper;
    private final PageableUtilsCustom pageableUtilsCustom;
    private final RepositoryUtils repositoryUtils;
    private final OllamaQueryUtils ollamaQueryUtils;
    private final OllamaAPIService ollamaAPIService;
    private final EmbedCache embedCache;

    private static final String SELECT_ALL = """
            SELECT
                ingredient.*,
                nutritional_fact.id AS n_id, nutritional_fact.created_at AS n_created_at, nutritional_fact.updated_at AS n_updated_at,
                nutritional_fact.user_id AS n_user_id, nutritional_fact.fat AS n_fat,
                nutritional_fact.saturated_fat AS n_saturated_fat, nutritional_fact.carbohydrates AS n_carbohydrates,
                nutritional_fact.sugar AS n_sugar, nutritional_fact.protein AS n_protein, nutritional_fact.salt AS n_salt,
                nutritional_fact.unit AS n_unit, nutritional_fact.ingredient_id AS n_ingredient_id ,
                (nutritional_fact.protein * 3.47 + nutritional_fact.fat * 8.37 + nutritional_fact.carbohydrates * 4.07) AS calories
            FROM ingredient
            JOIN nutritional_fact ON ingredient.id = nutritional_fact.ingredient_id
            JOIN ingredient_embedding e ON ingredient.id = e.entity_id
            """;

    private static final String COUNT_ALL = """
            SELECT COUNT(*)
            FROM ingredient
            LEFT JOIN nutritional_fact ON ingredient.id = nutritional_fact.ingredient_id
            JOIN ingredient_embedding e ON ingredient.id = e.entity_id
            """;

    @Override
    public Flux<IngredientNutritionalFact> getModelsFiltered(String name, Boolean display, DietType type,
                                                             LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                             LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                             PageRequest pageRequest) {


        return ollamaAPIService.getEmbedding(name, embedCache).flatMapMany(embeddings -> {
            StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);


            appendWhereClause(queryBuilder, name, embeddings, display, type, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

//        queryBuilder.append(pageableUtilsCustom.createPageRequestQuery(pageRequest));

            pageableUtilsCustom.appendPageRequestQueryCallbackIfFieldIsNotEmpty(queryBuilder, pageRequest, embeddings, ollamaQueryUtils::addOrder);

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(name, display, type,
                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                    queryBuilder);

            return executeSpec.map((row, metadata) -> ingredientNutritionalFactMapper.fromRowToModel(row)).all();
        });
    }

    @Override
    public Mono<Long> countModelsFiltered(String name, Boolean display, DietType type, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                          LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound) {
        return ollamaAPIService.getEmbedding(name, embedCache).flatMap(embeddings -> {

            StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);


            appendWhereClause(queryBuilder, name, embeddings, display, type, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(name, display, type,
                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                    queryBuilder);

            return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
        });
    }

    private void appendWhereClause(StringBuilder queryBuilder, String name, String embeddings, Boolean display, DietType type,
                                   LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                   LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound) {

        RepositoryUtils.MutableBoolean hasPreviousCriteria = new RepositoryUtils.MutableBoolean(false);


        repositoryUtils.addStringField(name, queryBuilder, hasPreviousCriteria, ollamaQueryUtils.addThresholdFilter(embeddings, " OR name ILIKE '%' || :name || '%' OR name % :name "));


        repositoryUtils.addNotNullField(display, queryBuilder, hasPreviousCriteria, " ingredient.display = :display");


        repositoryUtils.addNotNullField(type, queryBuilder, hasPreviousCriteria, " ingredient.type = :type");

        repositoryUtils.addCreatedAtBound("ingredient", createdAtLowerBound, createdAtUpperBound, queryBuilder, hasPreviousCriteria);
        repositoryUtils.addUpdatedAtBound("ingredient", updatedAtLowerBound, updatedAtUpperBound, queryBuilder, hasPreviousCriteria);

    }

    private DatabaseClient.GenericExecuteSpec getGenericExecuteSpec(String name, Boolean display, DietType type,
                                                                    LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                    LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                    StringBuilder queryBuilder) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(queryBuilder.toString());

        executeSpec = repositoryUtils.bindStringField(name, executeSpec, "name");
        executeSpec = repositoryUtils.bindNotNullField(display, executeSpec, "display");
        executeSpec = repositoryUtils.bindEnumField(type, executeSpec, "type");
        executeSpec = repositoryUtils.bindCreatedAtBound(createdAtLowerBound, createdAtUpperBound, executeSpec);
        executeSpec = repositoryUtils.bindUpdatedAtBound(updatedAtLowerBound, updatedAtUpperBound, executeSpec);
        return executeSpec;
    }
}
