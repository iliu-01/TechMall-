package com.techmall.product.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Category {

    private Long id;
    private String name;
    private Long parentId;
    private Integer sort;
    private LocalDateTime createdAt;

    /** 子分类列表，仅用于树形结构返回，不参与数据库映射 */
    private List<Category> children;
}
