package com.mocicarazvan.ollamasearch.service;

import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;

import java.util.List;

public interface OllamaAPIService {

    OllamaEmbedResponseModel generateEmbedding(String text);

    OllamaEmbedResponseModel generateEmbeddings(List<String> texts);

    float[] generateEmbeddingFloat(String text);
}
