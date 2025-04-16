package com.mocicarazvan.ollamasearch.utils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class TextPreprocessor {
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}");


    public static String preprocess(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKD);
        String noDiacritics = DIACRITICS.matcher(normalized).replaceAll("");

        return Normalizer.normalize(noDiacritics, Normalizer.Form.NFKC)
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
        return preprocess(Arrays.asList(texts));
    }
}
