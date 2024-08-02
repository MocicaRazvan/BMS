package com.mocicarazvan.ingredientservice.repositories.impl;

import com.mocicarazvan.ingredientservice.enums.DietType;
import com.mocicarazvan.ingredientservice.mappers.IngredientNutritionalFactMapper;
import com.mocicarazvan.ingredientservice.models.IngredientNutritionalFact;
import com.mocicarazvan.ingredientservice.repositories.ExtendedIngredientNutritionalFactRepository;
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

public class ExtendedIngredientNutritionalFactRepositoryImpl implements ExtendedIngredientNutritionalFactRepository {
    private final DatabaseClient databaseClient;
    private final IngredientNutritionalFactMapper ingredientNutritionalFactMapper;
    private final PageableUtilsCustom pageableUtilsCustom;
    private final RepositoryUtils requestsUtils;

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
            """;

    private static final String COUNT_ALL = """
            SELECT COUNT(*)
            FROM ingredient
            LEFT JOIN nutritional_fact ON ingredient.id = nutritional_fact.ingredient_id
            """;

    @Override
    public Flux<IngredientNutritionalFact> getModelsFiltered(String name, Boolean display, DietType type, PageRequest pageRequest) {
        StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);

        appendWhereClause(queryBuilder, name, display, type);

        queryBuilder.append(pageableUtilsCustom.createPageRequestQuery(pageRequest));

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(name, display, type, queryBuilder);

        return executeSpec.map((row, metadata) -> ingredientNutritionalFactMapper.fromRowToModel(row)).all();
    }

    @Override
    public Mono<Long> countModelsFiltered(String name, Boolean display, DietType type, PageRequest pageRequest) {
        StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);

        appendWhereClause(queryBuilder, name, display, type);

        DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(name, display, type, queryBuilder);

        return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
    }

    private void appendWhereClause(StringBuilder queryBuilder, String name, Boolean display, DietType type) {
//        boolean hasPreviousCriteria = false;

        RepositoryUtils.MutableBoolean hasPreviousCriteria = new RepositoryUtils.MutableBoolean(false);

//        if (name != null && !name.isEmpty()) {
//            queryBuilder.append(" WHERE");
//            queryBuilder.append(" UPPER(ingredient.name) LIKE UPPER(:name)");
//            hasPreviousCriteria = true;
//        }

        requestsUtils.addStringField(name, queryBuilder, hasPreviousCriteria, " UPPER(ingredient.name) LIKE UPPER(:name)");

//        if (display != null) {
//            queryBuilder.append(hasPreviousCriteria ? " AND" : " WHERE");
//            queryBuilder.append(" ingredient.display = :display");
//            hasPreviousCriteria = true;
//        }

        requestsUtils.addNotNullField(display, queryBuilder, hasPreviousCriteria, " ingredient.display = :display");

//        if (type != null) {
//            queryBuilder.append(hasPreviousCriteria ? " AND" : " WHERE");
//            queryBuilder.append(" ingredient.type = :type");
//        }

        requestsUtils.addNotNullField(type, queryBuilder, hasPreviousCriteria, " ingredient.type = :type");

    }

    private DatabaseClient.GenericExecuteSpec getGenericExecuteSpec(String name, Boolean display, DietType type, StringBuilder queryBuilder) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(queryBuilder.toString());

//        if (name != null && !name.isEmpty()) {
//            executeSpec = executeSpec.bind("name", "%" + name + "%");
//        }

        requestsUtils.bindStringSearchField(name, executeSpec, "name");

//        if (display != null) {
//            executeSpec = executeSpec.bind("display", display);
//        }

        requestsUtils.bindNotNullField(display, executeSpec, "display");

//        if (type != null) {
//            executeSpec = executeSpec.bind("type", type.name());
//        }


        requestsUtils.bindEnumField(type, executeSpec, "type");

        return executeSpec;
    }
}
