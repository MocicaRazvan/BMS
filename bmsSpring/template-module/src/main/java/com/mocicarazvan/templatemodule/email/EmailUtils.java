package com.mocicarazvan.templatemodule.email;

import reactor.core.publisher.Mono;

public interface EmailUtils {

    Mono<Void> sendEmail(String to, String subject, String content);

    Mono<Void> sendEmail(String to, String subject, String content, boolean mxCheck);

    Mono<Void> verifyMX(String email);

    String encodeUrlQueryParam(String param);
}
