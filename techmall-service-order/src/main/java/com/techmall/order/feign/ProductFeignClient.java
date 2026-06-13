package com.techmall.order.feign;

import com.techmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.techmall.order.feign.fallback.ProductFeignFallbackFactory;

import java.util.Map;

@FeignClient(name = "techmall-product-service", fallbackFactory = ProductFeignFallbackFactory.class)
public interface ProductFeignClient {
    @GetMapping("/internal/product/{id}")
    Result<?> getProduct(@PathVariable("id") Long id);

    @PutMapping("/internal/product/{id}/stock")
    Result<?> deductStock(@PathVariable("id") Long id, @RequestBody Map<String, Integer> body);
}
