package com.mocicarazvan.templatemodule.email.impl;


import com.mocicarazvan.templatemodule.email.EmailMXCacher;
import com.mocicarazvan.templatemodule.email.EmailUtils;
import com.mocicarazvan.templatemodule.exceptions.email.EmailFailToSend;
import com.mocicarazvan.templatemodule.exceptions.email.EmailMXFail;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;


@Slf4j
@RequiredArgsConstructor
@Setter
public class EmailUtilsImpl implements EmailUtils {


    private final JavaMailSender mailSender;
    private final EmailMXCacher emailMXCacher;
    private int mxCheckTimeoutSeconds = 10;


    @Override
    public Mono<Void> sendEmail(String to, String subject, String content) {
        return sendEmail(to, subject, content, false);
    }

    @Override
    public Mono<Void> sendEmail(String to, String subject, String content, boolean mxCheck) {
        // double context switch bc of virtual threads

        Mono<Void> emailSendMono = Mono.fromRunnable(() -> {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(content, true);
                mailSender.send(mimeMessage);
//                log.info("Email sent to: {}", to);
            } catch (Exception ex) {
                log.error("Error sending email to: {}", to, ex);
                throw new EmailFailToSend(to, subject, content);
            }
        }).then().subscribeOn(Schedulers.boundedElastic());

        if (mxCheck) {
            return verifyMX(to).then(emailSendMono);
        } else {
            return emailSendMono;
        }
    }

    @Override
    public Mono<Void> verifyMX(String email) {
        String domain = email.substring(email.indexOf('@') + 1);
        return emailMXCacher.getCachedMXCheck(domain)
                .switchIfEmpty(
                        Mono.defer(() ->
                                Mono.fromCallable(() -> isValidMX(domain))
                                        .flatMap(valid -> emailMXCacher.setCachedMXCheck(domain, valid)
                                                .thenReturn(valid))
                        )
                )
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new EmailMXFail(email)))
                .then()
                .subscribeOn(Schedulers.boundedElastic());
    }

    public boolean isValidMX(String domain) {
        try {
//            log.info("Checking MX records for domain: {}", domain);
            SimpleResolver resolver = new SimpleResolver();
            resolver.setTimeout(Duration.ofSeconds(mxCheckTimeoutSeconds));
            Lookup lookup = new Lookup(domain, Type.MX);
            lookup.setResolver(resolver);
            Record[] mxs = lookup.run();
            return mxs != null && mxs.length > 0;
        } catch (TextParseException | UnknownHostException e) {
            log.error("Error parsing email domain: {}", domain, e);
            return false;
        }
    }

    @Override
    public String encodeUrlQueryParam(String param) {
        return URLEncoder.encode(param, StandardCharsets.UTF_8);
    }


}
