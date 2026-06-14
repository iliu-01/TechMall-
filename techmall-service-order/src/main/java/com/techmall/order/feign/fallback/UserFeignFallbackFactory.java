package com.techmall.order.feign.fallback;

import com.techmall.common.result.Result;
import com.techmall.common.result.ResultCode;
import com.techmall.order.feign.UserFeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class UserFeignFallbackFactory implements FallbackFactory<UserFeignClient> {
    @Override
    public UserFeignClient create(Throwable cause) {
        return new UserFeignClient() {
            @Override public Result<?> deductBalance(Long id, java.util.Map<String, Object> body) {
                return Result.fail(ResultCode.SERVICE_BUSY);
            }
            @Override
            public Result<?> getUserById(Long id) {
                return Result.fail(ResultCode.SERVICE_BUSY);
            }
        };
    }
}
