package com.techmall.order.feign.fallback;

import com.techmall.common.result.Result;
import com.techmall.common.result.ResultCode;
import com.techmall.order.feign.ProductFeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProductFeignFallbackFactory implements FallbackFactory<ProductFeignClient> {
    @Override
    public ProductFeignClient create(Throwable cause) {
        return new ProductFeignClient() {
            @Override
            public Result<?> getProduct(Long id) {
                return Result.fail(ResultCode.SERVICE_BUSY);
            }

            @Override
            public Result<?> deductStock(Long id, Map<String, Integer> body) {
                return Result.fail(ResultCode.SERVICE_BUSY);
            }
        };
    }
}
