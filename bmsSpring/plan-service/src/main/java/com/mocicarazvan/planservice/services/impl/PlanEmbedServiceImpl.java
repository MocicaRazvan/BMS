package com.mocicarazvan.planservice.services.impl;


import com.mocicarazvan.ollamasearch.services.OllamaAPIService;
import com.mocicarazvan.ollamasearch.services.impl.EmbedServiceImpl;
import com.mocicarazvan.planservice.models.PlanEmbedding;
import com.mocicarazvan.planservice.repositories.PlanEmbeddingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

@Service
public class PlanEmbedServiceImpl extends EmbedServiceImpl<PlanEmbedding, PlanEmbeddingRepository> {


    public PlanEmbedServiceImpl(OllamaAPIService ollamaAPIService, TransactionalOperator transactionalOperator, PlanEmbeddingRepository embedRepository) {
        super(ollamaAPIService, transactionalOperator, embedRepository);
    }

    @Override
    protected PlanEmbedding createEmbedding() {
        return PlanEmbedding.builder().build();
    }
}
