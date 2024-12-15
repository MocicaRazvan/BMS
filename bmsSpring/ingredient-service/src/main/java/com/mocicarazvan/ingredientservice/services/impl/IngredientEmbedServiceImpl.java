package com.mocicarazvan.ingredientservice.services.impl;


import com.mocicarazvan.ingredientservice.models.IngredientEmbedding;
import com.mocicarazvan.ingredientservice.repositories.IngredientEmbeddingRepository;
import com.mocicarazvan.ollamasearch.services.OllamaAPIService;
import com.mocicarazvan.ollamasearch.services.impl.EmbedServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

@Service
public class IngredientEmbedServiceImpl extends EmbedServiceImpl<IngredientEmbedding, IngredientEmbeddingRepository> {


    public IngredientEmbedServiceImpl(OllamaAPIService ollamaAPIService, TransactionalOperator transactionalOperator, IngredientEmbeddingRepository embedRepository) {
        super(ollamaAPIService, transactionalOperator, embedRepository);
    }

    @Override
    protected IngredientEmbedding createEmbedding() {
        return IngredientEmbedding.builder().build();
    }
}
