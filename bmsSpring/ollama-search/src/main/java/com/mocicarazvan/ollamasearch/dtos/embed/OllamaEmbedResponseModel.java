package com.mocicarazvan.ollamasearch.dtos.embed;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class OllamaEmbedResponseModel {
    @JsonProperty("model")
    private String model;
    @JsonProperty("embeddings")
    private List<List<Double>> embeddings;
    @JsonProperty("total_duration")
    private long totalDuration;
    @JsonProperty("load_duration")
    private long loadDuration;
    @JsonProperty("prompt_eval_count")
    private int promptEvalCount;

    @JsonProperty("model")
    public void setModel(@NonNull String model) {
        this.model = model;
    }

    @JsonProperty("embeddings")
    public void setEmbeddings(@NonNull List<List<Double>> embeddings) {
        this.embeddings = embeddings;
    }

    @JsonProperty("total_duration")
    public void setTotalDuration(long totalDuration) {
        this.totalDuration = totalDuration;
    }

    @JsonProperty("load_duration")
    public void setLoadDuration(long loadDuration) {
        this.loadDuration = loadDuration;
    }

    @JsonProperty("prompt_eval_count")
    public void setPromptEvalCount(int promptEvalCount) {
        this.promptEvalCount = promptEvalCount;
    }
}
