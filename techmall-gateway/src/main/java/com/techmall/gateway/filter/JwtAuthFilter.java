package com.techmall.gateway.filter;

import com.techmall.common.utils.JwtUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
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
@Order(-1)
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITE_PATHS = List.of(
        "/api/user/login", "/api/user/register", "/api/user/merchants",
        "/api/product/list", "/api/category/list"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // 白名单 POST 放行
        if (isWhitePath(path, method)) {
            return chain.filter(exchange);
        }

        // GET 商品详情、GET 图片放行
        if (method == HttpMethod.GET) {
            if (path.startsWith("/api/product/") && !path.equals("/api/product/list")) return chain.filter(exchange);
            if (path.startsWith("/api/upload/")) return chain.filter(exchange);
        }

        // 提取 token
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "未登录");
        }
        String token = authHeader.substring(7);
        if (!JwtUtils.validateToken(token)) {
            return unauthorized(exchange, "token已过期");
        }

        // 注入 Header 传递给下游服务
        ServerHttpRequest modified = request.mutate()
            .header("X-User-Id", String.valueOf(JwtUtils.getUserId(token)))
            .header("X-User-Role", JwtUtils.getRole(token))
            .header("X-Username", JwtUtils.getUsername(token))
            .build();

        return chain.filter(exchange.mutate().request(modified).build());
    }

    private boolean isWhitePath(String path, HttpMethod method) {
        return WHITE_PATHS.stream().anyMatch(p -> path.equals(p) || path.startsWith(p + "/"));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = ("{\"code\":401,\"message\":\"" + msg + "\",\"data\":null}").getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
