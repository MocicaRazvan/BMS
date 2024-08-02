package com.mocicarazvan.gatewayservice.routing;

import com.mocicarazvan.gatewayservice.filters.AuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class Config {
    @Bean
    public CorsWebFilter corsFilter(
            @Value("${front.url}") String frontUrl
    ) {
        CorsConfiguration configuration = new CorsConfiguration();

        Arrays.asList(frontUrl.split(",")).forEach(configuration::addAllowedOrigin);

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.addAllowedHeader("*");
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "Accept", "X-Requested-With", "Origin"));
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return new CorsWebFilter(source);
    }

    @Bean
    AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }

    @Bean
    public RouteLocator gatewayRouteLocator(RouteLocatorBuilder builder, AuthFilter authFilter) {
        return builder.routes()
                .route("internal-paths", r -> r.path("/posts/internal/**",
                                "/comments/internal/**",
                                "/users/internal/**",
                                "/ws/internal/**",
                                "/ws-http/internal/**",
                                "/ingredients/internal/**",
                                "/files/internal/**",
                                "/recipes/internal/**",
                                "/plans/internal/**",
                                "/orders/internal/**",
                                "/kanban/internal/**"
                        )
                        .filters(f -> f.filter(((exchange, chain) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
                            return exchange.getResponse().setComplete();

                        }))).uri("no://op"))
                .route("file-service", r -> r.path("/files/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://file-service"))
                .route("auth-service", r -> r.path("/auth/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://user-service"))
                .route("user-service", r -> r.path("/users/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://user-service"))
                .route("post-service", r -> r.path("/posts/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://post-service"))
                .route("comment-service", r -> r.path("/comments/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://comment-service"))
                .route("websocket-service-ws", r -> r.path("/ws/**")
                        .filters(f -> f.stripPrefix(1)
                                .filter(authFilter)
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_UNIQUE")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_UNIQUE")
                        )
                        .uri("lb://websocket-service"))
                .route("websocket-service-http", r -> r.path("/ws-http/**")
                        .filters(f -> f.stripPrefix(1)
                                .filter(authFilter)
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_UNIQUE")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_UNIQUE")
                        )
                        .uri("lb://websocket-service"))
                .route("ingredient-service", r -> r.path("/ingredients/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://ingredient-service"))
                .route("recipe-service", r -> r.path("/recipes/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://recipe-service"))
                .route("plan-service", r -> r.path("/plans/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://plan-service"))
                .route("order-service", r -> r.path("/orders/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://order-service"))
                .route("kanban-service", r -> r.path("/kanban/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://kanban-service"))


                .route("user-openapi", r -> r.path("/user-service/v3/api-docs")
                        .uri("lb://user-service"))
                .route("file-openapi", r -> r.path("/file-service/v3/api-docs")
                        .uri("lb://file-service"))
                .route("post-openapi", r -> r.path("/post-service/v3/api-docs")
                        .uri("lb://post-service"))
                .route("comment-openapi", r -> r.path("/comment-service/v3/api-docs")
                        .uri("lb://comment-service"))
                .route("ingredient-openapi", r -> r.path("/ingredient-service/v3/api-docs")
                        .uri("lb://ingredient-service"))
                .route("recipe-openapi", r -> r.path("/recipe-service/v3/api-docs")
                        .uri("lb://recipe-service"))
                .route("order-openapi", r -> r.path("/order-service/v3/api-docs")
                        .uri("lb://order-service"))
                .route("websocket-openapi", r -> r.path("/websocket-service/v3/api-docs")
                        .uri("lb://websocket-service"))
                .route("plan-openapi", r -> r.path("/plan-service/v3/api-docs")
                        .uri("lb://plan-service"))
                .route("order-openapi", r -> r.path("/order-service/v3/api-docs")
                        .uri("lb://order-service"))
                .route("kanban-openapi", r -> r.path("/kanban-service/v3/api-docs")
                        .uri("lb://kanban-service"))


                .build();
    }
}
