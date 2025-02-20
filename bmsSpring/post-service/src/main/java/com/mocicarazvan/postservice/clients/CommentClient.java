package com.mocicarazvan.postservice.clients;


import com.mocicarazvan.postservice.dtos.comments.CommentResponse;
import com.mocicarazvan.templatemodule.clients.ClientBase;
import com.mocicarazvan.templatemodule.clients.ClientExceptionHandler;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CommentClient extends ClientBase {

    private final static String CLIENT_NAME = "commentService";

    @Value("${comment-service.url}")
    private String commentServiceUrl;

    public CommentClient(@Qualifier("commentWebClient") WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(CLIENT_NAME, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }


    public WebClient getClient() {
        return webClientBuilder.baseUrl(commentServiceUrl + "/comments").build();
    }


    public Mono<Void> deleteCommentsByPostId(String postId, String userId) {
        return getClient()
                .delete()
                .uri(uriBuilder ->
                        uriBuilder.path("/internal/post/{postId}")
                                .build(postId)

                )
                .header(RequestsUtils.AUTH_HEADER, userId)
                .accept(MediaType.APPLICATION_NDJSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> ClientExceptionHandler.handleClientException(response, serviceUrl, service))
                .bodyToMono(Void.class)
                .transform(this::applyResilience)
                .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, e -> Mono.error(new IllegalActionException("Post cannot be deleted")));

    }

    public Flux<ResponseWithUserDto<CommentResponse>> getCommentsByPost(String postId) {
        return getClient()
                .get()
                .uri(uriBuilder ->
                        uriBuilder.path("/internal/post/{postId}")
                                .build(postId)

                )
                .accept(MediaType.APPLICATION_NDJSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> ClientExceptionHandler.handleClientException(response, serviceUrl, service))
                .bodyToFlux(new ParameterizedTypeReference<ResponseWithUserDto<CommentResponse>>() {
                })
                .transform(this::applyResilience)
                .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                .onErrorResume(ThrowFallback.class, e -> Flux.empty());

    }


}
