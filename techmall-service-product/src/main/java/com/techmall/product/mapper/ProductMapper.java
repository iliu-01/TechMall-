package com.techmall.product.mapper;

import com.techmall.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    Product selectById(@Param("id") Long id);

    List<Product> selectList(@Param("categoryId") Long categoryId,
                             @Param("keyword") String keyword);

    int insert(Product product);

    int updateById(Product product);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateStock(@Param("id") Long id, @Param("stock") Integer stock);

    int deleteById(@Param("id") Long id);
}
