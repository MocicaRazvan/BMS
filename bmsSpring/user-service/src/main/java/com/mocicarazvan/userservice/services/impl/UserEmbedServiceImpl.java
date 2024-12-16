package com.mocicarazvan.userservice.services.impl;

import com.mocicarazvan.ollamasearch.services.OllamaAPIService;
import com.mocicarazvan.ollamasearch.services.impl.EmbedServiceImpl;
import com.mocicarazvan.userservice.models.UserEmbedding;
import com.mocicarazvan.userservice.repositories.UserEmbeddingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;


@Service
public class UserEmbedServiceImpl extends EmbedServiceImpl<UserEmbedding, UserEmbeddingRepository> {


    public UserEmbedServiceImpl(OllamaAPIService ollamaAPIService, TransactionalOperator transactionalOperator, UserEmbeddingRepository embedRepository) {
        super(ollamaAPIService, transactionalOperator, embedRepository);
    }

    @Override
    protected UserEmbedding createEmbedding() {
        return UserEmbedding.builder().build();
    }
}
