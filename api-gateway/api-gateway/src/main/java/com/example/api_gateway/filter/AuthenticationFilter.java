package com.example.api_gateway.filter;

import com.example.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouteValidator validator;
    private final JwtUtil jwtUtil;

    public AuthenticationFilter(RouteValidator validator, JwtUtil jwtUtil) {
        super(Config.class);
        this.validator = validator;
        this.jwtUtil = jwtUtil;
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (validator.isSecured.test(request)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(7);

                try {
                    // 1. Verify token & signature
                    jwtUtil.validateToken(token);
                    Claims claims = jwtUtil.extractAllClaims(token);

                    Object userId = claims.get("userId");
                    Object role = claims.get("role") != null ? claims.get("role") : claims.get("roles");
                    String email = claims.getSubject();

                    // 2. Mutate request headers to propagate user identity downstream
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Id", userId != null ? String.valueOf(userId) : "")
                            .header("X-User-Role", role != null ? String.valueOf(role) : "")
                            .header("X-User-Email", email != null ? email : "")
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());

                } catch (Exception e) {
                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                }
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }
}