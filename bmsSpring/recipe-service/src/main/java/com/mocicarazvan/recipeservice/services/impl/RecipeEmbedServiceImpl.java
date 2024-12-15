package com.mocicarazvan.recipeservice.services.impl;


import com.mocicarazvan.ollamasearch.services.OllamaAPIService;
import com.mocicarazvan.ollamasearch.services.impl.EmbedServiceImpl;
import com.mocicarazvan.recipeservice.models.RecipeEmbedding;
import com.mocicarazvan.recipeservice.repositories.RecipeEmbeddingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

@Service

public class RecipeEmbedServiceImpl extends EmbedServiceImpl<RecipeEmbedding, RecipeEmbeddingRepository> {


    public RecipeEmbedServiceImpl(OllamaAPIService ollamaAPIService, TransactionalOperator transactionalOperator, RecipeEmbeddingRepository embedRepository) {
        super(ollamaAPIService, transactionalOperator, embedRepository);
    }

    @Override
    protected RecipeEmbedding createEmbedding() {
        return RecipeEmbedding.builder().build();
    }
}
