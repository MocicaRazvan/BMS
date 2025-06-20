package com.mocicarazvan.websocketservice.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ObjectMapper objectMapper;

    @Value("${front.url}")
    private String frontUrl;

    @Value("${spring.rabbitmq.host}")
    private String rabbitmqHost;

    @Value("${spring.rabbitmq.stomp.port}")
    private int rabbitmqStompPort;

    @Value("${spring.rabbitmq.username}")
    private String rabbitmqUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitmqPassword;

    private final SimpleAsyncTaskScheduler simpleAsyncTaskScheduler;

    public WebSocketConfig(ObjectMapper objectMapper, @Qualifier("simpleAsyncTaskScheduler") SimpleAsyncTaskScheduler simpleAsyncTaskScheduler) {
        this.objectMapper = objectMapper;
        this.simpleAsyncTaskScheduler = simpleAsyncTaskScheduler;
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        registry.enableSimpleBroker("/user", "/queue", "/chat");
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(rabbitmqHost)
                .setRelayPort(rabbitmqStompPort)
                .setClientLogin(rabbitmqUsername)
                .setClientPasscode(rabbitmqPassword)
                .setUserRegistryBroadcast("/topic/unresolved-user")
                .setUserDestinationBroadcast("/topic/resolved-user")
                .setSystemHeartbeatReceiveInterval(30000)
                .setSystemHeartbeatSendInterval(30000)
                .setTaskScheduler(simpleAsyncTaskScheduler);


        registry.setApplicationDestinationPrefixes("/app");

//        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("Allowed origins: {}", frontUrl);

        String[] allowedOrigins = frontUrl.split(",");
        
        registry.addEndpoint("/ws-service")
                .setAllowedOrigins(allowedOrigins);

        registry.addEndpoint("/ws-service-sockjs")
                .setAllowedOrigins(allowedOrigins)
                .withSockJS();
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setContentTypeResolver(resolver);
        messageConverters.add(converter);
        return false;
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(5 * 1024 * 1024);
        registry.setSendTimeLimit(50 * 10000);
        registry.setSendBufferSizeLimit(5 * 512 * 1024);

    }
}
