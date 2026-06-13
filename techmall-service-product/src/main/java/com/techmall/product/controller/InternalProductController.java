package com.techmall.product.controller;

import com.techmall.common.exception.BusinessException;
import com.techmall.common.result.Result;
import com.techmall.common.result.ResultCode;
import com.techmall.product.entity.Product;
import com.techmall.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/product")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductMapper productMapper;

    /**
     * 内部 - 根据 ID 获取完整商品信息
     */
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable("id") Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return Result.success(product);
    }

    /**
     * 内部 - 扣减库存
     */
    @PutMapping("/{id}/stock")
    public Result<Void> deductStock(@PathVariable("id") Long id,
                                    @RequestBody Map<String, Integer> body) {
        Integer quantity = body.get("quantity");
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 原子扣减：UPDATE ... WHERE stock >= quantity，检查影响行数
        int affected = productMapper.updateStock(id, quantity);
        if (affected == 0) {
            throw new BusinessException(ResultCode.STOCK_INSUFFICIENT);
        }
        return Result.success();
    }
}
