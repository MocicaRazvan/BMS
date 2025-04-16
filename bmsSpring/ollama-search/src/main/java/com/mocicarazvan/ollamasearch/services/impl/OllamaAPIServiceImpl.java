package com.mocicarazvan.ollamasearch.services.impl;

import com.mocicarazvan.ollamasearch.cache.EmbedCache;
import com.mocicarazvan.ollamasearch.clients.OllamaAPI;
import com.mocicarazvan.ollamasearch.config.OllamaPropertiesConfig;
import com.mocicarazvan.ollamasearch.dtos.OllamaOptions;
import com.mocicarazvan.ollamasearch.dtos.embed.OllamaEmbedRequestModel;
import com.mocicarazvan.ollamasearch.dtos.embed.OllamaEmbedResponseModel;
import com.mocicarazvan.ollamasearch.services.OllamaAPIService;
import com.mocicarazvan.ollamasearch.utils.OllamaQueryUtils;
import com.mocicarazvan.ollamasearch.utils.TextPreprocessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaAPIServiceImpl implements OllamaAPIService {

    private final OllamaPropertiesConfig ollamaPropertiesConfig;
    private final OllamaAPI ollamaAPI;
    private final OllamaQueryUtils ollamaQueryUtils;


    @Override
    public Mono<OllamaEmbedResponseModel> generateEmbedding(String text) {
        return embed(List.of(text));
    }

    @Override
    public Mono<OllamaEmbedResponseModel> generateEmbeddings(List<String> texts) {
        return embed(texts);
    }


    private static float[] getFloats(List<Double> doubleList) {
        float[] floatArray = new float[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
            floatArray[i] = doubleList.get(i).floatValue();
        }
        return floatArray;
    }

    @Override
    public Mono<OllamaEmbedResponseModel> generateEmbeddingWithCache(String text, EmbedCache embedCache) {
        return embedCache.getEmbedding(text, this::generateEmbedding)
                // the response serialization is big
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Float[]> generateEmbeddingFloatMonoWithCache(String text, EmbedCache embedCache) {
        return generateEmbeddingWithCache(text, embedCache).map(ollamaEmbedResponseModel -> {
            List<Double> doubleList = ollamaEmbedResponseModel.getEmbeddings().getFirst();
            return convertToFloat(doubleList);
        });
    }

    @Override
    public Mono<Float[]> generateEmbeddingFloatMono(String text) {
        return generateEmbedding(text)
                .map(ollamaEmbedResponseModel -> convertToFloat(ollamaEmbedResponseModel.getEmbeddings().getFirst()));
    }

    private Float[] convertToFloat(List<Double> doubleList) {
        Float[] floatArray = new Float[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
            floatArray[i] = doubleList.get(i).floatValue();
        }
        return floatArray;
    }


    private Mono<OllamaEmbedResponseModel> embed(List<String> inputs) {
//        String[] texts = inputs.stream().map(t -> t.toLowerCase().replaceAll("\\s+", " ").trim()).toArray(String[]::new);
        String[] texts = TextPreprocessor.preprocess(inputs);
        return ollamaAPI.embed(OllamaEmbedRequestModel.builder()
                .fromConfig(ollamaPropertiesConfig)
                .input(texts)
                .options(OllamaOptions.builder()
                        .fromConfig(ollamaPropertiesConfig)
                        .build())
                .build());

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
                ? generateEmbeddingWithCache(text, embedCache).map(ollamaQueryUtils::getEmbeddingsAsString)
                : Mono.just("");
    }

    @Override
    public boolean isNotNullOrEmpty(String text) {
        return text != null && !text.isBlank();
    }
}
