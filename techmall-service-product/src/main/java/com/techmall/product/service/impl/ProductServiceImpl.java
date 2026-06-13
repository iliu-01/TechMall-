package com.techmall.product.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.techmall.common.dto.PageDTO;
import com.techmall.common.exception.BusinessException;
import com.techmall.common.result.ResultCode;
import com.techmall.product.entity.Product;
import com.techmall.product.mapper.ProductMapper;
import com.techmall.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public PageDTO<Product> listProducts(Long categoryId, String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        List<Product> list = productMapper.selectList(categoryId, keyword);
        PageInfo<Product> pageInfo = new PageInfo<>(list);
        return PageDTO.of(list, pageInfo.getTotal(), page, size);
    }

    @Override
    public Product getProductById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    @Override
    public Product addProduct(Product product, Long userId) {
        product.setMerchantId(userId);
        product.setStatus(1); // 默认上架
        productMapper.insert(product);
        return product;
    }

    @Override
    public void updateProduct(Product product, Long userId) {
        Product existing = productMapper.selectById(product.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (!existing.getMerchantId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        productMapper.updateById(product);
    }

    @Override
    public void deleteProduct(Long id, Long userId) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (!existing.getMerchantId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        productMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status, Long userId, String role) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        // ADMIN 可以操作任意商品，MERCHANT 只能操作自己的
        if (!"ADMIN".equals(role) && !existing.getMerchantId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        productMapper.updateStatus(id, status);
    }
}
