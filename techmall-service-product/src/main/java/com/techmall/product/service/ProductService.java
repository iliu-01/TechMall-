package com.techmall.product.service;

import com.techmall.common.dto.PageDTO;
import com.techmall.product.entity.Product;

public interface ProductService {

    /**
     * 分页查询商品列表（公开）
     */
    PageDTO<Product> listProducts(Long categoryId, String keyword, int page, int size, Long merchantId, Boolean includeOffShelf);

    /**
     * 根据 ID 获取商品详情（公开）
     */
    Product getProductById(Long id);

    /**
     * 新增商品（商家）
     */
    Product addProduct(Product product, Long userId);

    /**
     * 更新商品（商家，仅能修改自己的商品）
     */
    void updateProduct(Product product, Long userId);

    /**
     * 删除商品（商家，仅能删除自己的商品）
     */
    void deleteProduct(Long id, Long userId);

    /**
     * 更新商品状态（上架/下架），ADMIN 可操作任意商品，MERCHANT 仅自己
     */
    void updateStatus(Long id, Integer status, Long userId, String role);
}
