package com.techmall.gateway.filter;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class RoleAuthFilter extends AbstractGatewayFilterFactory<RoleAuthFilter.Config> {

    public RoleAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String role = request.getHeaders().getFirst("X-User-Role");
            String path = request.getURI().getPath();
            HttpMethod method = request.getMethod();

            // ADMIN-only paths
            if (path.equals("/api/user/list") && !"ADMIN".equals(role)) return forbidden(exchange);
            if (path.equals("/api/order/list") && !"ADMIN".equals(role)) return forbidden(exchange);

            // MERCHANT or ADMIN for product write ops
            if (path.startsWith("/api/product") && isWrite(method)) {
                if (!"MERCHANT".equals(role) && !"ADMIN".equals(role)) return forbidden(exchange);
            }

            // USER-only for creating orders
            if (path.equals("/api/order") && method == HttpMethod.POST && !"USER".equals(role))
                return forbidden(exchange);

            // ADMIN-only for creating categories
            if (path.equals("/api/category") && method == HttpMethod.POST && !"ADMIN".equals(role))
                return forbidden(exchange);

            return chain.filter(exchange);
        };
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = "{\"code\":403,\"message\":\"无权限\",\"data\":null}".getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    private boolean isWrite(HttpMethod m) {
        return m == HttpMethod.POST || m == HttpMethod.PUT || m == HttpMethod.DELETE;
    }

    @Data
    public static class Config {
        private List<String> adminPaths;
    }
}
