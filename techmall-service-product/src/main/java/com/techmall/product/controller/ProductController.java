package com.techmall.product.controller;

import com.techmall.common.dto.PageDTO;
import com.techmall.common.result.Result;
import com.techmall.product.entity.Product;
import com.techmall.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 公开 - 分页查询商品列表
     */
    @GetMapping("/list")
    public Result<PageDTO<Product>> list(@RequestParam(required = false) Long categoryId,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return Result.success(productService.listProducts(categoryId, keyword, page, size));
    }

    /**
     * 公开 - 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable("id") Long id) {
        return Result.success(productService.getProductById(id));
    }

    /**
     * 商家 - 新增商品
     */
    @PostMapping
    public Result<Product> add(@RequestBody Product product,
                               @RequestHeader("X-User-Id") Long userId) {
        return Result.success(productService.addProduct(product, userId));
    }

    /**
     * 商家 - 更新商品（仅能修改自己的）
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @RequestBody Product product,
                               @RequestHeader("X-User-Id") Long userId) {
        product.setId(id);
        productService.updateProduct(product, userId);
        return Result.success();
    }

    /**
     * 商家 - 删除商品（仅能删除自己的）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id,
                               @RequestHeader("X-User-Id") Long userId) {
        productService.deleteProduct(id, userId);
        return Result.success();
    }

    /**
     * 商家 - 上架/下架
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id,
                                     @RequestBody Map<String, Integer> body,
                                     @RequestHeader("X-User-Id") Long userId,
                                     @RequestHeader(value = "X-User-Role", defaultValue = "USER") String role) {
        productService.updateStatus(id, body.get("status"), userId, role);
        return Result.success();
    }
}
