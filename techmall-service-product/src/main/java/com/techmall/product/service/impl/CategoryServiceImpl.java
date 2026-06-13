package com.techmall.product.service.impl;

import com.techmall.common.exception.BusinessException;
import com.techmall.common.result.ResultCode;
import com.techmall.product.entity.Category;
import com.techmall.product.mapper.CategoryMapper;
import com.techmall.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<Category> listWithTree() {
        // 查询所有分类（已按 parent_id, sort 排序）
        List<Category> allCategories = categoryMapper.selectAll();

        // 按 parentId 分组
        Map<Long, List<Category>> childrenMap = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() != 0)
                .collect(Collectors.groupingBy(Category::getParentId));

        // 构建树：找出顶级分类，并设置子分类
        List<Category> tree = new ArrayList<>();
        for (Category category : allCategories) {
            if (category.getParentId() == null || category.getParentId() == 0) {
                category.setChildren(childrenMap.getOrDefault(category.getId(), new ArrayList<>()));
                tree.add(category);
            }
        }
        return tree;
    }

    @Override
    public Category addCategory(Category category) {
        categoryMapper.insert(category);
        return category;
    }
}
