package com.mocicarazvan.websocketservice.testUtils;

import org.springframework.jdbc.core.JdbcTemplate;

public class PgTrgmSimilarity {

    public static double similarity(String a, String b, JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForObject(
                "SELECT similarity(?, ?) AS similarity",
                Double.class,
                a, b
        );
    }

    public static boolean similarityGreaterThan(String a, String b, double threshold, JdbcTemplate jdbcTemplate) {
        return similarity(a, b, jdbcTemplate) > threshold;
    }
}
