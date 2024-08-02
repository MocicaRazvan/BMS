package com.mocicarazvan.templatemodule.email;

import reactor.core.publisher.Mono;

public interface EmailUtils {

    Mono<Void> sendEmail(String to, String subject, String content);
}
