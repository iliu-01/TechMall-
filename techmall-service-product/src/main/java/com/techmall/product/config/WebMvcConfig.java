package com.techmall.product.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${techmall.upload.path:E:/techmall/uploads}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 内部端点 /internal/** 不经过 Gateway，Feign 走网络 IP 非 localhost，无需拦截
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射上传文件为静态资源，使 /api/upload/filename 可访问
        registry.addResourceHandler("/api/upload/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
