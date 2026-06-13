package com.techmall.product.controller;

import com.techmall.common.result.Result;
import com.techmall.product.entity.Category;
import com.techmall.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取分类树形结构
     */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.listWithTree());
    }

    /**
     * 新增分类
     */
    @PostMapping
    public Result<Category> add(@RequestBody Category category) {
        return Result.success(categoryService.addCategory(category));
    }
}
