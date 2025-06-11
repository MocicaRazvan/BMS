package com.mocicarazvan.nextstaticserver.utils;

import java.util.regex.Pattern;

public class HeaderSanitizer {
    private static final Pattern ILLEGAL_CHARS = Pattern.compile("[\\x00-\\x1F\\x7F]");

    private HeaderSanitizer() {
    }

    public static String sanitize(String value) {
        if (value == null) return "";
        return ILLEGAL_CHARS.matcher(value).replaceAll("");
    }
}
