package com.mocicarazvan.templatemodule.email.config;


import com.mocicarazvan.templatemodule.crypt.Crypt;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@RequiredArgsConstructor
@Slf4j
@Builder
public class MailConfig<S extends SecretProperties, M extends CustomMailProps> {


    private final M customMailProps;
    private final S properties;

    public JavaMailSender javaMailSender() throws Exception {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(customMailProps.getHost());
        mailSender.setPort(customMailProps.getPort());
        mailSender.setUsername(customMailProps.getUsername());
        mailSender.setPassword(Crypt.decryptPassword(properties.getSpringMailPassword(), properties.getSecret()));
        mailSender.getJavaMailProperties().putAll(customMailProps.getProperties());
        return mailSender;
    }
}
