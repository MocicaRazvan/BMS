package com.mocicarazvan.templatemodule.email;

import reactor.core.publisher.Mono;

import java.text.Normalizer;

public interface EmailMXCacher {
    Mono<Boolean> getCachedMXCheck(String domain);

    Mono<Boolean> setCachedMXCheck(String domain, Boolean mxCheck);

    default String sanitizeDomain(String domain) {
        domain = Normalizer.normalize(domain.toLowerCase().strip(), Normalizer.Form.NFD);
        domain = domain.replaceAll("[^\\p{ASCII}]", "");
        return domain.replaceAll("[^a-z0-9]", "_") + "_" + Integer.toHexString(domain.hashCode());
    }

}
