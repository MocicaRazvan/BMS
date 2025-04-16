package com.mocicarazvan.templatemodule.email.impl;


import com.mocicarazvan.templatemodule.email.EmailUtils;
import com.mocicarazvan.templatemodule.exceptions.email.EmailFailToSend;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Slf4j
@RequiredArgsConstructor
public class EmailUtilsImpl implements EmailUtils {


    private final JavaMailSender mailSender;


    @Override
    public Mono<Void> sendEmail(String to, String subject, String content) {
        // double context switch bc of virtual threads
        return
                Mono.just(true)
                        .flatMap(_ ->
                                Mono.fromCallable(() -> {
                                            try {
                                                MimeMessage mimeMessage = mailSender.createMimeMessage();
                                                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                                                helper.setTo(to);
                                                helper.setSubject(subject);
                                                helper.setText(content, true);
                                                mailSender.send(mimeMessage);
                                                log.info("Email sent to: " + to);
                                                return Mono.empty();
                                            } catch (Exception ex) {
                                                log.error("Error sending email to: " + to, ex);
                                                throw new EmailFailToSend(to, subject, content);
                                            }
                                        })
                                        .subscribeOn(Schedulers.boundedElastic())
                        )
                        .then();
    }
}
