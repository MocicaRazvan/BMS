package com.mocicarazvan.ollamasearch.dtos.embed;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mocicarazvan.ollamasearch.config.OllamaPropertiesConfig;
import com.mocicarazvan.ollamasearch.dtos.OllamaOptions;
import lombok.*;

import java.util.List;
import java.util.Map;


@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OllamaEmbedRequestModel {
    private String model;
    private List<String> input;
    private Map<String, Object> options;
    @JsonProperty("keep_alive")
    private String keepAlive;
    @JsonProperty("truncate")
    private Boolean truncate = true;

    public static OllamaEmbedRequestModelBuilder builder() {
        return new OllamaEmbedRequestModelBuilder();
    }

    public void setModel(@NonNull String model) {
        this.model = model;
    }

    public void setInput(@NonNull List<String> input) {
        this.input = input;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    @JsonProperty("keep_alive")
    public void setKeepAlive(String keepAlive) {
        this.keepAlive = keepAlive;
    }

    @JsonProperty("truncate")
    public void setTruncate(Boolean truncate) {
        this.truncate = truncate;
    }

    public static class OllamaEmbedRequestModelBuilder {
        private String model;
        private List<String> input;
        private Map<String, Object> options;
        private String keepAlive;
        private Boolean truncate;

        OllamaEmbedRequestModelBuilder() {
        }

        public OllamaEmbedRequestModelBuilder fromConfig(OllamaPropertiesConfig config) {
            this.model = config.getEmbeddingModel();
            this.keepAlive = config.getKeepalive();
            return this;
        }

        public OllamaEmbedRequestModelBuilder model(@NonNull String model) {
            this.model = model;
            return this;
        }

        public OllamaEmbedRequestModelBuilder input(List<String> input) {
            this.input = input;
            return this;
        }

        public OllamaEmbedRequestModelBuilder input(String input) {
            this.input = List.of(input);
            return this;
        }

        public OllamaEmbedRequestModelBuilder input(String[] input) {
            this.input = List.of(input);
            return this;
        }

        public OllamaEmbedRequestModelBuilder options(Map<String, Object> options) {
            this.options = options;
            return this;
        }

        public OllamaEmbedRequestModelBuilder options(OllamaOptions ollamaOptions) {
            this.options = ollamaOptions.getOptions();
            return this;
        }

        @JsonProperty("keep_alive")
        public OllamaEmbedRequestModelBuilder keepAlive(@NonNull String keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        @JsonProperty("truncate")
        public OllamaEmbedRequestModelBuilder truncate(@NonNull Boolean truncate) {
            this.truncate = truncate;
            return this;
        }

        public OllamaEmbedRequestModel build() {
            return new OllamaEmbedRequestModel(this.model, this.input, this.options, this.keepAlive, this.truncate);
        }

        public String toString() {
            return "OllamaEmbedRequestModel.OllamaEmbedRequestModelBuilder(model=" + this.model + ", input=" + this.input + ", options=" + this.options + ", keepAlive=" + this.keepAlive + ", truncate=" + this.truncate + ")";
        }
    }
}
