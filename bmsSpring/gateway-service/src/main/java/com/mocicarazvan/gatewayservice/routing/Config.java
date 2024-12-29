package com.mocicarazvan.gatewayservice.routing;

import com.mocicarazvan.gatewayservice.config.ExternalServicesConfig;
import com.mocicarazvan.gatewayservice.filters.AuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.Buildable;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
public class Config {

    private final ExternalServicesConfig externalServicesConfig;

    @Bean
    public CorsWebFilter corsFilter(
            @Value("${front.url}") String frontUrl
    ) {
        CorsConfiguration configuration = new CorsConfiguration();

        Arrays.asList(frontUrl.split(",")).forEach(configuration::addAllowedOrigin);

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        configuration.addAllowedHeader("*");
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "Accept", "X-Requested-With", "Origin", "Range"));
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Authorization", "Content-Type", "Content-Range", "Content-Length", "Content-Disposition", "Accept-Ranges"));
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
    @Profile("!k8s")
    public RouteLocator gatewayRouteLocator(RouteLocatorBuilder builder, AuthFilter authFilter) {
        return builder.routes()
                .route("internal-paths", routeInternal())
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
                .route("day-service", r -> r.path("/days/**", "/meals/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://day-service"))
                .route("diffusion-service", r -> r.path("/diffusion/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .filter(authFilter)
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_UNIQUE"))
                        .uri(externalServicesConfig.getDiffusion()))

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
                .route("day-openapi", r -> r.path("/day-service/v3/api-docs")
                        .uri("lb://day-service"))


                .build();
    }

    @Bean
    @Profile("k8s")
    public RouteLocator gatewayRouteLocatorK8s(RouteLocatorBuilder builder, AuthFilter authFilter) {
        return builder.routes()
                .route("internal-paths", routeInternal())
                .route("file-service", r -> r.path("/files/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://file-service:8090"))
                .route("auth-service", r -> r.path("/auth/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://user-service:8081"))
                .route("user-service", r -> r.path("/users/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://user-service:8081"))
                .route("post-service", r -> r.path("/posts/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://post-service:8082"))
                .route("comment-service", r -> r.path("/comments/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://comment-service:8083"))
                .route("websocket-service-ws", r -> r.path("/ws/**")
                        .filters(f -> f.stripPrefix(1)
                                .filter(authFilter)
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_UNIQUE")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_UNIQUE")
                        )
                        .uri("http://websocket-service:8089"))
                .route("websocket-service-http", r -> r.path("/ws-http/**")
                        .filters(f -> f.stripPrefix(1)
                                .filter(authFilter)
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_UNIQUE")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_UNIQUE")
                        )
                        .uri("http://websocket-service:8089"))
                .route("ingredient-service", r -> r.path("/ingredients/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://ingredient-service:8084"))
                .route("recipe-service", r -> r.path("/recipes/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://recipe-service:8085"))
                .route("plan-service", r -> r.path("/plans/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://plan-service:8086"))
                .route("order-service", r -> r.path("/orders/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://order-service:8087"))
                .route("kanban-service", r -> r.path("/kanban/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://kanban-service:8088"))
                .route("day-service", r -> r.path("/days/**", "/meals/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://day-service:8091"))
                .route("diffusion-service", r -> r.path("/diffusion/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .filter(authFilter)
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_UNIQUE"))
                        .uri(externalServicesConfig.getDiffusion()))


                .route("user-openapi", r -> r.path("/user-service/v3/api-docs")
                        .uri("http://user-service:8081"))
                .route("file-openapi", r -> r.path("/file-service/v3/api-docs")
                        .uri("http://file-service:8090"))
                .route("post-openapi", r -> r.path("/post-service/v3/api-docs")
                        .uri("http://post-service:8082"))
                .route("comment-openapi", r -> r.path("/comment-service/v3/api-docs")
                        .uri("http://comment-service:8083"))
                .route("ingredient-openapi", r -> r.path("/ingredient-service/v3/api-docs")
                        .uri("http://ingredient-service:8084"))
                .route("recipe-openapi", r -> r.path("/recipe-service/v3/api-docs")
                        .uri("http://recipe-service:8085"))
                .route("order-openapi", r -> r.path("/order-service/v3/api-docs")
                        .uri("http://order-service:8087"))
                .route("websocket-openapi", r -> r.path("/websocket-service/v3/api-docs")
                        .uri("http://websocket-service:8089"))
                .route("plan-openapi", r -> r.path("/plan-service/v3/api-docs")
                        .uri("http://plan-service:8086"))
                .route("order-openapi", r -> r.path("/order-service/v3/api-docs")
                        .uri("http://order-service:8087"))
                .route("kanban-openapi", r -> r.path("/kanban-service/v3/api-docs")
                        .uri("http://kanban-service:8088"))
                .route("day-openapi", r -> r.path("/day-service/v3/api-docs")
                        .uri("http://day-service:8091"))


                .build();
    }

    private Function<PredicateSpec, Buildable<Route>> routeInternal() {
        return r -> r.path("/posts/internal/**",
                        "/comments/internal/**",
                        "/users/internal/**",
                        "/ws/internal/**",
                        "/ws-http/internal/**",
                        "/ingredients/internal/**",
                        "/files/internal/**",
                        "/recipes/internal/**",
                        "/plans/internal/**",
                        "/orders/internal/**",
                        "/kanban/internal/**",
                        "/days/internal/**",
                        "/meals/internal/**"
                )
                .filters(f -> f.filter(((exchange, chain) -> {
                    exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
                    return exchange.getResponse().setComplete();

                }))).uri("no://op");
    }
}
