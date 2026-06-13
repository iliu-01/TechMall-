package com.techmall.product.service;

import com.techmall.product.entity.Category;

import java.util.List;

public interface CategoryService {

    /**
     * 获取分类树形结构（顶级分类 + 子分类列表）
     */
    List<Category> listWithTree();

    /**
     * 新增分类
     */
    Category addCategory(Category category);
}
