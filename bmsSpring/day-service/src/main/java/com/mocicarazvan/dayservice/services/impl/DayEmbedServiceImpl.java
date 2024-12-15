package com.mocicarazvan.dayservice.services.impl;

import com.mocicarazvan.dayservice.models.DayEmbedding;
import com.mocicarazvan.dayservice.repositories.DayEmbeddingRepository;
import com.mocicarazvan.ollamasearch.services.OllamaAPIService;
import com.mocicarazvan.ollamasearch.services.impl.EmbedServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;


@Service
public class DayEmbedServiceImpl extends EmbedServiceImpl<DayEmbedding, DayEmbeddingRepository> {


    public DayEmbedServiceImpl(OllamaAPIService ollamaAPIService, TransactionalOperator transactionalOperator, DayEmbeddingRepository embedRepository) {
        super(ollamaAPIService, transactionalOperator, embedRepository);
    }

    @Override
    protected DayEmbedding createEmbedding() {
        return DayEmbedding.builder().build();
    }
}
