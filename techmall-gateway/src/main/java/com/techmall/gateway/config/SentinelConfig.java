package com.techmall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class SentinelConfig {

    @PostConstruct
    public void init() {
        GatewayCallbackManager.setBlockHandler((exchange, exception) ->
            ServerResponse.status(429)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"code\":429,\"message\":\"请求太频繁\",\"data\":null}")
        );
    }
}
