package com.techmall.product.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {

    private Long id;
    private String name;
    private String description;
    private String tags;
    private BigDecimal price;
    private Integer stock;
    private Long categoryId;
    private Long merchantId;
    private String imageUrl;
    /** 状态: 0-下架, 1-上架 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
