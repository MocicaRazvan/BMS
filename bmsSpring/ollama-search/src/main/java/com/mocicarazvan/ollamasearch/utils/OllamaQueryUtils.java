package com.mocicarazvan.ollamasearch.utils;


import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OllamaQueryUtils {


    @Value("${spring.custom.ollama.threshold:0.61}")
    private double threshold;

    public String getEmbeddingsAsString(OllamaEmbedResponseModel resp) {
        return "[" +
                resp.getEmbeddings().getFirst().stream()
                        .map(Double::floatValue)
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ")) +
                "]";
    }

    public String addOrder(String vector) {
        if (isNullOrEmpty(vector)) {
            return "";
        }
        return String.format("(e.embedding <#> '%s')", vector);
    }

    public String addDistance(String vector) {

        return String.format("(e.embedding <#> '%s') * -1 AS distance", vector);
    }


    public String addThresholdFilter(String vector) {
        return addThresholdFilter(vector, threshold);
    }

    public String addThresholdFilter(String vector, String extraFilter) {
        return addThresholdFilter(vector, threshold, extraFilter);
    }

    public String addThresholdFilter(String vector, double threshold) {
        return addThresholdFilter(vector, threshold, "");
    }

    public String addThresholdFilter(String vector, double threshold, String extraFilter) {
        if (isNullOrEmpty(vector)) {
            return "";
        }
        if (isNullOrEmpty(extraFilter)) {
            return String.format("(e.embedding <#> '%s') * -1 >= %s", vector, threshold);
        }
        return String.format("( (e.embedding <#> '%s') * -1 >= %s %s )", vector, threshold, extraFilter);

    }

    public boolean isNullOrEmpty(String field) {
        return field == null || field.isEmpty();
    }
}
