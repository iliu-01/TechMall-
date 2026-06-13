package com.techmall.product.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${techmall.upload.path:E:/techmall/uploads}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InternalInterceptor())
                .addPathPatterns("/internal/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射上传文件为静态资源，使 /api/upload/filename 可访问
        registry.addResourceHandler("/api/upload/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    /**
     * 内部端点拦截器：仅允许 localhost 访问
     */
    private static class InternalInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String remoteAddr = request.getRemoteAddr();
            if ("127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr)) {
                return true;
            }
            response.setStatus(403);
            return false;
        }
    }
}
