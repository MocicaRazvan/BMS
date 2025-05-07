package com.mocicarazvan.templatemodule.email;

import com.mocicarazvan.templatemodule.email.impl.EmailUtilsImpl;
import com.mocicarazvan.templatemodule.exceptions.email.EmailFailToSend;
import com.mocicarazvan.templatemodule.exceptions.email.EmailMXFail;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailUtilsImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailMXCacher emailMXCacher;

    @InjectMocks
    private EmailUtilsImpl emailUtils;

    @Test
    @SneakyThrows
    void sendEmail_success() {
        Session session = Session.getInstance(new Properties());

        MimeMessage mimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(mimeMessage);
        StepVerifier.create(emailUtils.sendEmail("to", "subject", "text"))
                .verifyComplete();

        assertEquals("to", mimeMessage.getAllRecipients()[0].toString());
        assertEquals("subject", mimeMessage.getSubject());
    }

    @Test
    @SneakyThrows
    void sendEmail_error() {
        Session session = Session.getInstance(new Properties());

        MimeMessage mimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Error sending email")).when(mailSender).send(mimeMessage);

        StepVerifier.create(emailUtils.sendEmail("to", "subject", "text"))
                .expectErrorMatches(throwable -> {
                    if (throwable instanceof EmailFailToSend emailFailToSend) {
                        assertEquals("subject", emailFailToSend.getSubject());
                        assertEquals("to", emailFailToSend.getTo());
                        assertEquals("text", emailFailToSend.getContent());
                        return true;
                    }
                    return false;
                })
                .verify();

    }

    @Test
    @SneakyThrows
    void sendEmail_withMXCheck_success() {
        Session session = Session.getInstance(new Properties());
        MimeMessage mimeMessage = new MimeMessage(session);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(mimeMessage);
        when(emailMXCacher.getCachedMXCheck("example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(emailUtils.sendEmail("to@example.com", "subject", "text", true))
                .verifyComplete();

        assertEquals("to@example.com", mimeMessage.getAllRecipients()[0].toString());
        assertEquals("subject", mimeMessage.getSubject());
    }

    @Test
    @SneakyThrows
    void sendEmail_withMXCheck_invalidDomain() {
        when(emailMXCacher.getCachedMXCheck("invalid.com")).thenReturn(Mono.just(false));

        StepVerifier.create(emailUtils.sendEmail("to@invalid.com", "subject", "text", true))
                .expectErrorMatches(throwable -> throwable instanceof EmailMXFail)
                .verify();
    }

    @Test
    @SneakyThrows
    void verifyMX_validDomain() {
        when(emailMXCacher.getCachedMXCheck("example.com")).thenReturn(Mono.empty());
        when(emailMXCacher.setCachedMXCheck("example.com", true)).thenReturn(Mono.empty());

        StepVerifier.create(emailUtils.verifyMX("to@example.com"))
                .verifyComplete();
    }

    @Test
    @SneakyThrows
    void verifyMX_invalidDomain() {
        when(emailMXCacher.getCachedMXCheck("invalid.com")).thenReturn(Mono.empty());
        when(emailMXCacher.setCachedMXCheck("invalid.com", false)).thenReturn(Mono.empty());
        emailUtils.setMxCheckTimeoutSeconds(1);

        StepVerifier.create(emailUtils.verifyMX("to@invalid.com"))
                .expectErrorMatches(throwable -> throwable instanceof EmailMXFail)
                .verify();
    }

    @Test
    void encodeUrlQueryParam_encodesCorrectly() {
        String param = "test param";
        String encoded = emailUtils.encodeUrlQueryParam(param);

        assertEquals("test+param", encoded);
    }
}