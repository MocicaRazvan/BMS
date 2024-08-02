package com.mocicarazvan.ingredientservice.repositories.impl;

import com.mocicarazvan.ingredientservice.models.Ingredient;
import com.mocicarazvan.ingredientservice.repositories.CustomIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
@RequiredArgsConstructor
public class CustomIngredientRepositoryImpl implements CustomIngredientRepository {

    private final R2dbcEntityTemplate r2dbcEntityTemplate;


    @Override
    public Flux<Ingredient> findAllByExample(Ingredient example, Pageable pageable) {
        Query query = Query.query(buildCriteria(example)).with(pageable);
        return r2dbcEntityTemplate.select(query, Ingredient.class);
    }

    @Override
    public Mono<Long> countByExample(Ingredient example) {
        Query query = Query.query(buildCriteria(example));
        return r2dbcEntityTemplate.select(query, Ingredient.class)
                .count();
    }

    private Criteria buildCriteria(Ingredient example) {
        Criteria criteria = Criteria.empty();
        if (example.getName() != null) {
            criteria = criteria.and("name")
                    .like("%" + example.getName() + "%")
                    .ignoreCase(true);
        }
        if (Boolean.TRUE.equals(example.isDisplay())) {
            criteria = criteria.and("display").is(true);
        } else if (Boolean.FALSE.equals(example.isDisplay())) {
            criteria = criteria.and("display").is(false);
        }
        if (example.getType() != null) {
            criteria = criteria.and("type").is(example.getType());
        }
        return criteria;
    }
}
