package com.techmall.order.feign;

import com.techmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.techmall.order.feign.fallback.UserFeignFallbackFactory;

@FeignClient(name = "techmall-user-service", fallbackFactory = UserFeignFallbackFactory.class)
public interface UserFeignClient {
    @GetMapping("/internal/user/{id}")
    Result<?> getUserById(@PathVariable Long id);
}
