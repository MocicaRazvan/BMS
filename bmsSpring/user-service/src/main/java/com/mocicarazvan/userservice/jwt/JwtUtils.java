package com.mocicarazvan.userservice.jwt;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.exceptions.notFound.TokenNotFound;
import com.mocicarazvan.userservice.models.UserCustom;
import com.mocicarazvan.userservice.repositories.JwtTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtils {

    @Value("${security.hs256.key}")
    private String hs256Key;

    @Value("${security.jwt.expiration}")
    private int expiration;

    private final JwtTokenRepository jwtTokenRepository;
    private final ObjectMapper objectMapper;


    public String extractUsername(String jwt) {
        return extractClaim(jwt, Claims::getSubject);
    }


    public <T> T extractClaim(String jwt, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(jwt));
    }


    public String generateToken(UserCustom userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities())
                .claim("email", userDetails.getUsername())
                .claim("provider", userDetails.getProvider())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public Mono<Boolean> isTokenValid(String jwt, String username) {
        return jwtTokenRepository.findByToken(jwt)
                .flatMap(savedToken -> {
                            if (isTokenExpired(jwt) || savedToken.isRevoked() ||
                                    !username.equals(extractUsername(jwt))) {
                                savedToken.setRevoked(true);
                                return jwtTokenRepository.save(savedToken).thenReturn(false);
                            }
                            return Mono.just(true);
                        }
                )
                .switchIfEmpty(Mono.error(new TokenNotFound()));
    }

    public boolean isTokenExpired(String jwt) {
        return extractExpiration(jwt).before(new Date(System.currentTimeMillis()));
    }

    private Date extractExpiration(String jwt) {
        return extractClaim(jwt, Claims::getExpiration);
    }

    public Claims extractAllClaims(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(hs256Key));
    }

    public Mono<Void> createResponse(ServerWebExchange exchange, HttpStatus status, RuntimeException exception) {
        ServerHttpResponse response = exchange.getResponse();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("message", exception.getMessage());
        resp.put("timestamp", Instant.now().toString());
        resp.put("error", status.getReasonPhrase());
        resp.put("status", status.value());
        resp.put("path", exchange.getRequest().getPath().value());

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            return response.writeWith(Mono.just(response.bufferFactory().wrap(objectMapper.writeValueAsBytes(resp))));
        } catch (Exception e) {
            log.error("Error while writing response", e);
            return Mono.error(e);
        }
    }

}
