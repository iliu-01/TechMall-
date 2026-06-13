package com.techmall.product.mapper;

import com.techmall.product.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {

    List<Category> selectAll();

    Category selectById(@Param("id") Long id);

    int insert(Category category);
}
