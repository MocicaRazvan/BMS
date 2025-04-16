package com.mocicarazvan.ollamasearch.impl;

import com.mocicarazvan.ollamasearch.services.OllamaAPIService;
import com.mocicarazvan.ollamasearch.services.impl.EmbedServiceImpl;
import org.springframework.transaction.reactive.TransactionalOperator;

public class EmbedServiceTestImpl extends EmbedServiceImpl<EmbedModelImpl, EmbedModelRepositoryTestImpl> {
    public EmbedServiceTestImpl(OllamaAPIService ollamaAPIService, TransactionalOperator transactionalOperator, EmbedModelRepositoryTestImpl embedRepository) {
        super(ollamaAPIService, transactionalOperator, embedRepository);
    }

    @Override
    protected EmbedModelImpl createEmbedding() {
        return EmbedModelImpl.builder().build();
    }
}
