package com.mocicarazvan.userservice.utils;

import io.pinggy.emails.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class EmailNormalizerWrapperHolder {

    public static final EmailNormalizer EmailNormalizer = new EmailNormalizerWrapper();

    public static class EmailNormalizerWrapper implements EmailNormalizer {

        private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        private final ConcurrentHashMap<String, EmailNormalizationStrategy> strategies = new ConcurrentHashMap<>();

        public EmailNormalizerWrapper() {
            this.strategies.put("gmail.com", new GmailNormalizationStrategy());
            this.strategies.put("outlook.com", new OutlookNormalizationStrategy());
            this.strategies.put("googlemail.com", new GmailNormalizationStrategy());
            this.strategies.put("live.com", new LiveNormalizationStrategy());
            this.strategies.put("hotmail.com", new HotmailNormalizationStrategy());
        }

        public String normalizeBase(String email) {
            if (email != null && !email.isEmpty()) {
                email = email.toLowerCase().strip();
                String[] parts = email.split("@");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid email format");
                } else {
                    String localPart = parts[0];
                    String domain = parts[1];
                    if (!EMAIL_PATTERN.matcher(domain).matches()) {
                        throw new IllegalArgumentException("Invalid domain format");
                    } else {
                        EmailNormalizationStrategy strategy = (EmailNormalizationStrategy) this.strategies.getOrDefault(domain, new DefaultNormalizationStrategy());
                        return strategy.normalizeEmailString(localPart, domain);
                    }
                }
            } else {
                throw new IllegalArgumentException("Email cannot be null or empty");
            }
        }

        @Override
        public void addStrategy(String domain, EmailNormalizationStrategy strategy) {
            if (domain != null && !domain.isEmpty()) {
                if (strategy == null) {
                    throw new IllegalArgumentException("Strategy cannot be null");
                } else {
                    this.strategies.put(domain, strategy);
                }
            } else {
                throw new IllegalArgumentException("Domain cannot be null or empty");
            }
        }

        @Override
        public String normalize(String email) {
            try {
                return this.normalizeBase(email);
            } catch (IllegalArgumentException e) {
                return email;
            }
        }
    }

}
