package com.mocicarazvan.ollamasearch.service.impl;

import com.mocicarazvan.ollamasearch.annotations.EmbedRetry;
import com.mocicarazvan.ollamasearch.exceptions.OllamaEmbedException;
import com.mocicarazvan.ollamasearch.service.OllamaAPIService;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.embeddings.OllamaEmbedRequestBuilder;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaAPIServiceImpl implements OllamaAPIService {

    @Value("${spring.custom.ollama.embedding.model}")
    private String model;
    @Value("${spring.custom.ollama.keepalive:-1m}")
    private String keepAlive;
    private final OllamaAPI ollamaAPI;


    @Override
    public OllamaEmbedResponseModel generateEmbedding(String text) {
        return embed(List.of(text));
    }

    @Override
    public OllamaEmbedResponseModel generateEmbeddings(List<String> texts) {
        return embed(texts);
    }

    @Override
    public float[] generateEmbeddingFloat(String text) {
        List<Double> doubleList = generateEmbedding(text).getEmbeddings().getFirst();
        float[] floatArray = new float[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
            floatArray[i] = doubleList.get(i).floatValue();
        }
        return floatArray;
    }


    @EmbedRetry
    private OllamaEmbedResponseModel embed(List<String> inputs) {
        String[] texts = inputs.stream().map(t -> t.trim().toLowerCase()).toList().toArray(new String[0]);
        try {
            return ollamaAPI.embed(OllamaEmbedRequestBuilder.getInstance(
                    model, texts
            ).withoutTruncate().withKeepAlive(keepAlive).build());
        } catch (IOException | OllamaBaseException | InterruptedException e) {
            log.error("Error while embedding: " + e.getMessage());
            throw new OllamaEmbedException("Error while embedding: " + e.getMessage(), e);
        }
    }
}
