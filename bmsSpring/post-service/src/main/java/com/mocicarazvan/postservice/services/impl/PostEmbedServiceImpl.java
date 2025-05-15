package com.mocicarazvan.postservice.services.impl;


import com.mocicarazvan.ollamasearch.services.OllamaAPIService;
import com.mocicarazvan.ollamasearch.services.impl.EmbedServiceImpl;
import com.mocicarazvan.postservice.models.PostEmbedding;
import com.mocicarazvan.postservice.repositories.PostEmbeddingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

@Service
public class PostEmbedServiceImpl extends EmbedServiceImpl<PostEmbedding, PostEmbeddingRepository> {

    public PostEmbedServiceImpl(OllamaAPIService ollamaAPIService, TransactionalOperator transactionalOperator, PostEmbeddingRepository embedRepository) {
        super(ollamaAPIService, transactionalOperator, embedRepository);
    }


    @Override
    protected PostEmbedding createEmbedding() {
        return PostEmbedding.builder().build();
    }
}
