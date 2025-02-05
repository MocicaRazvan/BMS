package com.mocicarazvan.ollamasearch.utils;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class TextPreprocessor {

    public static String preprocess(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\p{C}", "")
                .replaceAll("\\s+", " ")
                .strip();
    }

    public static String[] preprocess(List<String> texts) {
        return texts.stream()
                .map(TextPreprocessor::preprocess)
                .toArray(String[]::new);
    }

    public static String[] preprocess(String[] texts) {
        return preprocess(List.of(texts));
    }
}
