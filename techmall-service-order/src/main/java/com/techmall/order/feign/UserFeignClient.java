package com.techmall.order.feign;

import com.techmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import com.techmall.order.feign.fallback.UserFeignFallbackFactory;

@FeignClient(name = "techmall-user-service", fallbackFactory = UserFeignFallbackFactory.class)
public interface UserFeignClient {
    @GetMapping("/internal/user/{id}")
    Result<?> getUserById(@PathVariable("id") Long id);

    @PutMapping("/internal/user/{id}/deduct-balance")
    Result<?> deductBalance(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);
}
