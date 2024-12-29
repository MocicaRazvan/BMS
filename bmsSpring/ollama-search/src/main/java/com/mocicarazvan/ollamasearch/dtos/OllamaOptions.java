package com.mocicarazvan.ollamasearch.dtos;

import com.mocicarazvan.ollamasearch.config.OllamaPropertiesConfig;
import lombok.Data;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

@Data
public class OllamaOptions {
    private Map<String, Object> options = new HashMap<>();

    OllamaOptions(Map<String, Object> options) {
        this.options = options;
    }

    public static OllamaOptionsBuilder builder() {
        return new OllamaOptionsBuilder();
    }


    public static class OllamaOptionsBuilder {
        private Map<String, Object> options = new HashMap<>();

        OllamaOptionsBuilder() {
        }

        public OllamaOptionsBuilder fromConfig(OllamaPropertiesConfig config) {
            this.options.put("num_ctx", config.getNumCtx());
            return this;
        }

        public OllamaOptionsBuilder options(Map<String, Object> options) {
            this.options = options;
            return this;
        }

        public OllamaOptions build() {
            return new OllamaOptions(this.options);
        }


        public OllamaOptionsBuilder setMirostat(int value) {
            this.options.put("mirostat", value);
            return this;
        }

        public OllamaOptionsBuilder setMirostatEta(float value) {
            this.options.put("mirostat_eta", value);
            return this;
        }

        public OllamaOptionsBuilder setMirostatTau(float value) {
            this.options.put("mirostat_tau", value);
            return this;
        }

        public OllamaOptionsBuilder setNumCtx(int value) {
            this.options.put("num_ctx", value);
            return this;
        }

        public OllamaOptionsBuilder setNumGqa(int value) {
            this.options.put("num_gqa", value);
            return this;
        }

        public OllamaOptionsBuilder setNumGpu(int value) {
            this.options.put("num_gpu", value);
            return this;
        }

        public OllamaOptionsBuilder setNumThread(int value) {
            this.options.put("num_thread", value);
            return this;
        }

        public OllamaOptionsBuilder setRepeatLastN(int value) {
            this.options.put("repeat_last_n", value);
            return this;
        }

        public OllamaOptionsBuilder setRepeatPenalty(float value) {
            this.options.put("repeat_penalty", value);
            return this;
        }

        public OllamaOptionsBuilder setTemperature(float value) {
            this.options.put("temperature", value);
            return this;
        }

        public OllamaOptionsBuilder setSeed(int value) {
            this.options.put("seed", value);
            return this;
        }

        public OllamaOptionsBuilder setStop(String value) {
            this.options.put("stop", value);
            return this;
        }

        public OllamaOptionsBuilder setTfsZ(float value) {
            this.options.put("tfs_z", value);
            return this;
        }

        public OllamaOptionsBuilder setNumPredict(int value) {
            this.options.put("num_predict", value);
            return this;
        }

        public OllamaOptionsBuilder setTopK(int value) {
            this.options.put("top_k", value);
            return this;
        }

        public OllamaOptionsBuilder setTopP(float value) {
            this.options.put("top_p", value);
            return this;
        }

        public OllamaOptionsBuilder setMinP(float value) {
            this.options.put("min_p", value);
            return this;
        }

        public OllamaOptionsBuilder setCustomOption(@NonNull String name, @NonNull Object value) throws IllegalArgumentException {
            if (!(value instanceof Integer) && !(value instanceof Float) && !(value instanceof String)) {
                throw new IllegalArgumentException("Invalid type for parameter. Allowed types are: Integer, Float, or String.");
            } else {
                this.options.put(name, value);
                return this;
            }
        }
    }
}






