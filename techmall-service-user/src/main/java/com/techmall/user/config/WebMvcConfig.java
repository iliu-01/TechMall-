package com.techmall.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // 内部端点 /internal/** 不经过 Gateway 路由，外部无法直接访问
    // 无需 IP 拦截——Feign 通过服务名调用时走的是网络 IP 而非 localhost
}
