package com.mocicarazvan.gatewayservice.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.cloud.gateway.config.HttpClientProperties;
import org.springframework.cloud.gateway.config.conditional.ConditionalOnEnabledGlobalFilter;
import org.springframework.cloud.gateway.filter.WebsocketRoutingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import reactor.netty.http.server.WebsocketServerSpec;

import java.util.function.Supplier;

@Slf4j
@Configuration
public class WebSocketUpgradeConfig {
    
    @Bean
    @ConditionalOnEnabledGlobalFilter(WebsocketRoutingFilter.class)
    @Primary
    public ReactorNettyRequestUpgradeStrategy customRectorNettyStrategy(
            HttpClientProperties httpClientProperties) {

        Supplier<WebsocketServerSpec.Builder> builderSupplier = () -> {
            WebsocketServerSpec.Builder builder = WebsocketServerSpec.builder();
            HttpClientProperties.Websocket websocket = httpClientProperties.getWebsocket();
            PropertyMapper map = PropertyMapper.get();
            map.from(websocket::getMaxFramePayloadLength).whenNonNull().to(builder::maxFramePayloadLength);
            map.from(websocket::isProxyPing).to(builder::handlePing);
            builder.compress(true);
            return builder;
        };

        return new ReactorNettyRequestUpgradeStrategy(builderSupplier);
    }
}
