package com.mocicarazvan.ollamasearch.services.impl;

import com.mocicarazvan.ollamasearch.annotations.EmbedRetry;
import com.mocicarazvan.ollamasearch.cache.EmbedCache;
import com.mocicarazvan.ollamasearch.config.OllamaPropertiesConfig;
import com.mocicarazvan.ollamasearch.exceptions.OllamaEmbedException;
import com.mocicarazvan.ollamasearch.services.OllamaAPIService;
import com.mocicarazvan.ollamasearch.utils.OllamaQueryUtils;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.embeddings.OllamaEmbedRequestBuilder;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;
import io.github.ollama4j.utils.OptionsBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaAPIServiceImpl implements OllamaAPIService {

    private final OllamaPropertiesConfig ollamaPropertiesConfig;
    private final OllamaAPI ollamaAPI;
    private final OllamaQueryUtils ollamaQueryUtils;


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
        return getFloats(doubleList);
    }

    private static float[] getFloats(List<Double> doubleList) {
        float[] floatArray = new float[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
            floatArray[i] = doubleList.get(i).floatValue();
        }
        return floatArray;
    }

    @Override
    public Mono<OllamaEmbedResponseModel> generateEmbeddingMono(String text, EmbedCache embedCache) {
        return embedCache.getEmbedding(text, this::generateEmbedding)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Float[]> generateEmbeddingFloatMono(String text, EmbedCache embedCache) {
        return generateEmbeddingMono(text, embedCache).map(ollamaEmbedResponseModel -> {
            List<Double> doubleList = ollamaEmbedResponseModel.getEmbeddings().getFirst();
            Float[] floatArray = new Float[doubleList.size()];
            for (int i = 0; i < doubleList.size(); i++) {
                floatArray[i] = doubleList.get(i).floatValue();
            }
            return floatArray;
        });
    }


    @EmbedRetry
    private OllamaEmbedResponseModel embed(List<String> inputs) {
        String[] texts = inputs.stream().map(t -> t.trim().replaceAll("\\s+", " ").toLowerCase()).toList().toArray(new String[0]);
        OptionsBuilder optionsBuilder = new OptionsBuilder();
        optionsBuilder.setNumCtx(ollamaPropertiesConfig.getNumCtx());
        try {
            return ollamaAPI.embed(OllamaEmbedRequestBuilder.getInstance(
                    ollamaPropertiesConfig.getEmbeddingModel(), texts
            ).withOptions(optionsBuilder.build()).withKeepAlive(ollamaPropertiesConfig.getKeepalive()).build());
        } catch (IOException | OllamaBaseException | InterruptedException e) {
            log.error("Error while embedding: " + e.getMessage());
            throw new OllamaEmbedException("Error while embedding: " + e.getMessage(), e);
        }
    }

    @Override
    public float[] convertToFloatPrimitive(Float[] floats) {
        float[] floatArray = new float[floats.length];
        for (int i = 0; i < floats.length; i++) {
            floatArray[i] = floats[i];
        }
        return floatArray;
    }

    @Override
    public Mono<String> getEmbedding(String text, EmbedCache embedCache) {
        return isNotNullOrEmpty(text)
                ? generateEmbeddingMono(text, embedCache).map(ollamaQueryUtils::getEmbeddingsAsString)
                : Mono.just("");
    }

    @Override
    public boolean isNotNullOrEmpty(String text) {
        return text != null && !text.isEmpty();
    }
}
